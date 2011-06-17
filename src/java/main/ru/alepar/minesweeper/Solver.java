package ru.alepar.minesweeper;

import ru.alepar.minesweeper.analyzer.MinMaxConfidentAnalyzer;
import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;

public class Solver {

    private final FieldApi fieldApi;
    private MinMaxConfidentAnalyzer minMaxAnalyzer;

    public Solver(FieldApi fieldApi) {
        this.fieldApi = fieldApi;
        minMaxAnalyzer = new MinMaxConfidentAnalyzer(fieldApi);
    }

    public FieldState solve() {
        FieldState last;
        FieldState current = fieldApi.getCurrentField();
        do {
            last = current;
            current = minMaxAnalyzer.solve();
        } while(!last.equals(current));
        return current;
    }

}
