package ru.alepar.minesweeper;

import java.util.Set;

public class PointFactory {

    private final int width;
    private final int height;

    public PointFactory(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Point[] allPoints() {
        throw new RuntimeException("todo");
    }

    public Set<Point> adjacentTo(Point p) {
        throw new RuntimeException("todo");
    }
}
