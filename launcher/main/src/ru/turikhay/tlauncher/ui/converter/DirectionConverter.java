package ru.turikhay.tlauncher.ui.converter;

import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;
import ru.turikhay.util.Direction;
import ru.turikhay.util.Reflect;

public class DirectionConverter extends LocalizableStringConverter<Direction> {
    public DirectionConverter() {
        super("settings.direction");
    }

    public Direction fromString(String from) {
        return Reflect.parseEnum(Direction.class, from);
    }

    public String toValue(Direction from) {
        return from == null ? null : from.toString().toLowerCase();
    }

    public Class<Direction> getObjectClass() {
        return Direction.class;
    }

    protected String toPath(Direction from) {
        return toValue(from);
    }
}
