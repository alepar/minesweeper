package ru.alepar.minesweeper;

import java.awt.*;

public interface FieldState {
    int width();

    int height();

    Cell cellAt(Point p);
}
