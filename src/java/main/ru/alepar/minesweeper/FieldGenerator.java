package ru.alepar.minesweeper;

import ru.alepar.minesweeper.fieldstate.ArrayFieldState;

public class FieldGenerator {

    private final int width;
    private final int height;
    private final int numOfBombs;

    public FieldGenerator(int width, int height, int numOfBombs) {
        this.width = width;
        this.height = height;
        this.numOfBombs = numOfBombs;
    }

    public ArrayFieldState generate() {
        throw new RuntimeException("alepar, implement me!");
    }
}
