package ru.alepar.minesweeper.ui.swing;

import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom-painted Minesweeper grid. Renders closed / opened / flagged cells
 * and dispatches mouse clicks (cell coords + button code) to a handler.
 */
public class BoardPanel extends JPanel {

    public interface ClickHandler {
        void onClick(Point cell, int mouseButton);
    }

    public static final int CELL_SIZE = 22;

    private static final Color CLOSED_BG = new Color(192, 192, 192);
    private static final Color OPENED_BG = new Color(220, 220, 220);
    private static final Color GRID_LINE = new Color(160, 160, 160);
    private static final Color HIGHLIGHT = Color.WHITE;
    private static final Color SHADOW = new Color(128, 128, 128);
    private static final Color BOMB_COLOR = Color.BLACK;
    private static final Color FLAG_RED = new Color(200, 0, 0);
    private static final Font NUMBER_FONT = new Font("SansSerif", Font.BOLD, 14);

    private final Map<Integer, Color> numberColors = new HashMap<>();
    private FieldState field;
    private ClickHandler clickHandler;

    public BoardPanel() {
        numberColors.put(1, new Color(0, 0, 200));
        numberColors.put(2, new Color(0, 120, 0));
        numberColors.put(3, new Color(200, 0, 0));
        numberColors.put(4, new Color(0, 0, 120));
        numberColors.put(5, new Color(120, 0, 0));
        numberColors.put(6, new Color(0, 120, 120));
        numberColors.put(7, new Color(0, 0, 0));
        numberColors.put(8, new Color(100, 100, 100));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e);
            }
        });
    }

    public void setClickHandler(ClickHandler h) {
        this.clickHandler = h;
    }

    public void setField(FieldState f) {
        this.field = f;
        if (f != null) {
            setPreferredSize(new Dimension(f.width() * CELL_SIZE, f.height() * CELL_SIZE));
            revalidate();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (field == null) return;
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            for (int y = 0; y < field.height(); y++) {
                for (int x = 0; x < field.width(); x++) {
                    paintCell(g, x, y, field.cellAt(new Point(x, y)));
                }
            }
        } finally {
            g.dispose();
        }
    }

    private void paintCell(Graphics2D g, int x, int y, Cell c) {
        int px = x * CELL_SIZE;
        int py = y * CELL_SIZE;

        if (c == Cell.CLOSED) {
            g.setColor(CLOSED_BG);
            g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
            // Raised bevel.
            g.setColor(HIGHLIGHT);
            g.drawLine(px, py, px + CELL_SIZE - 2, py);
            g.drawLine(px, py, px, py + CELL_SIZE - 2);
            g.setColor(SHADOW);
            g.drawLine(px + CELL_SIZE - 1, py, px + CELL_SIZE - 1, py + CELL_SIZE - 1);
            g.drawLine(px, py + CELL_SIZE - 1, px + CELL_SIZE - 1, py + CELL_SIZE - 1);
        } else if (c == Cell.BOMB) {
            // Flagged or revealed-bomb (post game-over): same closed-bevel base with
            // a red triangle flag glyph and a small black bomb dot at the center.
            g.setColor(CLOSED_BG);
            g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
            g.setColor(HIGHLIGHT);
            g.drawLine(px, py, px + CELL_SIZE - 2, py);
            g.drawLine(px, py, px, py + CELL_SIZE - 2);
            g.setColor(SHADOW);
            g.drawLine(px + CELL_SIZE - 1, py, px + CELL_SIZE - 1, py + CELL_SIZE - 1);
            g.drawLine(px, py + CELL_SIZE - 1, px + CELL_SIZE - 1, py + CELL_SIZE - 1);
            // Flag pole and triangle.
            int cx = px + CELL_SIZE / 2;
            int cy = py + CELL_SIZE / 2;
            g.setColor(BOMB_COLOR);
            g.drawLine(cx, cy - 6, cx, cy + 6);
            g.fillRect(cx - 4, cy + 5, 9, 2);
            g.setColor(FLAG_RED);
            int[] xs = {cx, cx, cx - 6};
            int[] ys = {cy - 7, cy - 1, cy - 4};
            g.fillPolygon(xs, ys, 3);
        } else {
            // Opened number.
            g.setColor(OPENED_BG);
            g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
            g.setColor(GRID_LINE);
            g.drawRect(px, py, CELL_SIZE - 1, CELL_SIZE - 1);
            int v = c.value;
            if (v > 0) {
                Color color = numberColors.get(v);
                if (color == null) color = Color.BLACK;
                g.setColor(color);
                g.setFont(NUMBER_FONT);
                FontMetrics fm = g.getFontMetrics();
                String s = String.valueOf(v);
                int tx = px + (CELL_SIZE - fm.stringWidth(s)) / 2;
                int ty = py + (CELL_SIZE + fm.getAscent()) / 2 - 2;
                g.drawString(s, tx, ty);
            }
        }
    }

    private void handleClick(MouseEvent e) {
        if (field == null || clickHandler == null) return;
        int cx = e.getX() / CELL_SIZE;
        int cy = e.getY() / CELL_SIZE;
        if (cx < 0 || cx >= field.width() || cy < 0 || cy >= field.height()) return;
        clickHandler.onClick(new Point(cx, cy), e.getButton());
    }
}
