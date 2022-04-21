package ru.turikhay.tlauncher.ui.converter;

import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;
import ru.turikhay.util.Direction;

public class DirectionConverter extends LocalizableStringConverter<Direction> {
    public DirectionConverter() {
        super("settings.direction");
    }

    public Direction fromString(String from) {
        return Direction.parse(from);
    }

    public String toValue(Direction from) {
        return from == null ? null : from.toString().toLowerCase(java.util.Locale.ROOT);
    }

    public Class<Direction> getObjectClass() {
        return Direction.class;
    }

    protected String toPath(Direction from) {
        return toValue(from);
    }
}
