package ru.alepar.minesweeper.analyzer;

import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.Region;

import java.util.HashSet;
import java.util.Set;

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
        Set<Limit> last = limits;
        Set<Limit> prev = new HashSet<>();

        while(true) {
            Set<Limit> current = shuffleLimitsOneIteration(prev, last);
            prev.addAll(last);
            last = new HashSet<>(current);

            current.removeAll(prev);
            if(current.isEmpty()) {
                break;
            }
        }

        return prev;
    }

    private Set<Limit> shuffleLimitsOneIteration(Set<Limit> prev, Set<Limit> last) {
        Set<Limit> result = new HashSet<>();

        for (Limit first : last) {
            for (Limit second : prev) {
                result.addAll(limitShuffler.shuffleLimitsPair(first, second));
            }
            for (Limit second : last) {
                if (first != second) {
                    result.addAll(limitShuffler.shuffleLimitsPair(first, second));
                }
            }
        }

        return result;
    }

    private Result openDeterminedLimits(Set<Limit> limits) {
        final Region toOpen = pointFactory.emptyRegion();
        final Region toMark = pointFactory.emptyRegion();
        for (Limit limit : limits) {
            if (limit.min == 0 && limit.max == 0) {
                toOpen.or(limit.region);
            } else if (limit.min == limit.region.size()) {
                toMark.or(limit.region);
            }
        }
        return new Result(toMark, toOpen);
    }

    private Set<Limit> createLimits() {
        final Set<Limit> result = new HashSet<>();
        final Region closedCells = pointFactory.closedCellsOn(currentField);
        final Region bombCellsOn = pointFactory.bombCellsOn(currentField);

        for (Point p : pointFactory.allPoints()) {
            final Cell cell = currentField.cellAt(p);
            if (cell.isOpened()) {
                final Region neighbours = pointFactory.adjacentTo(p);
                final Region closedNeighbours = neighbours.intersect(closedCells);
                final Region discoveredBombNeighbours = neighbours.intersect(bombCellsOn);

                final int bombsLeftUndiscovered = cell.value - discoveredBombNeighbours.size();
                final Region region = closedNeighbours.subtract(discoveredBombNeighbours);
                if (!region.isEmpty()) {
                    result.add(new Limit(region, bombsLeftUndiscovered, bombsLeftUndiscovered));
                }
            }
        }
        return result;
    }
}
