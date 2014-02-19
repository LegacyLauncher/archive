package com.turikhay.tlauncher.ui.converter;

import com.turikhay.tlauncher.configuration.Configuration.ConnectionQuality;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class ConnectionQualityConverter extends LocalizableStringConverter<ConnectionQuality> {

	public ConnectionQualityConverter() {
		super("settings.connection");
	}

	@Override
	public ConnectionQuality fromString(String from) {
		return ConnectionQuality.get(from);
	}

	@Override
	public String toValue(ConnectionQuality from) {
		return from.toString();
	}

	@Override
	public String toPath(ConnectionQuality from) {
		return from.toString();
	}

}
