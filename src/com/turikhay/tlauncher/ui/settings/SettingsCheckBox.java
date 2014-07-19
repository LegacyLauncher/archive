package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.loc.LocalizableCheckbox;

public class SettingsCheckBox extends LocalizableCheckbox implements
		SettingsField {
	private static final long serialVersionUID = -2540132118355226609L;

	SettingsCheckBox(String path) {
		super(path);
	}

	@Override
	public String getSettingsValue() {
		return isSelected() ? "true" : "false";
	}

	@Override
	public void setSettingsValue(String value) {
		this.setSelected(Boolean.parseBoolean(value));
	}

	@Override
	public boolean isValueValid() {
		return true;
	}

	@Override
	public void block(Object reason) {
		this.setEnabled(false);
	}

	@Override
	public void unblock(Object reason) {
		this.setEnabled(true);
	}
}
