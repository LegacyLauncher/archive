package com.turikhay.tlauncher.ui;

import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.turikhay.tlauncher.TLauncher;

public class LangChoice extends Choice {
	private static final long serialVersionUID = 570709181645184106L;
	private Map<String, String> replacer = new LinkedHashMap<String, String>();
	private String current;
	
	boolean changed;
	
	LangChoice(SettingsForm sf){
		String[] available = TLauncher.SUPPORTED_LOCALE;
		
		for(Locale loc : Locale.getAvailableLocales()){
			for(String id : available){
				if(!loc.toString().equals(id)) continue;
				String curdisp = loc.getDisplayCountry(Locale.ENGLISH) + " ("+id+")";
					replacer.put(curdisp, id);
					this.add(curdisp);
				if(TLauncher.getInstance().getLocale() == loc) this.select(curdisp);
				break;
			}
		}
		
		this.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(e.getItem().equals(current)) changed = false;
				else changed = true;
			}
		});
	}
	
	public String getValue(){
		return replacer.get(this.getSelectedItem());
	}
	
	public void selectValue(String id){
		for(Entry<String, String> curen : replacer.entrySet())
			if(curen.getKey().equals(id))
				this.select(curen.getValue());
		setCurrent();
	}
	
	public void setCurrent(){
		changed = false;
		current = getSelectedItem();
	}
}
