package ru.alepar.minesweeper.ui.swing;

import ru.alepar.minesweeper.ui.Difficulty;
import ru.alepar.minesweeper.ui.GameController;

import javax.swing.SwingUtilities;

public final class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingUserInterface ui = new SwingUserInterface();
            GameController controller = new GameController(ui, Difficulty.INTERMEDIATE, System.nanoTime());
            ui.setController(controller);
            ui.show();
        });
    }

    private App() {}
}
