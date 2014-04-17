package com.turikhay.tlauncher.ui.settings;

abstract class SettingsFieldListener {
	protected abstract void onChange(SettingsHandler handler, String oldValue,
			String newValue);
}
