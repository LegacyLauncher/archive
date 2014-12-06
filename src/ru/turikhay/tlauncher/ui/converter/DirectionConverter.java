package ru.turikhay.tlauncher.ui.converter;

import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;
import ru.turikhay.util.Direction;
import ru.turikhay.util.Reflect;

public class DirectionConverter extends LocalizableStringConverter<Direction> {

	public DirectionConverter() {
		super("settings.direction");
	}

	@Override
	public Direction fromString(String from) {
		return Reflect.parseEnum(Direction.class, from);
	}

	@Override
	public String toValue(Direction from) {
		if(from == null)
			return null;
		return from.toString().toLowerCase();
	}

	@Override
	public Class<Direction> getObjectClass() {
		return Direction.class;
	}

	@Override
	protected String toPath(Direction from) {
		return toValue(from);
	}

}
