package ru.alepar.minesweeper.core;


import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldApi;
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

    public static Filter closedCellsOn(final FieldApi api) {
        return new Filter() {
            @Override
            public boolean accept(Point p) {
                return !api.getCurrentField().cellAt(p).isOpened();
            }
        };
    }

    public static Filter bombCellsOn(final FieldApi api) {
        return new Filter() {
            @Override
            public boolean accept(Point p) {
                return Cell.BOMB == api.getCurrentField().cellAt(p);
            }
        };
    }

    public interface Filter {
        boolean accept(Point p);
    }
}
