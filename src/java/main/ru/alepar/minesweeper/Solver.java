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
    private final Writer writer = createWriter();

    private static BufferedWriter createWriter() {
        try {
            return new BufferedWriter(new FileWriter("output.tsv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Solver(FieldApi fieldApi, LimitShuffler limitShuffler) {
        this.fieldApi = fieldApi;
        this.limitShuffler = limitShuffler;

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

                fieldApi.open(createGuessingAnalyzer().guessWhatToOpen());
            }
        } finally {
            try {
                writer.close();
            } catch (IOException ignored) { }
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

    private GuessingAnalyzer createGuessingAnalyzer() {
        return new LowestProbabilityAnalyzer(pointFactory, fieldApi.getCurrentField(), fieldApi.bombsLeft(), writer);
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
