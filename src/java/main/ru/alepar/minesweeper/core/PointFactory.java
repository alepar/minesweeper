package ru.alepar.minesweeper.core;

import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.Region;

import java.util.ArrayList;
import java.util.List;

public class PointFactory {

    private final int width;
    private final int height;
    private final List<Point> allPoints;

    public PointFactory(int width, int height) {
        this.width = width;
        this.height = height;

        allPoints = new ArrayList<>(height*width);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                allPoints.add(new Point(x, y));
            }
        }
    }

    public List<Point> toPoints(Region region) {
        final List<Point> points = new ArrayList<>(region.size());

        for (int i=0; i<width*height; i++) {
            if (region.get(i)) {
                points.add(new Point(i % width, i / width));
            }
        }

        return points;
    }

    public List<Point> allPoints() {
        return allPoints;
    }

    public Region adjacentTo(Point p) {
        final Region points = emptyRegion();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int x = p.x + dx;
                int y = p.y + dy;
                if (!(dx == 0 && dy == 0) && x >= 0 && x < width && y >= 0 && y < height) {
                    points.set(y*width + x);
                }
            }
        }
        return points;
    }

    public Region closedCellsOn(FieldState field) {
        final Region points = emptyRegion();
        for (Point p : allPoints) {
            if (!field.cellAt(p).isOpened()) {
                points.set(p.y * width + p.x);
            }
        }
        return points;
    }

    public Region openedCellsOn(FieldState field) {
        final Region points = emptyRegion();
        for (Point p : allPoints) {
            if (field.cellAt(p).isOpened()) {
                points.set(p.y * width + p.x);
            }
        }
        return points;
    }

    public Region bombCellsOn(FieldState field) {
        final Region points = emptyRegion();
        for (Point p : allPoints) {
            if (Cell.BOMB == field.cellAt(p)) {
                points.set(p.y * width + p.x);
            }
        }
        return points;
    }

    public Region emptyRegion() {
        return new Region(width*height);
    }
}
