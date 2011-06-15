package ru.alepar.minesweeper;

public class SimpleFieldApi implements FieldApi {

    private final FieldState full;
    private final FieldState start;

    public SimpleFieldApi(FieldState full, FieldState start) {
        this.full = full;
        this.start = start;
    }

    @Override
    public FieldState getCurrentField() {
        throw new RuntimeException("todo");
    }

    @Override
    public void open(Point p) {
        throw new RuntimeException("todo");
    }

    @Override
    public void markBomb(Point p) {
        throw new RuntimeException("todo");
    }
}
