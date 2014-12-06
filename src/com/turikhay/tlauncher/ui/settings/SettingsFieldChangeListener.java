package com.turikhay.tlauncher.ui.settings;

public abstract class SettingsFieldChangeListener extends SettingsFieldListener {
	@Override
	protected void onChange(SettingsHandler handler, String oldValue,
			String newValue) {
		if (newValue == null && oldValue == null)
			return;
		if (newValue != null && newValue.equals(oldValue))
			return;

		this.onChange(oldValue, newValue);
	}

	protected abstract void onChange(String oldValue, String newValue);
}
