package ru.alepar.minesweeper.model;

import java.util.Arrays;

public final class Region {

    private final boolean[] array;

    public Region(int length) {
        array = new boolean[length];
    }

    public Region(boolean[] array) {
        this.array = array;
    }

    public int size() {
        int res = 0;
        for (boolean b : array) {
            if (b) {
                res++;
            }
        }
        return res;
    }

    public Region intersect(Region that) {
        final Region intersection = this.clone();
        intersection.and(that);
        return intersection;
    }

    public Region subtract(Region that) {
        final Region mask = that.clone();
        mask.invert();

        final Region subtract = this.clone();
        subtract.and(mask);

        return subtract;
    }

    public boolean contains(Region that) {
        return that.subtract(this).isEmpty();
    }

    @Override
    public Region clone() {
        return new Region(Arrays.copyOf(array, array.length));
    }

    private void and(Region that) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] & that.array[i];
        }
    }

    private void invert() {
        for (int i = 0; i < array.length; i++) {
            array[i] = !array[i];
        }
    }

    public void or(Region that) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] | that.array[i];
        }
    }

    public boolean get(int i) {
        return array[i];
    }

    public void set(int i) {
        array[i] = true;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Region region = (Region) o;

        return Arrays.equals(array, region.array);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }
}
