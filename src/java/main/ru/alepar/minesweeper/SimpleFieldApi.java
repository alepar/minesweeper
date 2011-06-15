package ru.alepar.minesweeper;

public class SimpleFieldApi implements FieldApi {

    private final ArrayFieldState full;
    private ArrayFieldState current;

    public SimpleFieldApi(ArrayFieldState full, ArrayFieldState start) {
        this.full = full;
        this.current = start;
    }

    @Override
    public FieldState getCurrentField() {
        return current;
    }

    @Override
    public void open(Point p) throws SteppedOnABomb {
        Cell cell = full.cellAt(p);
        if(cell == Cell.BOMB) {
            throw new SteppedOnABomb(p);
        }

        current = current.mutate(p, cell);
    }

    @Override
    public void markBomb(Point p) {
        if(current.cellAt(p).isOpened()) {
            throw new RuntimeException("tried to mark as bomb alread open cell at " + p);
        }
        current = current.mutate(p, Cell.BOMB);
    }

}
