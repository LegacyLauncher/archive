package com.turikhay.tlauncher.ui;

import java.awt.Label;

import com.turikhay.tlauncher.settings.Settings;

public class LocalizableLabel extends Label implements LocalizableComponent {
	private static final long serialVersionUID = 1L;
	static Settings l;
	
	private String path, r0, r1;
	private Object w0, w1;
	
    public LocalizableLabel(String path){
    	this.path = path;
    	this.setText();
    }
    
    public LocalizableLabel(String path, int aligment){
    	super("", aligment);
    	
    	this.path = path;
    	this.setText();
    }
    
    public void setText(){ this.setText(path); }
    
    public void setText(String path){
    	this.path = path; u();
    	super.setText(l.get(path));
    }
    
    public void setText(String path, String replace, Object with){
    	this.path = path; u(); r0 = replace; w0 = with;
    	super.setText(l.get(path, replace, with));
    }
    
    public void setText(String path, String replace0, Object with0, String replace1, Object with1){
    	this.path = path; u(); r0 = replace0; w0 = with0; r1 = replace1; w1 = with1;
    	super.setText(l.get(path, replace0, with0, replace1, with1));
    }
    
    public String getPath(){
    	return path;
    }
    
    public static void setLang(Settings lang){
    	l = lang;
    }

	public void updateLocale() {
		this.setText(path, r0, w0, r1, w1);
	}
	
	private void u(){
		r0 = r1 = null;
		w0 = w1 = null;
	}

}
