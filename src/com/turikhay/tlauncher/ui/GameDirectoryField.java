package com.turikhay.tlauncher.ui;

import java.io.File;

import com.turikhay.tlauncher.util.MinecraftUtil;

public class GameDirectoryField extends ExtendedTextField {
	private static final long serialVersionUID = 9048714882203326864L;
	
	GameDirectoryField(SettingsForm settingsform){		
		super(settingsform);
	}
	
	public void setText(String dir){
		if(dir == null || !new File(dir).isDirectory())
			dir = MinecraftUtil.getWorkingDirectory().toString();
		
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
