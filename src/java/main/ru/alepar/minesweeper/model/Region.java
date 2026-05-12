package ru.alepar.minesweeper.model;

import java.util.Arrays;

public final class Region {

    private final int bits;
    private final long[] words;

    private int sizeCache = -1;
    private int hashCache;

    public Region(int length) {
        this.bits = length;
        this.words = new long[(length + 63) >>> 6];
    }

    private Region(int bits, long[] words) {
        this.bits = bits;
        this.words = words;
    }

    public int size() {
        int cached = sizeCache;
        if (cached >= 0) {
            return cached;
        }
        int s = 0;
        for (long w : words) {
            s += Long.bitCount(w);
        }
        sizeCache = s;
        return s;
    }

    public Region intersect(Region that) {
        long[] out = new long[words.length];
        for (int i = 0; i < words.length; i++) {
            out[i] = words[i] & that.words[i];
        }
        return new Region(bits, out);
    }

    public Region subtract(Region that) {
        long[] out = new long[words.length];
        for (int i = 0; i < words.length; i++) {
            out[i] = words[i] & ~that.words[i];
        }
        return new Region(bits, out);
    }

    // that ⊆ this  iff  that & ~this == 0
    public boolean contains(Region that) {
        for (int i = 0; i < words.length; i++) {
            if ((that.words[i] & ~words[i]) != 0L) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Region clone() {
        return new Region(bits, Arrays.copyOf(words, words.length));
    }

    public void or(Region that) {
        for (int i = 0; i < words.length; i++) {
            words[i] |= that.words[i];
        }
        sizeCache = -1;
        hashCache = 0;
    }

    public boolean get(int i) {
        return (words[i >>> 6] & (1L << (i & 63))) != 0L;
    }

    public void set(int i) {
        long mask = 1L << (i & 63);
        int w = i >>> 6;
        if ((words[w] & mask) == 0L) {
            words[w] |= mask;
            sizeCache = -1;
            hashCache = 0;
        }
    }

    public boolean isEmpty() {
        for (long w : words) {
            if (w != 0L) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Region that = (Region) o;
        return bits == that.bits && Arrays.equals(words, that.words);
    }

    @Override
    public int hashCode() {
        int h = hashCache;
        if (h != 0) {
            return h;
        }
        h = Arrays.hashCode(words);
        if (h == 0) {
            h = 1;
        }
        hashCache = h;
        return h;
    }
}
