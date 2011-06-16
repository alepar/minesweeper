package ru.alepar.minesweeper.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Region {

    private final Set<Point> points;

    public Region(Set<Point> points) {
        this.points = Collections.unmodifiableSet(points);
    }

    public Set<Point> points() {
        return points;
    }

    public Region intersect(Region that) {
        Set<Point> intersection = new HashSet<Point>(this.points);
        intersection.retainAll(that.points);
        return new Region(intersection);
    }

    public Region subtract(Region that) {
        Set<Point> substract = new HashSet<Point>(this.points);
        substract.removeAll(that.points);
        return new Region(substract);
    }

    public boolean contains(Region that) {
        return this.points.containsAll(that.points);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Region region = (Region) o;

        return !(points != null ? !points.equals(region.points) : region.points != null);

    }

    @Override
    public int hashCode() {
        return points != null ? points.hashCode() : 0;
    }
}
