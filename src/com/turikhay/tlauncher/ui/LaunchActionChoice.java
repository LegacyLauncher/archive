package com.turikhay.tlauncher.ui;

import java.awt.Choice;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.GlobalSettings.ActionOnLaunch;
import com.turikhay.tlauncher.settings.Settings;

public class LaunchActionChoice extends Choice implements LocalizableComponent, SettingsField {
	private static final long serialVersionUID = 7116359806652349614L;
	private Map<String, String> values = new LinkedHashMap<String, String>();
	
	private final SettingsForm sf;
	private Settings l;
	
	private boolean saveable;
	
	LaunchActionChoice(SettingsForm settingsform){
		this.sf = settingsform;
		this.l = sf.l;
		
		this.createList();
	}
	
	private void createList(){ removeAll();
		ActionOnLaunch[] available = GlobalSettings.ActionOnLaunch.values();
		String current = TLauncher.getInstance().getSettings().getActionOnLaunch().toString();
		
		for(ActionOnLaunch al : available){
			String value = al.toString(), key = l.get("settings.launch-action." + value);
			values.put(key, value);
			add(key);
			
			if(current.equals(value)) select(key);
		}
	}
	
	public String getValue(){
		return getValue(getSelectedItem());
	}
	
	private String getValue(String name){
		return values.get(name);
	}
	
	public void setValue(String id){
		if(id == null) id = GlobalSettings.ActionOnLaunch.getDefault().toString();
		for(Entry<String, String> curen : values.entrySet())
			if(curen.getKey().equals(id))
				select(curen.getValue());
	}
	
	public void updateLocale() {
		this.createList();
	}

	public String getSettingsPath() {
		return "minecraft.onlaunch";
	}

	public boolean isValueValid() {
		return true;
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
