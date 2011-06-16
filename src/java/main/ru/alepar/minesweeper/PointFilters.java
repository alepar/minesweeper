package ru.alepar.minesweeper;


import java.util.HashSet;
import java.util.Set;

public class PointFilters {

    public static Set<Point> filter(Iterable<Point> points, Filter filter) {
        Set<Point> result = new HashSet<Point>();
        for (Point point : points) {
            if(filter.accept(point)) {
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

    public interface Filter {
        boolean accept(Point p);
    }
}
