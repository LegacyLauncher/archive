package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.exceptions.ParseException;
import com.turikhay.util.StringUtil;

public class SettingsCheckbox extends LocalizableCheckbox implements SettingsField {
	private static final long serialVersionUID = -9013976214526482171L;
	
	private String settingspath;
	private boolean defaultState, saveable;

	public SettingsCheckbox(String path, String settingspath, boolean defaultState) {
		super(path);
		
		this.settingspath = settingspath;
		this.defaultState = defaultState;
	}
	
	public SettingsCheckbox(SettingsForm sf, String path, String settingspath) {
		super(path);
		
		this.settingspath = settingspath;
		this.defaultState = sf.s.getDefaultBoolean(settingspath);
	}

	public String getValue() {
		return getState() + "";
	}

	public boolean isValueValid() {
		return true;
	}

	public void setValue(String value) {
		try{ setState(StringUtil.parseBoolean(value)); }catch(ParseException e){}
	}

	public String getSettingsPath() {
		return settingspath;
	}
	
	public void setToDefault() {
		setState(defaultState);
	}
	
	public boolean isSaveable() {
		return saveable;
	}

	public void setSaveable(boolean val) {
		this.saveable = val;
	}
}
