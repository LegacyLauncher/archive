package com.turikhay.tlauncher.ui.converter;

import java.util.Locale;

import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class LocaleConverter extends LocalizableStringConverter<Locale> {
	public LocaleConverter() {
		super(null);
	}

	@Override
	public String toString(Locale from) {
		return from.getDisplayCountry(Locale.US) + " (" + from.toString() + ")";
	}

	@Override
	public Locale fromString(String from) {
		return Configuration.getLocaleOf(from);
	}

	@Override
	public String toValue(Locale from) {
		if (from == null)
			return null;
		return from.toString();
	}

	@Override
	public String toPath(Locale from) {
		return null;
	}

}
