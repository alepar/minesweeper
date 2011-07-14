package ru.alepar.minesweeper.analyzer;

import org.omg.PortableServer.portable.Delegate;

import java.util.Set;

public class CountingLimitShuffler implements LimitShuffler {

    private final LimitShuffler delegate;
    private int count;

    public CountingLimitShuffler(LimitShuffler delegate) {
        this.delegate = delegate;
    }

    @Override
    public Set<Limit> shuffleLimitsPair(Limit first, Limit second) {
        count++;
        return delegate.shuffleLimitsPair(first, second);
    }

    public int getCount() {
        return count;
    }
}
