package ru.alepar.minesweeper.core;

import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.model.*;

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
        if (cell == Cell.BOMB) {
            throw new SteppedOnABomb(p);
        }

        current = current.mutate(p, cell);
    }

    @Override
    public void markBomb(Point p) {
        if (current.cellAt(p).isOpened()) {
            throw new RuntimeException("tried to mark as bomb alread open cell at " + p);
        }
        current = current.mutate(p, Cell.BOMB);
    }

    @Override
    public int bombsLeft() {
        int result = 0;
        for (int x=0; x<full.width(); x++) {
            for (int y=0; y<full.height(); y++) {
                Point p = new Point(x, y);
                if(current.cellAt(p) == Cell.CLOSED && full.cellAt(p) == Cell.BOMB) {
                    result++;
                }
            }
        }
        return result;
    }
}
