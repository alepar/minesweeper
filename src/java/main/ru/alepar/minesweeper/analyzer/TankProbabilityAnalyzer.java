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
 * Computes per-cell bomb probabilities via enumeration over connected border
 * components ("tank" solver), then couples them with each other and with the
 * unconstrained closed cells through the global bombsLeft constraint by
 * convolving each component's bomb-count distribution with the binomial
 * (1+x)^|unconstrained| and reading off coefficient [x^bombsLeft] of the joint
 * polynomial.
 *
 * <p>Components are formed by union-find on limit co-occurrence: two cells
 * sharing any limit's region are in the same component. Each component up to
 * {@link #MAX_COMPONENT_SIZE} cells is enumerated by backtracking with
 * constraint propagation; we tally placements bucketed by bombs-in-component
 * so the per-component bomb-count distribution becomes the polynomial that
 * couples with the rest of the board.</p>
 *
 * <p>Per-component analysis also yields exact tank-derived certainties:
 * a cell that is a bomb in 0 valid placements of its component is globally
 * safe; one that is a bomb in every valid placement is globally a bomb.
 * Those are exposed via {@link Analysis#certainSafe}/{@link Analysis#certainBomb}
 * so callers can short-circuit guessing when local enumeration uncovers
 * forced moves that MinMax propagation didn't.</p>
 */
public class TankProbabilityAnalyzer implements GuessingAnalyzer {

    // Backtracking packs the per-cell assignment into a single long, so 64
    // cells per component is the hard ceiling. Larger components fall back
    // to per-limit max-aggregation with no global coupling.
    private static final int MAX_COMPONENT_SIZE = 64;

    // Floating-point precision floor when reading probabilities off the
    // joint polynomial. Anything smaller is treated as 0.
    private static final double EPS = 1e-12;

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
        return analyze().pickLowestProbability();
    }

    public Analysis analyze() {
        final Region closedCells = pointFactory.closedCellsOn(currentField);
        if (closedCells.isEmpty()) {
            throw new RuntimeException("there are no closed cells to choose from");
        }

        final MinMaxAnalyzer minMax = new MinMaxAnalyzer(
                pointFactory, currentField, new SubtractIntersectLimitShuffler(), writer);
        final Collection<Limit> limits = minMax.shuffledLimits();

        // Constrained = cells touched by any limit; everything else closed
        // (excluding already-marked bombs) is unconstrained.
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

        // Enumerate each component into its per-bomb-count distribution.
        // Per-component certainties (P=0/P=1 within the component) are exact
        // even before we touch the joint coupling, so we extract them up front.
        final List<ComponentDist> dists = new ArrayList<>(components.size());
        final Set<Point> certainSafe = new HashSet<>();
        final Set<Point> certainBomb = new HashSet<>();
        boolean allFit = true;
        for (Component c : components) {
            ComponentDist d = enumerateComponent(c);
            dists.add(d);
            if (d == null) {
                allFit = false;
                continue;
            }
            extractComponentCertainties(c, d, certainSafe, certainBomb);
        }

        Map<Point, Double> probability;
        double unconstrainedProb;
        if (allFit && !components.isEmpty()) {
            JointResult jr = computeJointProbabilities(dists, unconstrained.size());
            probability = jr.probability;
            unconstrainedProb = jr.unconstrainedProb;
        } else {
            FallbackResult fr = computeFallbackProbabilities(components, dists, unconstrained.size());
            probability = fr.probability;
            unconstrainedProb = fr.unconstrainedProb;
        }

        return new Analysis(probability, unconstrained, unconstrainedProb, certainSafe, certainBomb);
    }

    private static void extractComponentCertainties(Component c, ComponentDist d,
                                                    Set<Point> certainSafe, Set<Point> certainBomb) {
        long totalValid = 0L;
        for (long t : d.totalByK) totalValid += t;
        if (totalValid == 0L) return;
        for (int j = 0; j < c.cells.size(); j++) {
            long bomb = 0L;
            for (int k = 0; k < d.cellBombByK.length; k++) {
                bomb += d.cellBombByK[k][j];
            }
            if (bomb == 0L) {
                certainSafe.add(c.cells.get(j));
            } else if (bomb == totalValid) {
                certainBomb.add(c.cells.get(j));
            }
        }
    }

    // Joint convolution: P_c(x) = sum_k count_k * x^k for each component c,
    // I(x) = (1+x)^|inner|; total partition Z = [x^bombsLeft] (prod_c P_c(x)) * I(x).
    // For a cell in component c, P(cell=bomb) =
    //   [x^bombsLeft] (Pcell_c(x) * prod_{c'!=c} P_{c'}(x) * I(x)) / Z
    // where Pcell_c is the same polynomial restricted to placements that have
    // this cell as a bomb. For an unconstrained cell, swap I for x*(1+x)^|inner-1|.
    private JointResult computeJointProbabilities(List<ComponentDist> dists, int innerSize) {
        int maxDeg = bombsLeft;

        int C = dists.size();
        double[][] compPoly = new double[C][];
        for (int i = 0; i < C; i++) {
            ComponentDist d = dists.get(i);
            int deg = Math.min(d.totalByK.length - 1, maxDeg);
            double[] p = new double[deg + 1];
            for (int k = 0; k <= deg; k++) {
                p[k] = (double) d.totalByK[k];
            }
            compPoly[i] = p;
        }

        double[] innerPoly = binomialPoly(innerSize, maxDeg);

        // Joint over all components only (no inner). We'll fold inner in later.
        double[] jointComp = new double[]{1.0};
        for (double[] p : compPoly) {
            jointComp = polyMultiply(jointComp, p, maxDeg);
        }

        double[] jointFull = polyMultiply(jointComp, innerPoly, maxDeg);
        double Z = bombsLeft < jointFull.length ? jointFull[bombsLeft] : 0.0;

        Map<Point, Double> probability = new HashMap<>();
        double unconstrainedProb = 0.0;

        if (Z <= EPS) {
            // Inconsistent global constraints -- shouldn't happen on a valid
            // game. Fall back to per-component independent marginals to avoid
            // dividing by zero.
            return new JointResult(perComponentMarginals(dists), 0.0);
        }

        // Per-component complement = product of all OTHER comp polys * innerPoly.
        // Simple O(C^2) build; C is small (handful of components per game).
        double[][] complement = new double[C][];
        for (int i = 0; i < C; i++) {
            double[] cmp = new double[]{1.0};
            for (int j = 0; j < C; j++) {
                if (j != i) cmp = polyMultiply(cmp, compPoly[j], maxDeg);
            }
            complement[i] = polyMultiply(cmp, innerPoly, maxDeg);
        }

        for (int i = 0; i < C; i++) {
            ComponentDist d = dists.get(i);
            double[] cmp = complement[i];
            int kMax = Math.min(d.cellBombByK.length - 1, maxDeg);
            for (int cellLocal = 0; cellLocal < d.cells.size(); cellLocal++) {
                double num = 0.0;
                for (int k = 0; k <= kMax; k++) {
                    int rem = bombsLeft - k;
                    if (rem >= 0 && rem < cmp.length) {
                        num += d.cellBombByK[k][cellLocal] * cmp[rem];
                    }
                }
                probability.put(d.cells.get(cellLocal), num / Z);
            }
        }

        if (innerSize > 0) {
            // I'(x) = x * (1+x)^(innerSize-1); coefficient at m is C(innerSize-1, m-1).
            double[] innerWithBomb = binomialPolyShifted(innerSize, maxDeg);
            double[] jointInnerBomb = polyMultiply(jointComp, innerWithBomb, maxDeg);
            double innerNum = bombsLeft < jointInnerBomb.length ? jointInnerBomb[bombsLeft] : 0.0;
            unconstrainedProb = innerNum / Z;
        }

        return new JointResult(probability, unconstrainedProb);
    }

    // Used when at least one component overflows MAX_COMPONENT_SIZE. Each
    // component contributes per-cell ratios independently (no joint coupling);
    // oversized ones revert to the old per-limit max-aggregation heuristic.
    private FallbackResult computeFallbackProbabilities(List<Component> components,
                                                        List<ComponentDist> dists,
                                                        int innerSize) {
        Map<Point, Double> probability = new HashMap<>();
        double expectedBorderBombs = 0.0;
        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);
            ComponentDist d = dists.get(i);
            if (d != null) {
                long totalValid = 0L;
                for (long t : d.totalByK) totalValid += t;
                if (totalValid == 0L) continue;
                for (int j = 0; j < c.cells.size(); j++) {
                    long bomb = 0L;
                    for (int k = 0; k < d.cellBombByK.length; k++) {
                        bomb += d.cellBombByK[k][j];
                    }
                    double p = (double) bomb / totalValid;
                    probability.put(c.cells.get(j), p);
                    expectedBorderBombs += p;
                }
            } else {
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
        double unconstrainedProb = 0.0;
        if (innerSize > 0) {
            double remaining = Math.max(0.0, bombsLeft - expectedBorderBombs);
            unconstrainedProb = remaining / innerSize;
        }
        return new FallbackResult(probability, unconstrainedProb);
    }

    private Map<Point, Double> perComponentMarginals(List<ComponentDist> dists) {
        Map<Point, Double> probability = new HashMap<>();
        for (ComponentDist d : dists) {
            if (d == null) continue;
            long totalValid = 0L;
            for (long t : d.totalByK) totalValid += t;
            if (totalValid == 0L) continue;
            for (int j = 0; j < d.cells.size(); j++) {
                long bomb = 0L;
                for (int k = 0; k < d.cellBombByK.length; k++) {
                    bomb += d.cellBombByK[k][j];
                }
                probability.put(d.cells.get(j), (double) bomb / totalValid);
            }
        }
        return probability;
    }

    private List<Component> findComponents(Collection<Limit> limits) {
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

    private ComponentDist enumerateComponent(Component c) {
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
        st.totalByK = new long[n + 1];
        st.cellBombByK = new long[n + 1][n];
        st.assignment = 0L;
        st.bombsInComponent = 0;

        backtrack(st, 0);
        return new ComponentDist(c.cells, st.totalByK, st.cellBombByK);
    }

    private static void backtrack(BacktrackState s, int idx) {
        if (idx == s.n) {
            int k = s.bombsInComponent;
            s.totalByK[k]++;
            long m = s.assignment;
            while (m != 0L) {
                int j = Long.numberOfTrailingZeros(m);
                s.cellBombByK[k][j]++;
                m &= m - 1L;
            }
            return;
        }

        final int[] memb = s.cellLimits[idx];

        // Try cell = not bomb.
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
        for (int k = 0; k < memb.length; k++) {
            s.undecided[memb[k]]++;
        }

        // Try cell = bomb.
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
            s.bombsInComponent++;
            backtrack(s, idx + 1);
            s.bombsInComponent--;
            s.assignment &= ~(1L << idx);
        }
        for (int k = 0; k < memb.length; k++) {
            int l = memb[k];
            s.bombCount[l]--;
            s.undecided[l]++;
        }
    }

    private static double perLimitProbability(Limit limit) {
        long in = 0L, total = 0L;
        for (int i = limit.min; i <= limit.max; i++) {
            in += LowestProbabilityAnalyzer.c(limit.region.size() - 1, i - 1);
            total += LowestProbabilityAnalyzer.c(limit.region.size(), i);
        }
        return ((double) in) / total;
    }

    static double[] polyMultiply(double[] a, double[] b, int maxDeg) {
        int outLen = Math.min(a.length + b.length - 1, maxDeg + 1);
        double[] r = new double[outLen];
        for (int i = 0; i < a.length && i < outLen; i++) {
            double ai = a[i];
            if (ai == 0.0) continue;
            int jLimit = Math.min(b.length, outLen - i);
            for (int j = 0; j < jLimit; j++) {
                r[i + j] += ai * b[j];
            }
        }
        return r;
    }

    // (1+x)^n truncated at degree maxDeg: r[k] = C(n, k).
    static double[] binomialPoly(int n, int maxDeg) {
        int d = Math.min(n, maxDeg);
        double[] r = new double[d + 1];
        r[0] = 1.0;
        for (int i = 1; i <= d; i++) {
            r[i] = r[i - 1] * (n - i + 1) / i;
        }
        return r;
    }

    // x * (1+x)^(n-1) truncated at maxDeg: r[m] = C(n-1, m-1) for m >= 1.
    static double[] binomialPolyShifted(int n, int maxDeg) {
        if (n <= 0) return new double[]{0.0};
        int d = Math.min(n, maxDeg);
        double[] r = new double[d + 1];
        if (d < 1) return r;
        r[1] = 1.0;
        for (int m = 2; m <= d; m++) {
            r[m] = r[m - 1] * (n - m + 1) / (m - 1);
        }
        return r;
    }

    private static final class BacktrackState {
        int n;
        int[] bombCount;
        int[] undecided;
        int[] mins;
        int[] maxs;
        int[][] cellLimits;
        long[] totalByK;
        long[][] cellBombByK;
        long assignment;
        int bombsInComponent;
    }

    private static final class Component {
        final List<Point> cells;
        final List<Limit> limits;
        Component(List<Point> cells, List<Limit> limits) {
            this.cells = cells;
            this.limits = limits;
        }
    }

    private static final class ComponentDist {
        final List<Point> cells;
        final long[] totalByK;
        final long[][] cellBombByK;
        ComponentDist(List<Point> cells, long[] totalByK, long[][] cellBombByK) {
            this.cells = cells;
            this.totalByK = totalByK;
            this.cellBombByK = cellBombByK;
        }
    }

    private static final class JointResult {
        final Map<Point, Double> probability;
        final double unconstrainedProb;
        JointResult(Map<Point, Double> probability, double unconstrainedProb) {
            this.probability = probability;
            this.unconstrainedProb = unconstrainedProb;
        }
    }

    private static final class FallbackResult {
        final Map<Point, Double> probability;
        final double unconstrainedProb;
        FallbackResult(Map<Point, Double> probability, double unconstrainedProb) {
            this.probability = probability;
            this.unconstrainedProb = unconstrainedProb;
        }
    }

    public static final class Analysis {
        public final Map<Point, Double> borderProbability;
        public final Set<Point> unconstrained;
        public final double unconstrainedProbability;
        public final Set<Point> certainSafe;
        public final Set<Point> certainBomb;

        Analysis(Map<Point, Double> borderProbability,
                 Set<Point> unconstrained,
                 double unconstrainedProbability,
                 Set<Point> certainSafe,
                 Set<Point> certainBomb) {
            this.borderProbability = borderProbability;
            this.unconstrained = unconstrained;
            this.unconstrainedProbability = unconstrainedProbability;
            this.certainSafe = certainSafe;
            this.certainBomb = certainBomb;
        }

        public boolean hasCertainties() {
            return !certainSafe.isEmpty() || !certainBomb.isEmpty();
        }

        public ConfidentAnalyzer.Result certaintiesAsResult(PointFactory pointFactory) {
            Region toOpen = pointFactory.emptyRegion();
            Region toMark = pointFactory.emptyRegion();
            int width = pointFactory.width();
            for (Point p : certainSafe) {
                toOpen.set(p.y * width + p.x);
            }
            for (Point p : certainBomb) {
                toMark.set(p.y * width + p.x);
            }
            return new ConfidentAnalyzer.Result(toMark, toOpen);
        }

        public Point pickLowestProbability() {
            SortedSet<ProbabilityPoint> guessing = new TreeSet<>();
            for (Map.Entry<Point, Double> e : borderProbability.entrySet()) {
                guessing.add(new ProbabilityPoint(e.getKey(), e.getValue()));
            }
            if (!unconstrained.isEmpty()) {
                for (Point p : unconstrained) {
                    guessing.add(new ProbabilityPoint(p, unconstrainedProbability));
                }
            }
            if (guessing.isEmpty()) {
                throw new RuntimeException("no candidate cells to guess from");
            }
            return guessing.iterator().next().point;
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
