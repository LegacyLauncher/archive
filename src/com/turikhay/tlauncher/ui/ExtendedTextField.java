package com.turikhay.tlauncher.ui;

import java.awt.TextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import com.turikhay.tlauncher.settings.Settings;

public abstract class ExtendedTextField extends TextField implements LocalizableComponent {
	private static final long serialVersionUID = 1L;
	
	protected CenterPanel parent;
	protected Settings l;
	protected String value, placeholder;
	
	protected boolean edit;
	private String error;
	
	protected FocusListener focusListener;
	protected TextListener textListener;
	
	public ExtendedTextField(CenterPanel parentPanel, String placeholder, String text, int columns){
		super(columns);
		
		this.parent = parentPanel;
		this.l = (parent != null)? parent.l : null;
		this.placeholder = placeholder;
		
		this.setText(text);
		this.initListeners();
	}
	
	public ExtendedTextField(String placeholder, String text, int columns){
		this(null, placeholder, text, columns);
	}
	
	public ExtendedTextField(String text, int columns){
		this(null, text, columns);
	}
	
	public ExtendedTextField(CenterPanel parentPanel, String placeholder, String text){
		this(parentPanel, placeholder, text, (text != null)? text.length() : (placeholder != null)? placeholder.length() : 0);
	}
	
	public ExtendedTextField(CenterPanel parentPanel, String placeholder){
		this(parentPanel, placeholder, null);
	}
	
	public ExtendedTextField(String placeholder){
		this(null, placeholder, null);		
	}
	
	public ExtendedTextField(CenterPanel parentPanel){
		this(parentPanel, null, null);
	}
	
	public void setPlaceholder(){
		super.setText(placeholder);
		ok();
		
		if(parent == null) return;
		this.setBackground(parent.textBackground);
		this.setForeground(parent.panelColor);
		this.setFont(parent.font_italic);
	}
	
	public void setPlaceholder(String placeholder){		
		this.placeholder = placeholder;
		if(!edit) this.setPlaceholder();
	}
	
	public String getPlaceholder(){
		return this.placeholder;
	}
	
	public void setText(String text){
		this.setText((Object) text);
	}
	
	public void setText(Object text){	
		if(text != null){
			this.onChangePrepare();
			
			String r = (text != null)? text.toString() : null;
			this.value = r;
			super.setText(r);
		} else if(placeholder != null) setPlaceholder();
	}
	
	@Deprecated
	public String getText(){		
		if(!edit) return placeholder;
		return super.getText(); 
	}
	
	public String getValue(){		
		if(!edit) return null;
		return value;
	}
	
	public boolean check(){	
		String text = this.getValue();
		
		if(check(text))
			return ok();
		return wrong(error);
	}
	
	protected boolean wrong(String reason){
		if(parent == null) return false;
		
		this.setBackground(parent.wrongColor);
		
		if(reason != null && reason != null)
			this.parent.setError(reason);
		
		return false;
	}
	
	protected boolean ok(){		
		if(parent == null) return true;
		
		this.setBackground(parent.textBackground);
		this.setForeground(parent.textForeground);
		this.setFont(parent.font);
		this.parent.setError(null);
		
		return true;
	}
	
	protected void onFocusGained(FocusEvent e){
		if(edit) return;
		
		this.edit = true;
		this.setText("");
	}
	
	protected void onFocusLost(FocusEvent e){		
		String text = getValue();
		
		if(text != null && !text.equals("")) return;
		this.edit = false;
		this.setPlaceholder();
	}
	
	protected void onChange(TextEvent e){		
		if(!edit) return;
		
		value = super.getText();
		
		if(!check())
			value = null;
	}
	
	protected void onChangePrepare(){
		if(edit) return;
		
		this.edit = true;
		this.setFont(parent.font);
		this.setForeground(parent.textForeground);
	}
	
	protected boolean setError(String error){
		this.error = error;
		return false;
	}
	
	protected String getError(){
		return error;
	}
	
	private void initListeners(){
		this.addFocusListener(focusListener = new FocusListener(){
			public void focusGained(FocusEvent e) { onFocusGained(e); }
			public void focusLost(FocusEvent e) { onFocusLost(e); }
		});
		
		this.addTextListener(textListener = new TextListener(){
			public void textValueChanged(TextEvent e) { onChange(e); }
		});
	}
	
	public void updateLocale(){ check(); }
	
	protected abstract boolean check(String text);
}
