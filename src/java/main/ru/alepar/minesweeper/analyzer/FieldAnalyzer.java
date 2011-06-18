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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            if (toMark != null ? !toMark.equals(result.toMark) : result.toMark != null) return false;
            if (toOpen != null ? !toOpen.equals(result.toOpen) : result.toOpen != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = toOpen != null ? toOpen.hashCode() : 0;
            result = 31 * result + (toMark != null ? toMark.hashCode() : 0);
            return result;
        }
    }
}
