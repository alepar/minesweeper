package ru.alepar.minesweeper.model;

public interface FieldState {
    int width();

    int height();

    Cell cellAt(Point p);
}
