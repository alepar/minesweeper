package ru.alepar.minesweeper.analyzer;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.Region;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MinMaxAnalyzer implements ConfidentAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(MinMaxAnalyzer.class);

    private final PointFactory pointFactory;
    private final FieldState currentField;
    private final LimitShuffler limitShuffler;

    private final Writer writer;

    private int iteration;

    public MinMaxAnalyzer(PointFactory pointFactory, FieldState currentField, LimitShuffler limitShuffler, Writer writer) {
        this.pointFactory = pointFactory;
        this.currentField = currentField;
        this.limitShuffler = limitShuffler;
        this.writer = writer;
    }

    @Override
    public Result solve() {
        return openDeterminedLimits(shuffledLimits());
    }

    Collection<Limit> shuffledLimits() {
        return shuffleLimits(createLimits());
    }

    private Collection<Limit> shuffleLimits(Set<Limit> initial) {
        Map<Region, Limit> known = new HashMap<>();
        Map<Region, Limit> last = new HashMap<>();
        for (Limit l : initial) {
            Limit tightened = installTightening(known, l);
            if (tightened != null) {
                last.put(l.region, tightened);
            }
        }

        while (!last.isEmpty()) {
            Map<Region, Limit> derived = shuffleLimitsOneIteration(known, last);

            Map<Region, Limit> next = new HashMap<>();
            for (Limit l : derived.values()) {
                Limit tightened = installTightening(known, l);
                if (tightened != null) {
                    next.put(l.region, tightened);
                }
            }
            log.warn("shuffleLimitsOneIteration()#{} added {} limits", iteration, next.size());
            iteration++;
            last = next;
        }

        return known.values();
    }

    private Map<Region, Limit> shuffleLimitsOneIteration(Map<Region, Limit> known, Map<Region, Limit> last) {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        Map<Region, Limit> result = new HashMap<>();

        // Pair every "newly tightened" limit against every known limit (which
        // includes the freshly-derived ones too). first != second guard avoids
        // self-pair edge cases.
        for (Limit first : last.values()) {
            for (Limit second : known.values()) {
                if (first == second) {
                    continue;
                }
                for (Limit derived : limitShuffler.shuffleLimitsPair(first, second)) {
                    installTightening(result, derived);
                }
            }
        }

        log.warn("shuffleLimitsOneIteration()#{} took {}ms, {} limits", iteration, stopwatch.elapsed(TimeUnit.MILLISECONDS), result.size());

        if (writer != null) {
            try {
                for (Limit limit : result.values()) {
                    writer.write(String.valueOf(iteration) + '\t' + limit.region.hashCode() + '\t' + limit.min + '\t' + limit.max + '\n');
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    // Merges `l` into `map` by interval-intersection on its region key.
    // Returns the resulting tightened/inserted Limit, or null if nothing changed.
    private static Limit installTightening(Map<Region, Limit> map, Limit l) {
        Limit cur = map.get(l.region);
        if (cur == null) {
            map.put(l.region, l);
            return l;
        }
        int newMin = Math.max(cur.min, l.min);
        int newMax = Math.min(cur.max, l.max);
        if (newMin == cur.min && newMax == cur.max) {
            return null;
        }
        Limit tighter = (newMin == l.min && newMax == l.max) ? l : new Limit(l.region, newMin, newMax);
        map.put(l.region, tighter);
        return tighter;
    }

    private Result openDeterminedLimits(Collection<Limit> limits) {
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
