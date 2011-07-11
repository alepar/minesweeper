package ru.alepar.minesweeper.thirdparty;

import ru.alepar.minesweeper.model.*;

public class WinmineFieldApi implements FieldApi {

    private final Window window;

    private WinmineScreenshotFieldState fieldState;

    public WinmineFieldApi(Window window) {
        this.window = window;
        refreshFieldState();
    }

    private void refreshFieldState() {
        try {
            fieldState = new WinmineScreenshotFieldState(window.getScreenshot());
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
        window.leftClickAt(fieldState.clickCoordsForPoint(p));
        refreshFieldState();
        fieldState.blownUp(p);
    }

    @Override
    public void markBomb(Point p) {
        window.rightClickAt(fieldState.clickCoordsForPoint(p));
        refreshFieldState();
    }
}
