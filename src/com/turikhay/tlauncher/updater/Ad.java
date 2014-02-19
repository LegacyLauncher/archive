package com.turikhay.tlauncher.updater;

import java.net.URI;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.configuration.SimpleConfiguration;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.util.U;

public class Ad {
	private int id;
	private String title, text, textarea;
	private URI uri;
	
	private boolean shown;
	
	private Ad(int id, String title, String text, String textarea, URI uri){
		this.id = id;
		this.title = title;
		this.text = text;
		this.textarea = textarea;
		this.uri = uri;
	}
	
	Ad(int id, String title, String text, String textarea){
		this.id = id;
		this.title = title;
		this.text = text;
		this.textarea = textarea;
	}
	
	Ad(int id, String title, String text, String textarea, String uri){
		this(id, title, text, textarea, U.makeURI(uri));
	}
	
	Ad(SimpleConfiguration settings){
		if(settings == null)
			throw new NullPointerException("Settings is NULL!");
		
		this.id = settings.getInteger("ad.id");
		this.title = settings.get("ad.title");
		this.text = settings.get("ad.text");
		this.textarea = settings.get("ad.textarea");
		
		this.uri = U.makeURI(settings.get("ad.url"));
	}
	
	public int getID(){
		return id;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getText(){
		return text;
	}
	
	public String getTextarea(){
		return textarea;
	}
	
	public URI getURI(){
		return uri;
	}
	
	public boolean canBeShown(){
		return !shown && id != 0;
	}
	
	public void show(boolean force){
		if(shown) return;
		this.shown = true;
		
		if(uri != null){
			if(Alert.showQuestion(title, text, textarea, force))
				try{ OperatingSystem.openLink(uri); }finally{}
		} else
			Alert.showMessage(title, text, textarea);
	}

}
