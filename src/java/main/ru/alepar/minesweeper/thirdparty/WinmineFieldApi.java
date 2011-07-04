package ru.alepar.minesweeper.thirdparty;

import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.SteppedOnABomb;

public class WinmineFieldApi implements FieldApi {

    private final WinmineApplication app;

    public WinmineFieldApi(WinmineApplication app) {
        this.app = app;
    }

    @Override
    public FieldState getCurrentField() {
        throw new RuntimeException("fix me!");
    }

    @Override
    public void open(Point p) throws SteppedOnABomb {
        throw new RuntimeException("fix me!");
    }

    @Override
    public void markBomb(Point p) {
        throw new RuntimeException("fix me!");
    }
}
