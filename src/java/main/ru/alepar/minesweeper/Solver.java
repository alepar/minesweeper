package ru.alepar.minesweeper;

import ru.alepar.minesweeper.analyzer.*;
import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.*;

public class Solver {

    private final FieldApi fieldApi;
    private final ResultExecutor executor;
    private final PointFactory pointFactory;

    public Solver(FieldApi fieldApi) {
        this.fieldApi = fieldApi;

        executor = new ResultExecutor(fieldApi);
        pointFactory = new PointFactory(fieldApi.getCurrentField().width(), fieldApi.getCurrentField().height());
    }

    public FieldState solve() throws SteppedOnABomb {
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
        return new MinMaxAnalyzer(pointFactory, fieldApi.getCurrentField(), new SubtractIntersectLimitShuffler());
    }

    private GuessingAnalyzer createGuessingAnalyzer() {
        return new LowestProbabilityAnalyzer(pointFactory, fieldApi.getCurrentField(), fieldApi.bombsLeft());
    }

}
