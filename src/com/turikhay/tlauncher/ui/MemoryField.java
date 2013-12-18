package com.turikhay.tlauncher.ui;

import net.minecraft.launcher.OperatingSystem;

public class MemoryField extends ExtendedTextField {
	private static final long serialVersionUID = 104141941185197117L;
	private long maxMB = Runtime.getRuntime().maxMemory() / 1024 / 1024;

	MemoryField(SettingsForm settingsform){		
		super(settingsform);
	}

	protected boolean check(String text) {
		if(text == null || text.equals("")) return true;
		
		int cur = -1;
		try{ cur = Integer.parseInt(text); }catch(Exception e){ return setError(l.get("settings.java.memory.parse")); }
		
		if(cur < 0 || cur > maxMB) return setError(l.get("settings.java.memory.incorrect", "s", maxMB));
		
		return true;
	}
	
	public int getSpecialValue(){		
		String val = getValue();
		if(val == null || val.equals("")) return OperatingSystem.getCurrentPlatform().getRecommendedMemory();
		
		return Integer.parseInt(val);
	}

}
