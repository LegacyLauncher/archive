package com.turikhay.tlauncher.ui.settings;

public abstract class SettingsFieldListener {
	protected abstract void onChange(SettingsHandler handler, String oldValue, String newValue);
}
