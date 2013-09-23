package com.turikhay.tlauncher.ui;


public class AutologinTimeoutField extends ExtendedTextField implements LocalizableComponent {
	private static final long serialVersionUID = 104141941185197117L;
	private static final int MAX_TIMEOUT = Autologin.MAX_TIMEOUT;

	AutologinTimeoutField(SettingsForm settingsform){		
		super(settingsform);
	}

	protected boolean check(String text) {		
		int cur = -1;
		try{ cur = Integer.parseInt(text); }catch(Exception e){ return setError(l.get("settings.tlauncher.autologin.parse")); }
		
		if(cur < 1 || cur > MAX_TIMEOUT) return setError(l.get("settings.tlauncher.autologin.incorrect", "s", MAX_TIMEOUT));
		
		return true;
	}
	
	public int getSpecialValue(){		
		String val = getValue();
		if(val == null || val.equals("")) return Autologin.DEFAULT_TIMEOUT;
		
		return Integer.parseInt(val);
	}

	public void updateLocale() { check(); }
}
