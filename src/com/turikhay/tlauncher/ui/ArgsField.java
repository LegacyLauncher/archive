package com.turikhay.tlauncher.ui;


public class ArgsField extends LocalizableTextField implements SettingsField {
	private static final long serialVersionUID = -5279771273100196802L;
	private String settingspath;
	private boolean saveable = true;

	ArgsField(SettingsForm sf, String placeholder, String settingspath){
		super(sf, placeholder, null, 0);
		
		this.settingspath = settingspath;
	}

	protected boolean check(String text) {
		return true;
	}

	public boolean isValueValid() {
		return true;
	}

	public String getSettingsPath() {
		return settingspath;
	}

	public void setToDefault() {
		setValue(null);
	}

	public boolean isSaveable() {
		return saveable;
	}

	public void setSaveable(boolean val) {
		this.saveable = val;
	}

}
