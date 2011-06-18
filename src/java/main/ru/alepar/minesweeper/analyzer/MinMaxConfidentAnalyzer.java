package ru.alepar.minesweeper.analyzer;

import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.Region;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static ru.alepar.minesweeper.core.PointFilters.*;

public class MinMaxConfidentAnalyzer implements FieldAnalyzer {

    private final PointFactory pointFactory;
    private final FieldState currentField;

    public MinMaxConfidentAnalyzer(PointFactory pointFactory, FieldState currentField) {
        this.pointFactory = pointFactory;
        this.currentField = currentField;
    }

    @Override
    public Result solve() {
        Set<Limit> limits = createLimits();
        limits = shuffleLimits(limits);
        return openDeterminedLimits(limits);
    }

    private static Set<Limit> shuffleLimits(Set<Limit> limits) {
        Set<Limit> shuffledLimits = limits;

        do {
            limits = shuffledLimits;
            shuffledLimits = shuffleLimitsOneIteration(shuffledLimits);
        } while (shuffledLimits.size() > limits.size());

        return shuffledLimits;
    }

    private static Set<Limit> shuffleLimitsOneIteration(Set<Limit> limits) {
        limits = new HashSet<Limit>(limits);
        Set<Limit> shuffledLimits = new HashSet<Limit>();

        Iterator<Limit> it = limits.iterator();
        while (it.hasNext()) {
            Limit first = it.next();
            it.remove();
            shuffledLimits.add(first);

            for (Limit second : limits) {
                shuffledLimits.addAll(shuffleLimitsPair(first, second));
            }
        }
        return shuffledLimits;
    }

    private static Set<Limit> shuffleLimitsPair(Limit first, Limit second) {

        if (first.region.contains(second.region)) {
            return subtractRegion(first, second);
        }

        if (second.region.contains(first.region)) {
            return subtractRegion(second, first);
        }

        Region intersect = first.region.intersect(second.region);
        if (!intersect.points().isEmpty()) {
            Set<Limit> result = new HashSet<Limit>();
            result.add(
                    new Limit(intersect,
                            Math.max(
                                    startLimitForIntersection(first, intersect),
                                    startLimitForIntersection(second, intersect)
                            ),
                            Math.min(
                                    endLimitForIntersection(first, intersect),
                                    endLimitForIntersection(second, intersect)
                            )
                    )
            );
            return result;
        }

        return Collections.emptySet();
    }

    private static Set<Limit> subtractRegion(Limit outer, Limit inner) {
        Region subtractRegion = outer.region.subtract(inner.region);
        if (subtractRegion.points().isEmpty()) {
            return Collections.emptySet();
        }

        Limit substraction = new Limit(
                subtractRegion,
                Math.max(0, outer.min - inner.max),
                Math.max(0, outer.max - inner.min)
        );

        return Collections.singleton(substraction);
    }

    private static int startLimitForIntersection(Limit src, Region dst) {
        return Math.max(0, src.min - src.region.points().size() + dst.points().size());
    }

    private static int endLimitForIntersection(Limit src, Region dst) {
        return Math.min(src.max, dst.points().size());
    }

    private Result openDeterminedLimits(Set<Limit> limits) {
        Set<Point> toOpen = new HashSet<Point>();
        Set<Point> toMark = new HashSet<Point>();
        for (Limit limit : limits) {
            if (limit.min == 0 && limit.max == 0) {
                for (Point p : limit.region.points()) {
                    toOpen.add(p);
                }
            } else if (limit.min == limit.max && limit.min == limit.region.points().size()) {
                for (Point p : limit.region.points()) {
                    toMark.add(p);
                }
            }
        }
        return new Result(toMark, toOpen);
    }

    private Set<Limit> createLimits() {
        Set<Limit> result = new HashSet<Limit>();
        for (Point p : pointFactory.allPoints()) {
            Cell cell = currentField.cellAt(p);
            if (cell.isOpened()) {
                Set<Point> closedNeighbours = filter(pointFactory.adjacentTo(p), closedCellsOn(currentField));
                Set<Point> discoveredBombNeighbours = filter(pointFactory.adjacentTo(p), bombCellsOn(currentField));
                int bombsLeftUndiscovered = cell.value - discoveredBombNeighbours.size();
                result.add(new Limit(new Region(closedNeighbours).subtract(new Region(discoveredBombNeighbours)), bombsLeftUndiscovered, bombsLeftUndiscovered));
            }
        }
        return result;
    }

}
