package ru.alepar.minesweeper;

public class Cell {

    public static final Cell CLOSED = new Cell(-1);
    public static final Cell BOMB = new Cell(-2);
    public static final Cell OPENED[] = new Cell[] {new Cell(0), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8)};

    public final int value;

    private Cell(int value) {
        this.value = value;
    }

    public static Cell valueOf(int i) {
        return OPENED[i];
    }
}
