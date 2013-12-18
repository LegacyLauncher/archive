package com.turikhay.tlauncher.ui;

import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;

public class LangChoice extends Choice implements SettingsField {
	private static final long serialVersionUID = 570709181645184106L;
	
	private boolean saveable = true;
	
	private Map<String, String> replacer = new LinkedHashMap<String, String>();
	private String current;
	
	boolean changed;
	
	LangChoice(SettingsForm sf){
		List<String> available = GlobalSettings.SUPPORTED_LOCALE;
		
		for(Locale loc : Locale.getAvailableLocales()){
			for(String id : available){
				if(!loc.toString().equals(id)) continue;
				String curdisp = loc.getDisplayCountry(Locale.ENGLISH) + " ("+id+")";
					replacer.put(curdisp, id);
					this.add(curdisp);
				if(TLauncher.getInstance().getSettings().getLocale() == loc) this.select(curdisp);
				break;
			}
		}
		
		this.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
                changed = !e.getItem().equals(current);
			}
		});
	}
	
	public String getValue(){
		return replacer.get(this.getSelectedItem());
	}
	
	public void setValue(String id){
		for(Entry<String, String> curen : replacer.entrySet())
			if(curen.getKey().equals(id))
				this.select(curen.getValue());
		setCurrent();
	}
	
	public void setCurrent(){
		changed = false;
		current = getSelectedItem();
	}

	public String getSettingsPath() {
		return "locale";		
	}

	public boolean isValueValid() {
		return true;
	}
	
	public void setToDefault() {}
	
	public boolean isSaveable() {
		return saveable;
	}

	public void setSaveable(boolean val) {
		this.saveable = val;
	}
}
