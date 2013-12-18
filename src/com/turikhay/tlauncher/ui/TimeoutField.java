package com.turikhay.tlauncher.ui;

import com.turikhay.util.U;

public class TimeoutField extends LocalizableTextField implements SettingsField {
	private static final long serialVersionUID = -1540285891285378219L;
	private FieldType ft;
	
	private boolean saveable;
	
	TimeoutField(SettingsForm sf, FieldType ft){
		this.ft = ft;
		
		this.addFocusListener(sf.warner);
	}
	
	protected boolean check(String text) {
		int cur = -1;
		try{ cur = Integer.parseInt(text); }catch(Exception e){ return setError(l.get("settings.timeouts.incorrect")); }
		if(cur < 1) return setError(l.get("settings.timeouts.incorrect"));
		
		return true;
	}

	public String getSettingsPath() {
		switch(ft){
		case READ: return "timeout.read";
		case CONNECTION: return "timeout.connection";
		}
		throw new IllegalStateException("Unknown field type!");
	}

	public boolean isValueValid() {
		return check();
	}
	public void setToDefault() { setValue(U.DEFAULT_READ_TIMEOUT); }
	
	public boolean isSaveable() {
		return saveable;
	}

	public void setSaveable(boolean val) {
		this.saveable = val;
	}
	
	public enum FieldType{
		READ, CONNECTION;
	}

}
