package com.turikhay.tlauncher.ui;

public interface SettingsField {
	String getSettingsPath();
	String getValue();
	boolean isValueValid();
	void setValue(String value);
	void setToDefault();
}
