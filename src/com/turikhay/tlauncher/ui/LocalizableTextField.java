package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;

public abstract class LocalizableTextField extends ExtendedTextField implements LocalizableComponent {
	private static final long serialVersionUID = 1L;
	static Settings l;
	
	private String placeholderPath;
	
	public LocalizableTextField(){}
	
	public LocalizableTextField(CenterPanel parentPanel, String placeholder, String text, int columns){
		super(parentPanel, null, text, columns);
		
		this.setPlaceholder(placeholder);
	}
	
	public LocalizableTextField(String placeholder, String text, int columns){
		this(null, placeholder, text, columns);
	}
	
	public LocalizableTextField(String text, int columns){
		this(null, text, columns);
	}
	
	public LocalizableTextField(CenterPanel parentPanel, String placeholder, String text){
		this(parentPanel, placeholder, text, (text != null)? text.length() : (placeholder != null)? placeholder.length() : 0);
	}
	
	public LocalizableTextField(CenterPanel parentPanel, String placeholder){
		this(parentPanel, placeholder, null);
	}
	
	public LocalizableTextField(String placeholder){
		this(null, placeholder, null);		
	}
	
	public LocalizableTextField(CenterPanel parentPanel){
		this(parentPanel, null, null);
	}
	
	public void setPlaceholder(String placeholderPath){
		this.placeholderPath = placeholderPath;
		super.setPlaceholder((l == null)? placeholderPath : l.get(placeholderPath));
	}
	
	public String getPlaceholderPath(){
		return this.placeholderPath;
	}
	
	public void updateLocale(){
		super.updateLocale();
		this.setPlaceholder(placeholderPath);
	}
}
