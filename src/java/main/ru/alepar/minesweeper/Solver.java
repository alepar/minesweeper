package ru.alepar.minesweeper;

import ru.alepar.minesweeper.analyzer.MinMaxAnalyzer;
import ru.alepar.minesweeper.analyzer.ResultExecutor;
import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.SteppedOnABomb;

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
        do {
            last = current;
            current = executor.execute(createMinMaxAnalyzer().solve());
        } while (!last.equals(current));
        return current;
    }

    private MinMaxAnalyzer createMinMaxAnalyzer() {
        return new MinMaxAnalyzer(pointFactory, fieldApi.getCurrentField());
    }

}
