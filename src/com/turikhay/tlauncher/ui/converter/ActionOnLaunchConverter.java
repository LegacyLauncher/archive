package com.turikhay.tlauncher.ui.converter;

import com.turikhay.tlauncher.configuration.Configuration.ActionOnLaunch;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class ActionOnLaunchConverter extends LocalizableStringConverter<ActionOnLaunch> {

	public ActionOnLaunchConverter() {
		super("settings.launch-action");
	}

	public ActionOnLaunch fromString(String from) {
		return ActionOnLaunch.get(from);
	}

	@Override
	public String toValue(ActionOnLaunch from) {
		return from.toString();
	}

	@Override
	public String toPath(ActionOnLaunch from) {
		return from.toString();
	}
}
