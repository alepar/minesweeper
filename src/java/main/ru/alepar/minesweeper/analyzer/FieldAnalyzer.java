package ru.alepar.minesweeper.analyzer;

import ru.alepar.minesweeper.model.FieldState;

public interface FieldAnalyzer {
    FieldState solve();
}
