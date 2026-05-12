package ru.alepar.minesweeper;

import ru.alepar.minesweeper.analyzer.*;
import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.*;
import ru.alepar.minesweeper.thirdparty.ResourceLauncher;
import ru.alepar.minesweeper.thirdparty.User32;
import ru.alepar.minesweeper.thirdparty.WinmineApplication;
import ru.alepar.minesweeper.thirdparty.WinmineFieldApi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Solver {

    private final FieldApi fieldApi;
    private final ResultExecutor executor;
    private final PointFactory pointFactory;
    private final LimitShuffler limitShuffler;
    private final Writer writer;

    private static BufferedWriter createDefaultWriter() {
        try {
            return new BufferedWriter(new FileWriter("output.tsv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Solver(FieldApi fieldApi, LimitShuffler limitShuffler) {
        this(fieldApi, limitShuffler, createDefaultWriter());
    }

    public Solver(FieldApi fieldApi, LimitShuffler limitShuffler, Writer writer) {
        this.fieldApi = fieldApi;
        this.limitShuffler = limitShuffler;
        this.writer = writer;

        this.pointFactory = new PointFactory(fieldApi.getCurrentField().width(), fieldApi.getCurrentField().height());
        this.executor = new ResultExecutor(fieldApi, pointFactory);
    }

    public FieldState solve() throws SteppedOnABomb {
        try {
            FieldState last;
            FieldState current = fieldApi.getCurrentField();
            while (true) {
                do {
                    last = current;
                    current = executor.execute(createConfidentAnalyzer().solve());
                } while (!last.equals(current));

                if(!hasClosedCells(current)) {
                    return current;
                }

                // Tank analysis runs the same enumeration we'd need for guessing
                // anyway. While we're there, harvest forced moves the local-only
                // MinMax propagator couldn't see -- cells that are bombs (or safe)
                // in every globally-consistent placement of their component. If
                // we found any, act on them and re-run the MinMax loop on the
                // new state; only fall through to guessing once tank is also out
                // of certainties.
                TankProbabilityAnalyzer.Analysis analysis = createTankAnalyzer().analyze();
                if (analysis.hasCertainties()) {
                    current = executor.execute(analysis.certaintiesAsResult(pointFactory));
                    continue;
                }

                fieldApi.open(analysis.pickLowestProbability());
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) { }
            }
        }
    }

    private static boolean hasClosedCells(FieldState field) {
        for (int x=0; x<field.width(); x++) {
            for (int y=0; y<field.height(); y++) {
                if(field.cellAt(new Point(x, y)) == Cell.CLOSED) {
                    return true;
                }
            }
        }
        return false;
    }

    private ConfidentAnalyzer createConfidentAnalyzer() {
        return new MinMaxAnalyzer(pointFactory, fieldApi.getCurrentField(), limitShuffler, writer);
    }

    private TankProbabilityAnalyzer createTankAnalyzer() {
        return new TankProbabilityAnalyzer(pointFactory, fieldApi.getCurrentField(), fieldApi.bombsLeft(), writer);
    }

    public static void main(String[] args) throws SteppedOnABomb {
        final WinmineApplication app = new WinmineApplication(User32.USER32, new ResourceLauncher());
        try {
            final WinmineFieldApi fieldApi = new WinmineFieldApi(app.getWindow());
            final Solver solver = new Solver(fieldApi, new SubtractIntersectLimitShuffler());
            solver.solve();
        } finally {
//            app.close();
        }
    }

}
