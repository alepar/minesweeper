package ru.alepar.minesweeper.thirdparty;

import ru.alepar.minesweeper.model.*;

public class WinmineFieldApi implements FieldApi {

    private final WinmineApplication app;

    private WinmineScreenshotFieldState fieldState;

    public WinmineFieldApi(WinmineApplication app) {
        this.app = app;
        refreshFieldState();
    }

    private void refreshFieldState() {
        try {
            fieldState = new WinmineScreenshotFieldState(app.getScreenshot());
        } catch (NativeException e) {
            throw new RuntimeException("failed to refresh fieldstate", e);
        }
    }

    @Override
    public FieldState getCurrentField() {
        return fieldState;
    }

    @Override
    public void open(Point p) throws SteppedOnABomb {
        app.leftClickAt(fieldState.clickCoordsForPoint(p));
        refreshFieldState();
        fieldState.blownUp(p);
    }

    @Override
    public void markBomb(Point p) {
        app.rightClickAt(fieldState.clickCoordsForPoint(p));
        refreshFieldState();
    }
}
