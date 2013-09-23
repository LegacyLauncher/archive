package com.turikhay.tlauncher.ui;

import java.io.File;

import net.minecraft.launcher_.OperatingSystem;

public class JavaExecutableField extends ExtendedTextField {
	private static final long serialVersionUID = 2221135591155035960L;
	
	JavaExecutableField(SettingsForm settingsform){		
		super(settingsform);
	}

	public void setText(String dir){
		if(dir == null || !new File(dir).isFile())
			dir = OperatingSystem.getCurrentPlatform().getJavaDir();
		
		super.setText(dir);
	}

	protected boolean check(String text) {
		File f = new File(text);
		
		if(f.exists())
			if(!f.canRead() || !f.canWrite())
				return false;
		
		return true;
	}

}
