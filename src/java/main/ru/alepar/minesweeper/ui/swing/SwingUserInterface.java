package ru.alepar.minesweeper.ui.swing;

import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.ui.Difficulty;
import ru.alepar.minesweeper.ui.GameController;
import ru.alepar.minesweeper.ui.GameState;
import ru.alepar.minesweeper.ui.UserInterface;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;

/**
 * Swing implementation of {@link UserInterface}. Owns the JFrame and routes
 * mouse / button events back into a {@link GameController}. All
 * UserInterface callback methods marshal onto the EDT internally so callers
 * don't have to worry about threading.
 */
public class SwingUserInterface implements UserInterface {

    private static final Font LCD_FONT = new Font("Monospaced", Font.BOLD, 22);

    private final JFrame frame;
    private final BoardPanel boardPanel;
    private final JLabel bombsLabel;
    private final JLabel timerLabel;
    private final JLabel statusLabel;
    private final JButton newGameButton;
    private final JToggleButton autosolveToggle;
    private final JButton stepButton;
    private final JSlider speedSlider;

    private GameController controller;

    public SwingUserInterface() {
        frame = new JFrame("Minesweeper");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);

        boardPanel = new BoardPanel();
        boardPanel.setClickHandler((cell, button) -> {
            if (controller == null) return;
            if (button == MouseEvent.BUTTON1) controller.leftClick(cell);
            else if (button == MouseEvent.BUTTON3) controller.rightClick(cell);
        });

        bombsLabel = makeLcdLabel("000");
        timerLabel = makeLcdLabel("000");
        statusLabel = new JLabel("Playing", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        newGameButton = new JButton("New");
        newGameButton.addActionListener(e -> { if (controller != null) controller.restart(); });

        autosolveToggle = new JToggleButton("Autosolve");
        autosolveToggle.addActionListener(e -> { if (controller != null) controller.toggleAutosolve(); });

        stepButton = new JButton("Step");
        stepButton.addActionListener(e -> { if (controller != null) controller.stepOnce(); });

        speedSlider = new JSlider(JSlider.HORIZONTAL, 10, 1000, 200);
        speedSlider.setMajorTickSpacing(250);
        speedSlider.setPaintTicks(true);
        speedSlider.setInverted(true);  // left = slower, right = faster intuition; inverted because lower delay = faster
        speedSlider.addChangeListener(e -> {
            if (controller != null) controller.setAutosolveSpeed(speedSlider.getValue());
        });

        frame.setJMenuBar(buildMenuBar());
        frame.add(buildStatusBar(), BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(buildControlBar(), BorderLayout.SOUTH);
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public void show() {
        SwingUtilities.invokeLater(() -> {
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // ---- UserInterface ----------------------------------------------------

    @Override
    public void onNewGame(FieldState initialField, int totalBombs) {
        SwingUtilities.invokeLater(() -> {
            boardPanel.setField(initialField);
            bombsLabel.setText(formatLcd(totalBombs));
            timerLabel.setText("000");
            statusLabel.setText("Playing");
            statusLabel.setForeground(Color.BLACK);
            autosolveToggle.setSelected(false);
            autosolveToggle.setText("Autosolve");
            frame.pack();
        });
    }

    @Override
    public void onFieldUpdated(FieldState field) {
        SwingUtilities.invokeLater(() -> boardPanel.setField(field));
    }

    @Override
    public void onBombsLeftChanged(int bombsLeft) {
        SwingUtilities.invokeLater(() -> bombsLabel.setText(formatLcd(bombsLeft)));
    }

    @Override
    public void onGameStateChanged(GameState state) {
        SwingUtilities.invokeLater(() -> {
            switch (state) {
                case IN_PROGRESS:
                    statusLabel.setText("Playing");
                    statusLabel.setForeground(Color.BLACK);
                    break;
                case WON:
                    statusLabel.setText("WON");
                    statusLabel.setForeground(new Color(0, 130, 0));
                    break;
                case LOST:
                    statusLabel.setText("LOST");
                    statusLabel.setForeground(new Color(180, 0, 0));
                    break;
            }
        });
    }

    @Override
    public void onTimerTick(int elapsedSeconds) {
        SwingUtilities.invokeLater(() -> timerLabel.setText(formatLcd(elapsedSeconds)));
    }

    @Override
    public void onAutosolveStateChanged(boolean running) {
        SwingUtilities.invokeLater(() -> {
            autosolveToggle.setSelected(running);
            autosolveToggle.setText(running ? "Stop" : "Autosolve");
        });
    }

    // ---- helpers ----------------------------------------------------------

    private JLabel makeLcdLabel(String initial) {
        JLabel l = new JLabel(initial, SwingConstants.CENTER);
        l.setFont(LCD_FONT);
        l.setOpaque(true);
        l.setBackground(Color.BLACK);
        l.setForeground(new Color(255, 60, 60));
        l.setPreferredSize(new Dimension(56, 30));
        l.setBorder(BorderFactory.createLoweredBevelBorder());
        return l;
    }

    private static String formatLcd(int value) {
        int clamped = Math.max(-99, Math.min(999, value));
        if (clamped < 0) return String.format("-%02d", -clamped);
        return String.format("%03d", clamped);
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        panel.add(bombsLabel, BorderLayout.WEST);

        JPanel centerWrap = new JPanel();
        centerWrap.setLayout(new BoxLayout(centerWrap, BoxLayout.Y_AXIS));
        statusLabel.setAlignmentX(0.5f);
        newGameButton.setAlignmentX(0.5f);
        centerWrap.add(Box.createVerticalStrut(2));
        centerWrap.add(statusLabel);
        centerWrap.add(Box.createVerticalStrut(2));
        centerWrap.add(newGameButton);
        panel.add(centerWrap, BorderLayout.CENTER);

        panel.add(timerLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildControlBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panel.add(autosolveToggle);
        panel.add(stepButton);
        panel.add(new JLabel("Slow"));
        panel.add(speedSlider);
        panel.add(new JLabel("Fast"));
        return panel;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");
        JMenuItem restart = new JMenuItem("New (same difficulty)");
        restart.addActionListener(e -> { if (controller != null) controller.restart(); });
        gameMenu.add(restart);
        gameMenu.addSeparator();

        gameMenu.add(difficultyItem("Beginner (9x9, 10 bombs)", Difficulty.BEGINNER));
        gameMenu.add(difficultyItem("Intermediate (16x16, 40 bombs)", Difficulty.INTERMEDIATE));
        gameMenu.add(difficultyItem("Expert (30x16, 99 bombs)", Difficulty.EXPERT));

        gameMenu.addSeparator();
        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(e -> frame.dispose());
        gameMenu.add(quit);

        mb.add(gameMenu);
        return mb;
    }

    private JMenuItem difficultyItem(String text, Difficulty d) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> { if (controller != null) controller.newGame(d); });
        return item;
    }
}
