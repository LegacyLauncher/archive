package com.turikhay.tlauncher.ui;

import java.io.File;

import com.turikhay.util.MinecraftUtil;

public class GameDirectoryField extends ExtendedTextField implements SettingsField {
	private static final long serialVersionUID = 9048714882203326864L;
	private boolean saveable = true;
	
	GameDirectoryField(SettingsForm settingsform){		
		super(settingsform);
	}
	
	public void setValue(String dir){
		if(dir == null || !new File(dir).isDirectory())
			dir = MinecraftUtil.getDefaultWorkingDirectory().toString();
		
		super.setText(dir);
	}

	protected boolean check(String text) {
		File f = new File(text);
		
		if(f.exists())
			if(!f.canRead() || !f.canWrite())
				return false;
		
		return true;
	}

	public String getSettingsPath() {
		return "minecraft.gamedir";
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
