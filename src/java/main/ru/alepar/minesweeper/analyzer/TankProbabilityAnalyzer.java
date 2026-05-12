package ru.alepar.minesweeper.analyzer;

import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.Region;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Replacement for {@link LowestProbabilityAnalyzer}. Computes per-cell bomb
 * probabilities by enumerating bomb placements within each connected component
 * of border cells (a.k.a. "tank" solver), instead of approximating with max()
 * across containing limits.
 *
 * <p>Two cells are in the same component if any limit's region contains both.
 * For each component up to {@link #BRUTE_FORCE_LIMIT} cells we enumerate all
 * 2^n assignments via bitmask and tally how many valid placements have each
 * cell as a bomb -- that ratio is the cell's exact P(bomb|local-limits).
 * Larger components fall back to a max-aggregation approximation.</p>
 *
 * <p>Unconstrained closed cells (closed cells not appearing in any limit) get
 * the residual prior: (bombsLeft - expected border bombs) / |unconstrained|.</p>
 */
public class TankProbabilityAnalyzer implements GuessingAnalyzer {

    // Backtracking packs the per-cell assignment into a single long, so 64
    // cells per component is the hard ceiling. Pruning typically collapses
    // the search well below the naive 2^N. Larger components fall back to
    // per-limit max-aggregation.
    private static final int MAX_COMPONENT_SIZE = 64;

    private final PointFactory pointFactory;
    private final FieldState currentField;
    private final int bombsLeft;
    private final Writer writer;

    public TankProbabilityAnalyzer(PointFactory pointFactory, FieldState currentField, int bombsLeft, Writer writer) {
        this.pointFactory = pointFactory;
        this.currentField = currentField;
        this.bombsLeft = bombsLeft;
        this.writer = writer;
    }

    @Override
    public Point guessWhatToOpen() {
        final Region closedCells = pointFactory.closedCellsOn(currentField);
        if (closedCells.isEmpty()) {
            throw new RuntimeException("there are no closed cells to choose from");
        }

        final MinMaxAnalyzer minMax = new MinMaxAnalyzer(
                pointFactory, currentField, new SubtractIntersectLimitShuffler(), writer);
        final Collection<Limit> limits = minMax.shuffledLimits();

        // Cells touched by any limit -- the "constrained" cells. Everything
        // else closed is unconstrained and gets the residual prior.
        // Cell.CLOSED and Cell.BOMB both have isOpened()=false, so
        // closedCellsOn() includes already-marked bombs; those must NOT enter
        // the unconstrained pool or the picker will happily try to open them.
        final Set<Point> constrained = new HashSet<>();
        for (Limit l : limits) {
            constrained.addAll(pointFactory.toPoints(l.region));
        }
        final Set<Point> unconstrained = new HashSet<>();
        for (Point p : pointFactory.toPoints(closedCells)) {
            if (currentField.cellAt(p) != Cell.BOMB && !constrained.contains(p)) {
                unconstrained.add(p);
            }
        }

        final List<Component> components = findComponents(limits);

        final Map<Point, Double> probability = new HashMap<>();
        double expectedBorderBombs = 0.0;
        for (Component c : components) {
            ComponentResult r = enumerateComponent(c);
            if (r != null && r.totalValid > 0) {
                for (int i = 0; i < c.cells.size(); i++) {
                    double p = ((double) r.bombCounts[i]) / r.totalValid;
                    probability.put(c.cells.get(i), p);
                    expectedBorderBombs += p;
                }
            } else {
                // Oversized (or empty solution set -- shouldn't happen) -> fall back
                // to per-limit probability with max() aggregation across containing limits.
                for (Limit limit : c.limits) {
                    double p = perLimitProbability(limit);
                    for (Point cell : pointFactory.toPoints(limit.region)) {
                        Double cur = probability.get(cell);
                        if (cur == null || cur < p) {
                            probability.put(cell, p);
                        }
                    }
                }
                for (Point cell : c.cells) {
                    Double p = probability.get(cell);
                    if (p != null) expectedBorderBombs += p;
                }
            }
        }

        SortedSet<ProbabilityPoint> guessing = new TreeSet<>();
        for (Map.Entry<Point, Double> e : probability.entrySet()) {
            guessing.add(new ProbabilityPoint(e.getKey(), e.getValue()));
        }

        if (!unconstrained.isEmpty()) {
            double remaining = Math.max(0.0, bombsLeft - expectedBorderBombs);
            double unconstrainedProb = remaining / unconstrained.size();
            for (Point p : unconstrained) {
                guessing.add(new ProbabilityPoint(p, unconstrainedProb));
            }
        }

        return guessing.iterator().next().point;
    }

    private List<Component> findComponents(Collection<Limit> limits) {
        // Union-find over Points, joined by co-occurrence in a limit.
        Map<Point, Point> parent = new HashMap<>();
        for (Limit l : limits) {
            List<Point> points = pointFactory.toPoints(l.region);
            for (Point p : points) {
                if (!parent.containsKey(p)) parent.put(p, p);
            }
            for (int i = 1; i < points.size(); i++) {
                union(parent, points.get(0), points.get(i));
            }
        }

        Map<Point, List<Point>> cellsByRoot = new HashMap<>();
        for (Point p : parent.keySet()) {
            Point root = find(parent, p);
            List<Point> list = cellsByRoot.get(root);
            if (list == null) {
                list = new ArrayList<>();
                cellsByRoot.put(root, list);
            }
            list.add(p);
        }

        Map<Point, List<Limit>> limitsByRoot = new HashMap<>();
        for (Limit l : limits) {
            List<Point> points = pointFactory.toPoints(l.region);
            if (points.isEmpty()) continue;
            Point root = find(parent, points.get(0));
            List<Limit> list = limitsByRoot.get(root);
            if (list == null) {
                list = new ArrayList<>();
                limitsByRoot.put(root, list);
            }
            list.add(l);
        }

        List<Component> result = new ArrayList<>();
        for (Map.Entry<Point, List<Point>> e : cellsByRoot.entrySet()) {
            List<Limit> ls = limitsByRoot.get(e.getKey());
            result.add(new Component(e.getValue(), ls != null ? ls : Collections.<Limit>emptyList()));
        }
        // Tackle small components first so the brute-force budget isn't dominated by
        // a single huge component that may fall back anyway.
        Collections.sort(result, new Comparator<Component>() {
            @Override public int compare(Component a, Component b) {
                return Integer.compare(a.cells.size(), b.cells.size());
            }
        });
        return result;
    }

    private static Point find(Map<Point, Point> parent, Point p) {
        Point cur = parent.get(p);
        while (!cur.equals(p)) {
            Point gp = parent.get(cur);
            parent.put(p, gp);
            p = cur;
            cur = gp;
        }
        return p;
    }

    private static void union(Map<Point, Point> parent, Point a, Point b) {
        Point ra = find(parent, a);
        Point rb = find(parent, b);
        if (!ra.equals(rb)) parent.put(ra, rb);
    }

    private ComponentResult enumerateComponent(Component c) {
        int n = c.cells.size();
        if (n == 0 || n > MAX_COMPONENT_SIZE) return null;

        Map<Point, Integer> cellIdx = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            cellIdx.put(c.cells.get(i), i);
        }

        int L = c.limits.size();
        long[] regionMasks = new long[L];
        int[] mins = new int[L];
        int[] maxs = new int[L];
        for (int i = 0; i < L; i++) {
            Limit l = c.limits.get(i);
            long mask = 0L;
            for (Point p : pointFactory.toPoints(l.region)) {
                Integer idx = cellIdx.get(p);
                if (idx != null) mask |= 1L << idx;
            }
            regionMasks[i] = mask;
            mins[i] = l.min;
            maxs[i] = l.max;
        }

        // For each cell, the limit indices that contain it -- so backtracking
        // updates only those limits when the cell is decided. Plain int[] (not
        // a long bitmask) because L can easily exceed 64 limits per component.
        int[][] cellLimits = new int[n][];
        int[] perCellLimitCount = new int[n];
        for (int i = 0; i < L; i++) {
            long m = regionMasks[i];
            while (m != 0L) {
                int j = Long.numberOfTrailingZeros(m);
                perCellLimitCount[j]++;
                m &= m - 1L;
            }
        }
        for (int j = 0; j < n; j++) {
            cellLimits[j] = new int[perCellLimitCount[j]];
            perCellLimitCount[j] = 0;
        }
        for (int i = 0; i < L; i++) {
            long m = regionMasks[i];
            while (m != 0L) {
                int j = Long.numberOfTrailingZeros(m);
                cellLimits[j][perCellLimitCount[j]++] = i;
                m &= m - 1L;
            }
        }

        BacktrackState st = new BacktrackState();
        st.n = n;
        st.bombCount = new int[L];
        st.undecided = new int[L];
        for (int i = 0; i < L; i++) {
            st.undecided[i] = Long.bitCount(regionMasks[i]);
        }
        st.mins = mins;
        st.maxs = maxs;
        st.cellLimits = cellLimits;
        st.cellBombCounts = new long[n];
        st.assignment = 0L;
        st.totalValid = 0L;

        backtrack(st, 0);
        return new ComponentResult(st.cellBombCounts, st.totalValid);
    }

    private static void backtrack(BacktrackState s, int idx) {
        if (idx == s.n) {
            s.totalValid++;
            long m = s.assignment;
            while (m != 0L) {
                int j = Long.numberOfTrailingZeros(m);
                s.cellBombCounts[j]++;
                m &= m - 1L;
            }
            return;
        }

        final int[] memb = s.cellLimits[idx];

        // Try cell = not bomb: each affected limit loses one undecided slot.
        // Feasibility fails only if any limit's remaining capacity drops below
        // its min bomb requirement.
        boolean feasible = true;
        for (int k = 0; k < memb.length; k++) {
            int l = memb[k];
            s.undecided[l]--;
            if (s.bombCount[l] + s.undecided[l] < s.mins[l]) {
                feasible = false;
            }
        }
        if (feasible) {
            backtrack(s, idx + 1);
        }
        // restore
        for (int k = 0; k < memb.length; k++) {
            s.undecided[memb[k]]++;
        }

        // Try cell = bomb: each affected limit gains a bomb and loses one
        // undecided slot. Feasibility fails if any limit's bomb count exceeds
        // its max OR its remaining capacity drops below its min.
        feasible = true;
        for (int k = 0; k < memb.length; k++) {
            int l = memb[k];
            s.bombCount[l]++;
            s.undecided[l]--;
            if (s.bombCount[l] > s.maxs[l]
                    || s.bombCount[l] + s.undecided[l] < s.mins[l]) {
                feasible = false;
            }
        }
        if (feasible) {
            s.assignment |= 1L << idx;
            backtrack(s, idx + 1);
            s.assignment &= ~(1L << idx);
        }
        // restore
        for (int k = 0; k < memb.length; k++) {
            int l = memb[k];
            s.bombCount[l]--;
            s.undecided[l]++;
        }
    }

    private static final class BacktrackState {
        int n;
        int[] bombCount;
        int[] undecided;
        int[] mins;
        int[] maxs;
        int[][] cellLimits;
        long[] cellBombCounts;
        long assignment;
        long totalValid;
    }

    private static double perLimitProbability(Limit limit) {
        // Same formula as LowestProbabilityAnalyzer's calculateProbabilityFor.
        long in = 0L, total = 0L;
        for (int i = limit.min; i <= limit.max; i++) {
            in += LowestProbabilityAnalyzer.c(limit.region.size() - 1, i - 1);
            total += LowestProbabilityAnalyzer.c(limit.region.size(), i);
        }
        return ((double) in) / total;
    }

    private static final class Component {
        final List<Point> cells;
        final List<Limit> limits;
        Component(List<Point> cells, List<Limit> limits) {
            this.cells = cells;
            this.limits = limits;
        }
    }

    private static final class ComponentResult {
        final long[] bombCounts;
        final long totalValid;
        ComponentResult(long[] bombCounts, long totalValid) {
            this.bombCounts = bombCounts;
            this.totalValid = totalValid;
        }
    }

    private static final class ProbabilityPoint implements Comparable<ProbabilityPoint> {
        final Point point;
        final double probability;
        ProbabilityPoint(Point point, double probability) {
            this.point = point;
            this.probability = probability;
        }
        @Override
        public int compareTo(ProbabilityPoint that) {
            return Double.compare(this.probability, that.probability);
        }
    }
}
