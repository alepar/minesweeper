package ru.alepar.minesweeper.core;


import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;

import java.util.HashSet;
import java.util.Set;

public class PointFilters {

    public static Set<Point> filter(Iterable<Point> points, Filter filter) {
        Set<Point> result = new HashSet<Point>();
        for (Point point : points) {
            if (filter.accept(point)) {
                result.add(point);
            }
        }
        return result;
    }

    public static Filter closedCellsOn(final FieldState field) {
        return new Filter() {
            @Override
            public boolean accept(Point p) {
                return !field.cellAt(p).isOpened();
            }
        };
    }

    public static Filter openedCellsOn(final FieldState field) {
        return new Filter() {
            @Override
            public boolean accept(Point p) {
                return field.cellAt(p).isOpened();
            }
        };
    }

    public static Filter bombCellsOn(final FieldState field) {
        return new Filter() {
            @Override
            public boolean accept(Point p) {
                return Cell.BOMB == field.cellAt(p);
            }
        };
    }

    public interface Filter {
        boolean accept(Point p);
    }
}
