package ru.alepar.minesweeper.analyzer;

import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.SteppedOnABomb;

public class ResultExecutor {

    private final FieldApi fieldApi;
    private final PointFactory pointFactory;

    public ResultExecutor(FieldApi fieldApi, PointFactory pointFactory) {
        this.fieldApi = fieldApi;
        this.pointFactory = pointFactory;
    }

    public FieldState execute(ConfidentAnalyzer.Result result) throws SteppedOnABomb {
        for (Point p : pointFactory.toPoints(result.toOpen)) {
            fieldApi.open(p);
        }
        for (Point p : pointFactory.toPoints(result.toMark)) {
            fieldApi.markBomb(p);
        }
        return fieldApi.getCurrentField();
    }
}
