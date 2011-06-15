package ru.alepar.minesweeper;

public interface FieldApi {
    FieldState getCurrentField();

    void open(Point p) throws SteppedOnABomb;

    void markBomb(Point p);
}
