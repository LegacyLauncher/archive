package ru.turikhay.util;

public enum Direction {
    TOP_LEFT,
    TOP,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM,
    BOTTOM_RIGHT;

    private final String lower = name().toLowerCase(java.util.Locale.ROOT);

    public String toString() {
        return lower;
    }
}
