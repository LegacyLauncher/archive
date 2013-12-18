package com.turikhay.tlauncher.ui;


public class AutologinTimeoutField extends ExtendedTextField implements LocalizableComponent, SettingsField {
	private static final long serialVersionUID = 104141941185197117L;
	private static final int DEFAULT_TIMEOUT = Autologin.DEFAULT_TIMEOUT, MAX_TIMEOUT = Autologin.MAX_TIMEOUT;
	private boolean saveable = true;

	AutologinTimeoutField(SettingsForm settingsform){		
		super(settingsform);
	}

	protected boolean check(String text) {		
		int cur = -1;
		try{ cur = Integer.parseInt(text); }catch(Exception e){ return setError(l.get("settings.tlauncher.autologin.parse")); }
		
		if(cur < 2 || cur > MAX_TIMEOUT) return setError(l.get("settings.tlauncher.autologin.incorrect", "s", MAX_TIMEOUT));
		
		return true;
	}

	public void updateLocale() { check(); }

	public String getSettingsPath() {
		return "login.auto.timeout";
	}

	public boolean isValueValid() {
		String val = getValue();
        return !(val == null || val.equals(""));

    }
	
	public void setToDefault() {
		setValue(DEFAULT_TIMEOUT);
	}
	
	public boolean isSaveable() {
		return saveable;
	}

	public void setSaveable(boolean val) {
		this.saveable = val;
	}
}
