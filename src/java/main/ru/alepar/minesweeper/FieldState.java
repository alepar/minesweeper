package ru.alepar.minesweeper;

public interface FieldState {
    int width();

    int height();

    Cell cellAt(Point p);
}
