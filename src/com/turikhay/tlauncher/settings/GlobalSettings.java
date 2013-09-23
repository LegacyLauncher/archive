package com.turikhay.tlauncher.settings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;

public class GlobalSettings extends Settings {
	public final static File file = MinecraftUtil.getNativeOptionsFile();
	public final static boolean firstRun = !file.exists();
	
	private Map<String, Object> d = new HashMap<String, Object>(); // defaults
	private double version = 0.13;
	
	public GlobalSettings() throws IOException {
		super(file);
		
		d.put("settings.version", version);
		
		d.put("login.auto", false);
		d.put("login.auto.timeout", 3);
		
		d.put("minecraft.size.width", 925);
		d.put("minecraft.size.height", 525);
		d.put("minecraft.versions.snapshots", true);
		d.put("minecraft.versions.beta", true);
		d.put("minecraft.versions.alpha", true);
		
		d.put("gui.sun", true);
		
		d.put("locale", TLauncher.getSupported());
		
		boolean forcedrepair = this.getDouble("settings.version") != version;
		
		for(Entry<String, Object> curen : d.entrySet()){
			String key = curen.getKey(), value = s.get(key); Object defvalue = d.get(key);			
			if(forcedrepair || value == null){ repair(key, defvalue); continue; }
			try {
				
				if(defvalue instanceof Integer) Integer.parseInt(value);
				else if(defvalue instanceof Boolean) this.parseBoolean(value);
				else if(defvalue instanceof Double) Double.parseDouble(value);
				else if(defvalue instanceof Long) Long.parseLong(value);
				
				continue;
			}catch(Exception e){}
			
			repair(key, defvalue);
		}
		
		this.save();
	}
	
	public String get(String key){
		String r = s.get(key);
		if(r == "") return null;
		return r;
	}
	
	public String getDefault(String key){
		String r = d.get(key) + "";
		if(r == "") return null;
		return r;
	}
	
	public int getDefaultInteger(String key){
		try{ return Integer.parseInt(d.get(key)+""); }catch(Exception e){ return 0; }
	}
	
	public long getDefaultLong(String key){
		try{ return Long.parseLong(d.get(key)+""); }catch(Exception e){ return 0; }
	}
	
	public double getDefaultDouble(String key){
		try{ return Double.parseDouble(d.get(key)+""); }catch(Exception e){ return 0; }
	}
	
	public float getDefaultFloat(String key){
		try{ return Float.parseFloat(d.get(key)+""); }catch(Exception e){ return 0; }
	}
	
	public boolean getDefaultBoolean(String key){
		try{ return Boolean.parseBoolean(d.get(key)+""); }catch(Exception e){ return false; }
	}
	
	public Locale getLocale(){
		String locale = get("locale");
		
		for(Locale lookup : Locale.getAvailableLocales()){
			String lookup_name = lookup.toString();
			for(String curloc : TLauncher.SUPPORTED_LOCALE){
				if(!lookup_name.equals(curloc)) continue;
				if(!curloc.equals(locale)) continue;
				// Selected locale is supported
				return lookup;
			}
		}
		
		return TLauncher.DEFAULT_LOCALE;
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
		
		set(key, value, false);
	}

}
