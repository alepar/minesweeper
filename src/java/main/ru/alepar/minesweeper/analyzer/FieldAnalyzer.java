package ru.alepar.minesweeper.analyzer;

import ru.alepar.minesweeper.model.Point;

import java.util.Set;

public interface FieldAnalyzer {
    Result solve();

    public class Result {
        public final Set<Point> toOpen;
        public final Set<Point> toMark;

        public Result(Set<Point> toMark, Set<Point> toOpen) {
            this.toMark = toMark;
            this.toOpen = toOpen;
        }
    }
}
