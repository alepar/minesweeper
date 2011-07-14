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

public class MinMaxAnalyzer implements ConfidentAnalyzer {

    private final PointFactory pointFactory;
    private final FieldState currentField;
    private final LimitShuffler limitShuffler;

    public MinMaxAnalyzer(PointFactory pointFactory, FieldState currentField, LimitShuffler limitShuffler) {
        this.pointFactory = pointFactory;
        this.currentField = currentField;
        this.limitShuffler = limitShuffler;
    }

    @Override
    public Result solve() {
        return openDeterminedLimits(shuffledLimits());
    }

    Set<Limit> shuffledLimits() {
        return shuffleLimits(createLimits());
    }

    private Set<Limit> shuffleLimits(Set<Limit> limits) {
        Set<Limit> shuffledLimits = limits;

        do {
            limits = shuffledLimits;
            shuffledLimits = shuffleLimitsOneIteration(shuffledLimits);
        } while (shuffledLimits.size() > limits.size());

        return shuffledLimits;
    }

    private Set<Limit> shuffleLimitsOneIteration(Set<Limit> limits) {
        limits = new HashSet<Limit>(limits);
        Set<Limit> shuffledLimits = new HashSet<Limit>();

        Iterator<Limit> it = limits.iterator();
        while (it.hasNext()) {
            Limit first = it.next();
            it.remove();
            shuffledLimits.add(first);

            for (Limit second : limits) {
                shuffledLimits.addAll(limitShuffler.shuffleLimitsPair(first, second));
            }
        }
        return shuffledLimits;
    }

    private Result openDeterminedLimits(Set<Limit> limits) {
        Set<Point> toOpen = new HashSet<Point>();
        Set<Point> toMark = new HashSet<Point>();
        for (Limit limit : limits) {
            if (limit.min == 0 && limit.max == 0) {
                for (Point p : limit.region.points()) {
                    toOpen.add(p);
                }
            } else if (limit.min == limit.region.points().size()) {
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
