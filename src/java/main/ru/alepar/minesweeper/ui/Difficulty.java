package ru.alepar.minesweeper.ui;

public final class Difficulty {

    public static final Difficulty BEGINNER = new Difficulty("Beginner", 9, 9, 10);
    public static final Difficulty INTERMEDIATE = new Difficulty("Intermediate", 16, 16, 40);
    public static final Difficulty EXPERT = new Difficulty("Expert", 30, 16, 99);

    public final String name;
    public final int width;
    public final int height;
    public final int bombs;

    public Difficulty(String name, int width, int height, int bombs) {
        if (bombs < 0 || bombs >= width * height) {
            throw new IllegalArgumentException("bombs out of range: " + bombs);
        }
        this.name = name;
        this.width = width;
        this.height = height;
        this.bombs = bombs;
    }

    @Override public String toString() { return name; }
}
