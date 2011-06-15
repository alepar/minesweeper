package ru.alepar.minesweeper;

import java.util.Collections;
import java.util.Set;

public class Limit {

    public final Set<Point> points;
    public final int min;
    public final int max;

    public Limit(Set<Point> points, int min, int max) {
        this.points = Collections.unmodifiableSet(points);
        this.min = min;
        this.max = max;
    }

}
