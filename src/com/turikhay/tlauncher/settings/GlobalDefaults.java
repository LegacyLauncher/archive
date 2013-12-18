package com.turikhay.tlauncher.settings;

import java.util.HashMap;
import java.util.Map;

import com.turikhay.tlauncher.settings.GlobalSettings.ActionOnLaunch;
import com.turikhay.util.IntegerArray;
import com.turikhay.util.U;

public class GlobalDefaults {
	private Map<String, Object> d = new HashMap<String, Object>(); // defaults
	
	GlobalDefaults(GlobalSettings g){
		d.put("settings.version", g.version);
		
		d.put("login.auto", false);
		d.put("login.auto.timeout", 3);
		
		d.put("minecraft.size", new IntegerArray(925, 530));
		
		d.put("minecraft.versions.snapshots", true);
		d.put("minecraft.versions.beta", false);
		d.put("minecraft.versions.alpha", false);
		d.put("minecraft.versions.cheats", false);
		
		d.put("minecraft.onlaunch", ActionOnLaunch.getDefault());
		
		d.put("gui.sun", true);
		d.put("gui.console.width", 620);
		d.put("gui.console.height", 400);
		d.put("gui.console.x", 1);
		d.put("gui.console.y", 1);
		
		d.put("timeout.connection", U.DEFAULT_CONNECTION_TIMEOUT);
		
		d.put("locale", GlobalSettings.getSupported());
	}
	
	public Map<String, Object> getMap(){
		return d;
	}
	
	public Object get(String key){
		return d.get(key);
	}
}
