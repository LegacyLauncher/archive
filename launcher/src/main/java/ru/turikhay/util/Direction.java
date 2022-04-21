package ru.turikhay.util;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

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

    private static final Collection<Direction> VALUES = Arrays.asList(values());

    @Nullable
    public static Direction parse(String name) {
        return VALUES.stream().filter(e -> e.name().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
