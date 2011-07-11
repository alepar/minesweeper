package ru.alepar.minesweeper.thirdparty;

import java.awt.image.BufferedImage;

public interface Window {
    void leftClickAt(Coords coords);

    void rightClickAt(Coords coords);

    BufferedImage getScreenshot() throws NativeException;

    void close();
}
