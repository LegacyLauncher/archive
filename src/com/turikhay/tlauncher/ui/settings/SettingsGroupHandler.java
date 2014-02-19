package com.turikhay.tlauncher.ui.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsGroupHandler {	
	private final SettingsHandler[] handlers;
	private final SettingsFieldListener listener;
	protected final List<SettingsFieldChangeListener> listeners;
	
	private final int checkedLimit;
	private int changedFlag, checkedFlag;
	
	SettingsGroupHandler(SettingsHandler...handlers){		
		if(handlers == null)
			throw new NullPointerException();
		
		this.checkedLimit = handlers.length;
		
		this.listener = new SettingsFieldListener(){
			protected void onChange(SettingsHandler handler, String oldValue, String newValue){
				if(newValue == null) return;
				if(!newValue.equals(oldValue)) ++changedFlag;
				
				++checkedFlag;
				
				if(checkedFlag == checkedLimit){
					if(changedFlag > 0)
						for(SettingsFieldChangeListener listener : listeners)
							listener.onChange(null, null);
					
					checkedFlag = changedFlag = 0;
				}
			}
		};
		
		for(int i=0;i<handlers.length;i++){
			SettingsHandler handler = handlers[i];
			if(handler == null)
				throw new NullPointerException("Handler is NULL at " + i);
			
			handler.addListener(listener);
		}
		
		this.handlers = new SettingsHandler[handlers.length];
		System.arraycopy(handlers, 0, this.handlers, 0, handlers.length);
		
		this.listeners = Collections.synchronizedList(new ArrayList<SettingsFieldChangeListener>());
	}
	
	public boolean addListener(SettingsFieldChangeListener listener){
		if(listener == null)
			throw new NullPointerException();
		
		return listeners.add(listener);		
	}
	
	public boolean removeListener(SettingsFieldChangeListener listener){
		if(listener == null)
			throw new NullPointerException();
		
		return listeners.remove(listener);		
	}
}
