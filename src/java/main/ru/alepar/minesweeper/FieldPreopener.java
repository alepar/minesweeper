package ru.alepar.minesweeper;

import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.Point;

public class FieldPreopener {

    public ArrayFieldState preopen(ArrayFieldState field) {
        Point zeroPoint = findZeroPoint(field);

        Cell[][] cells = new Cell[field.height()][];
        for (int y=0; y<field.height(); y++) {
            cells[y] = new Cell[field.width()];
            for (int x=0; x<field.width(); x++) {
                cells[y][x] = Cell.CLOSED;
            }
        }
        cells[zeroPoint.y][zeroPoint.x] = Cell.valueOf(0);

        return new ArrayFieldState(cells);
    }

    private Point findZeroPoint(ArrayFieldState field) {
        PointFactory pointFactory = new PointFactory(field.width(), field.height());
        for (Point p : pointFactory.allPoints()) {
            if(field.cellAt(p).value == 0) {
                return p;
            }
        }
        throw new RuntimeException("no zero point found");
    }

}
