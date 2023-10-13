package net.legacylauncher.ui.converter;

import net.legacylauncher.ui.loc.LocalizableStringConverter;
import net.legacylauncher.util.Direction;

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
