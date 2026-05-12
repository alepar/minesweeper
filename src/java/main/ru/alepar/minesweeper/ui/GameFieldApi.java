package ru.alepar.minesweeper.ui;

import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.SteppedOnABomb;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * FieldApi for an in-progress game. Wraps a "truth" board (with bomb
 * placements) and a "visible" board (what the player sees). The solver and
 * the UI both call through this API, so an autosolve click and a human click
 * go through the same code path -- including cascade open on 0-cells, which
 * SimpleFieldApi does not do.
 *
 * <p>Marked-bomb cells (Cell.BOMB on the visible board) come from two
 * sources: the player flagging via right-click, and tank-derived deductions
 * via the solver's markBomb call. Both produce the same on-screen flag.</p>
 */
public class GameFieldApi implements FieldApi {

    private final ArrayFieldState truth;
    private ArrayFieldState visible;
    private final int totalBombs;

    public GameFieldApi(ArrayFieldState truth) {
        this.truth = truth;
        this.visible = allClosed(truth.width(), truth.height());
        this.totalBombs = countBombs(truth);
    }

    @Override
    public FieldState getCurrentField() {
        return visible;
    }

    @Override
    public void open(Point p) throws SteppedOnABomb {
        if (!inBounds(p)) return;
        Cell vc = visible.cellAt(p);
        if (vc.isOpened()) return;
        if (vc == Cell.BOMB) return;          // flagged; click is ignored in classic winmine
        if (truth.cellAt(p) == Cell.BOMB) {
            throw new SteppedOnABomb(p);
        }
        cascade(p);
    }

    @Override
    public void markBomb(Point p) {
        if (!inBounds(p)) return;
        if (visible.cellAt(p).isOpened()) {
            throw new RuntimeException("cannot flag already-open cell at " + p);
        }
        if (visible.cellAt(p) != Cell.BOMB) {
            visible = visible.mutate(p, Cell.BOMB);
        }
    }

    /** Right-click on a flagged cell un-flags it. Not on FieldApi -- UI only. */
    public void unmarkBomb(Point p) {
        if (!inBounds(p)) return;
        if (visible.cellAt(p) == Cell.BOMB) {
            visible = visible.mutate(p, Cell.CLOSED);
        }
    }

    @Override
    public int bombsLeft() {
        // Same semantics as SimpleFieldApi: closed-in-visible-and-bomb-in-truth.
        // i.e. real remaining unmarked bombs, what the solver needs.
        int n = 0;
        for (int y = 0; y < truth.height(); y++) {
            for (int x = 0; x < truth.width(); x++) {
                Point p = new Point(x, y);
                if (visible.cellAt(p) == Cell.CLOSED && truth.cellAt(p) == Cell.BOMB) {
                    n++;
                }
            }
        }
        return n;
    }

    public int totalBombs() { return totalBombs; }
    public int width() { return truth.width(); }
    public int height() { return truth.height(); }

    /** Number of cells currently displaying as a bomb/flag, regardless of truth. */
    public int flaggedCount() {
        int n = 0;
        for (int y = 0; y < visible.height(); y++) {
            for (int x = 0; x < visible.width(); x++) {
                if (visible.cellAt(new Point(x, y)) == Cell.BOMB) n++;
            }
        }
        return n;
    }

    public boolean allNonBombsOpened() {
        int opened = 0;
        for (int y = 0; y < visible.height(); y++) {
            for (int x = 0; x < visible.width(); x++) {
                if (visible.cellAt(new Point(x, y)).isOpened()) opened++;
            }
        }
        return opened == width() * height() - totalBombs;
    }

    /** On game over: reveal every bomb truth-position that the player hasn't flagged. */
    public void revealAllBombs() {
        for (int y = 0; y < truth.height(); y++) {
            for (int x = 0; x < truth.width(); x++) {
                Point p = new Point(x, y);
                if (truth.cellAt(p) == Cell.BOMB && !visible.cellAt(p).isOpened()
                        && visible.cellAt(p) != Cell.BOMB) {
                    visible = visible.mutate(p, Cell.BOMB);
                }
            }
        }
    }

    private void cascade(Point start) {
        Deque<Point> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Point cur = queue.poll();
            if (visible.cellAt(cur).isOpened()) continue;
            if (visible.cellAt(cur) == Cell.BOMB) continue;
            Cell tc = truth.cellAt(cur);
            if (tc == Cell.BOMB) continue;        // safety; not reached on initial click

            visible = visible.mutate(cur, tc);

            if (tc.value == 0) {
                int x = cur.x, y = cur.y;
                int w = width(), h = height();
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                            queue.add(new Point(nx, ny));
                        }
                    }
                }
            }
        }
    }

    private boolean inBounds(Point p) {
        return p.x >= 0 && p.x < width() && p.y >= 0 && p.y < height();
    }

    private static ArrayFieldState allClosed(int w, int h) {
        Cell[][] cells = new Cell[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) cells[y][x] = Cell.CLOSED;
        }
        return new ArrayFieldState(cells);
    }

    private static int countBombs(ArrayFieldState board) {
        int n = 0;
        for (int y = 0; y < board.height(); y++) {
            for (int x = 0; x < board.width(); x++) {
                if (board.cellAt(new Point(x, y)) == Cell.BOMB) n++;
            }
        }
        return n;
    }
}
