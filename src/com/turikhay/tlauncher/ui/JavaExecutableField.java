package com.turikhay.tlauncher.ui;

import java.io.File;

import net.minecraft.launcher.OperatingSystem;

public class JavaExecutableField extends ExtendedTextField implements SettingsField {
	private static final long serialVersionUID = 2221135591155035960L;
	private boolean saveable = true;
	
	JavaExecutableField(SettingsForm settingsform){		
		super(settingsform);
	}

	public void setText(String dir){
		if(dir == null || !new File(dir).isFile())
			dir = OperatingSystem.getCurrentPlatform().getJavaDir();
		
		super.setText(dir);
	}

	protected boolean check(String text) {		
		return true;
	}

	public String getSettingsPath() {
		return "minecraft.javadir";
	}

	public boolean isValueValid() {
		return (getValue() != null);
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
