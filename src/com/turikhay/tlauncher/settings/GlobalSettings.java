package com.turikhay.tlauncher.settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;

public class GlobalSettings extends Settings {
	private Map<String, Object> d = new HashMap<String, Object>(); // defaults
	private double version = 0.1;
	
	public GlobalSettings() throws IOException {
		super( MinecraftUtil.getNativeOptionsFile() );
		
		d.put("settings.version", version);
		
		d.put("login.auto", false);
		d.put("login.auto.timeout", 5);
		
		d.put("minecraft.width", 925);
		d.put("minecraft.height", 525);
		d.put("minecraft.versions.snapshots", true);
		d.put("minecraft.versions.beta", true);
		d.put("minecraft.versions.alpha", true);
		
		d.put("gui.sun", true);
		
		boolean forcedrepair = this.getDouble("settings.version") != version;
		
		for(Entry<String, Object> curen : d.entrySet()){
			String key = curen.getKey(), value = s.get(key); Object defvalue = d.get(key);			
			if(forcedrepair || value == null){ this.repair(key, defvalue); continue; }
			try{
				if(defvalue instanceof Integer) Integer.parseInt(value);
				else if(defvalue instanceof Boolean) this.parseBoolean(value);
				else if(defvalue instanceof Double) Double.parseDouble(value);
				else if(defvalue instanceof Long) Long.parseLong(value);
				
				continue;
			}catch(Exception e){}
			
			this.repair(key, defvalue);
		}
		
		this.save();
	}
	
	public String get(String key){
		String r = s.get(key);
		if(r == "") return null;
		return r;
	}
	
	private boolean parseBoolean(String b) throws Exception {
		switch(b){
		case "true": return true;
		case "false": return false;
		}
		
		throw new Exception();
	}
	
	private void repair(String key, Object value) throws IOException {
		U.log("Field \""+key+"\" in GlobalSettings is invalid.");		
		this.set(key, value, false);
	}

}
