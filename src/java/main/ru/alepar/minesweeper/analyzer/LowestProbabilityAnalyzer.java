package ru.alepar.minesweeper.analyzer;

import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.Region;

import java.io.Writer;
import java.util.*;

public class LowestProbabilityAnalyzer implements GuessingAnalyzer {

    private final PointFactory pointFactory;
    private final FieldState currentField;
    private final int bombsLeft;
    private final Writer writer;

    public LowestProbabilityAnalyzer(PointFactory pointFactory, FieldState currentField, int bombsLeft, Writer writer) {
        this.pointFactory = pointFactory;
        this.currentField = currentField;
        this.bombsLeft = bombsLeft;
        this.writer = writer;
    }

    @Override
    public Point guessWhatToOpen() {
        final Region closedCells = pointFactory.closedCellsOn(currentField);
        final Region openedCells = pointFactory.openedCellsOn(currentField);
        if(closedCells.isEmpty()) {
            throw new RuntimeException("there are no closed cells to choose from");
        }

        Set<Point> borderPoints = new HashSet<>();
        Set<Point> innerPoints = new HashSet<>();
        for (Point p : pointFactory.toPoints(closedCells)) {
            if(pointFactory.adjacentTo(p).intersect(openedCells).isEmpty()) {
                innerPoints.add(p);
            } else {
                borderPoints.add(p);
            }
        }

        MinMaxAnalyzer minMaxAnalyzer = new MinMaxAnalyzer(pointFactory, currentField, new SubtractIntersectLimitShuffler(), writer);
        Collection<Limit> limits = minMaxAnalyzer.shuffledLimits();

        // For each border cell, pick the per-limit probability coming from the
        // smallest region that contains it: smaller regions concentrate the
        // bomb count over fewer cells, giving a sharper estimate than averaging
        // across a big diluted region. Ties broken by the larger probability
        // (conservative for safety).
        Map<Point, Double> probability = new HashMap<>(borderPoints.size());
        Map<Point, Integer> bestRegionSize = new HashMap<>(borderPoints.size());
        for (Limit limit : limits) {
            Double limitProbability = calculateProbabilityFor(limit);
            int regionSize = limit.region.size();
            for (Point p : pointFactory.toPoints(limit.region)) {
                Integer curSize = bestRegionSize.get(p);
                if (curSize == null
                        || regionSize < curSize
                        || (regionSize == curSize && probability.get(p) < limitProbability)) {
                    probability.put(p, limitProbability);
                    bestRegionSize.put(p, regionSize);
                }
            }
        }

        SortedSet<ProbabilityPoint> guessing = new TreeSet<>();
        for (Map.Entry<Point, Double> entry : probability.entrySet()) {
            guessing.add(new ProbabilityPoint(entry.getKey(), entry.getValue()));
        }

        if (!innerPoints.isEmpty()) {
            // Bombs already accounted for by border limits should not be
            // re-charged against inner cells. Using the sum of per-border-cell
            // probabilities as an estimator for E[border bombs].
            double expectedBorderBombs = 0.0;
            for (Double p : probability.values()) {
                expectedBorderBombs += p;
            }
            double remainingForInner = Math.max(0.0, bombsLeft - expectedBorderBombs);
            double innerProbability = remainingForInner / innerPoints.size();
            for (Point point : innerPoints) {
                guessing.add(new ProbabilityPoint(point, innerProbability));
            }
        }

        return guessing.iterator().next().point;
    }

    private static Double calculateProbabilityFor(Limit limit) {
        long in=0, total=0;
        for(int i=limit.min; i<=limit.max; i++) {
            in += c(limit.region.size()-1, i-1);
            total += c(limit.region.size(), i);
        }
        return ((double)in)/total;
    }

    static long c(int n, int k) {
        long result = 1L;
        for (int i=n-k+1; i<=n; i++) {
            result *= i;
        }
        for (int i=2; i<=k; i++) {
            result /= i;
        }
        return result;
    }

    private static class ProbabilityPoint implements Comparable<ProbabilityPoint> {
        private final Point point;
        private final double probability;

        public ProbabilityPoint(Point point, double probability) {
            this.point = point;
            this.probability = probability;
        }

        @Override
        public int compareTo(ProbabilityPoint that) {
            return Double.compare(this.probability, that.probability);
        }
    }
}
