package ru.alepar.minesweeper.analyzer;

import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;

import java.util.*;

import static ru.alepar.minesweeper.core.PointFilters.*;

public class LowestProbabilityAnalyzer implements GuessingAnalyzer {

    private final PointFactory pointFactory;
    private final FieldState currentField;
    private final int bombsLeft;

    public LowestProbabilityAnalyzer(PointFactory pointFactory, FieldState currentField, int bombsLeft) {
        this.pointFactory = pointFactory;
        this.currentField = currentField;
        this.bombsLeft = bombsLeft;
    }

    @Override
    public Point guessWhatToOpen() {
        Set<Point> closedCells = filter(pointFactory.allPoints(), closedCellsOn(currentField));
        if(closedCells.isEmpty()) {
            throw new RuntimeException("there are no closed cells to choose from");
        }

        Set<Point> borderPoints = new HashSet<Point>();
        Set<Point> innerPoints = new HashSet<Point>();
        for (Point p : closedCells) {
            if(filter(pointFactory.adjacentTo(p), openedCellsOn(currentField)).isEmpty()) {
                innerPoints.add(p);
            } else {
                borderPoints.add(p);
            }
        }

        MinMaxAnalyzer minMaxAnalyzer = new MinMaxAnalyzer(pointFactory, currentField);
        Set<Limit> limits = minMaxAnalyzer.shuffledLimits();

        Map<Point, Double> probability = new HashMap<Point, Double>(borderPoints.size());
        for (Limit limit : limits) {
            Double limitProbability = calculateProbabilityFor(limit);
            for (Point p : limit.region.points()) {
                if(!probability.containsKey(p) || probability.get(p) < limitProbability) {
                    probability.put(p, limitProbability);
                }
            }
        }

        SortedSet<ProbabilityPoint> guessing = new TreeSet<ProbabilityPoint>();
        for (Map.Entry<Point, Double> entry : probability.entrySet()) {
            guessing.add(new ProbabilityPoint(entry.getKey(), entry.getValue()));
        }

        if (!innerPoints.isEmpty()) {
            double innerProbability = ((double) bombsLeft) / innerPoints.size();
            for (Point point : innerPoints) {
                guessing.add(new ProbabilityPoint(point, innerProbability));
            }
        }

        return guessing.iterator().next().point;
    }

    private static Double calculateProbabilityFor(Limit limit) {
        int in=0, total=0;
        for(int i=limit.min; i<=limit.max; i++) {
            in += c(limit.region.points().size()-1, i-1);
            total += c(limit.region.points().size(), i);
        }
        return ((double)in)/total;
    }

    private static int c(int n, int k) {
        int result = 1;
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
            return Double.compare(that.probability, this.probability);
        }
    }
}
