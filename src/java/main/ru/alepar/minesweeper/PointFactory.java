package ru.alepar.minesweeper;

import java.util.HashSet;
import java.util.Set;

public class PointFactory {

    private final int width;
    private final int height;

    public PointFactory(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Set<Point> allPoints() {
        Set<Point> result = new HashSet<Point>();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                result.add(new Point(x, y));
            }
        }
        return result;
    }

    public Set<Point> adjacentTo(Point p) {
        Set<Point> result = new HashSet<Point>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int x = p.x + dx;
                int y = p.y + dy;
                if (!(dx == 0 && dy == 0) && x >= 0 && x < width && y >= 0 && y < height) {
                    result.add(new Point(x, y));
                }
            }
        }
        return result;
    }
}
