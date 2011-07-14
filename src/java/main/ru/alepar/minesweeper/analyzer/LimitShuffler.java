package ru.alepar.minesweeper.analyzer;

import java.util.Set;

public interface LimitShuffler {
    Set<Limit> shuffleLimitsPair(Limit first, Limit second);
}
