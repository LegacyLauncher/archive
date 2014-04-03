package com.turikhay.tlauncher.ui.converter;

import com.turikhay.tlauncher.configuration.Configuration.ConsoleType;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class ConsoleTypeConverter extends
		LocalizableStringConverter<ConsoleType> {

	public ConsoleTypeConverter() {
		super("settings.console");
	}

	@Override
	public ConsoleType fromString(String from) {
		return ConsoleType.get(from);
	}

	@Override
	public String toValue(ConsoleType from) {
		if (from == null)
			return null;
		return from.toString();
	}

	@Override
	public String toPath(ConsoleType from) {
		if (from == null)
			return null;
		return from.toString();
	}

}
