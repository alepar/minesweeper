package ru.alepar.minesweeper;

public interface FieldApi {
    FieldState getCurrentField();

    void open(Point p);

    void markBomb(Point p);
}
