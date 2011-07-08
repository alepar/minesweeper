package ru.alepar.minesweeper.thirdparty;

import ru.alepar.minesweeper.model.*;

public class WinmineFieldApi implements FieldApi {

    private final WinmineApplication app;

    public WinmineFieldApi(WinmineApplication app) {
        this.app = app;
    }

    @Override
    public FieldState getCurrentField() {
        try {
            return new WinmineScreenshotFieldState(app.getScreenshot());
        } catch (NativeException e) {
            throw new RuntimeException("failed to get current fieldstate", e);
        }
    }

    @Override
    public void open(Point p) throws SteppedOnABomb {
        try {
            app.leftClickAt(new WinmineScreenshotFieldState(app.getScreenshot()).clickCoordsForPoint(p));
        } catch (NativeException e) {
            throw new RuntimeException("failed to click cell", e);
        }
    }

    @Override
    public void markBomb(Point p) {
        throw new RuntimeException("fix me!");
    }
}
