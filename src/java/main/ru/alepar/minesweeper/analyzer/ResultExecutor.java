package ru.alepar.minesweeper.analyzer;

import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.SteppedOnABomb;

public class ResultExecutor {

    private final FieldApi fieldApi;

    public ResultExecutor(FieldApi fieldApi) {
        this.fieldApi = fieldApi;
    }

    public FieldState execute(FieldAnalyzer.Result result) throws SteppedOnABomb {
        for (Point p : result.toOpen) {
            fieldApi.open(p);
        }
        for (Point p : result.toMark) {
            fieldApi.markBomb(p);
        }
        return fieldApi.getCurrentField();
    }
}
