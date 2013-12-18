package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.TextListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.turikhay.tlauncher.settings.Settings;

public abstract class ExtendedTextField extends JTextField implements LocalizableComponent {
	private static final long serialVersionUID = 1L;
	
	protected CenterPanel parent;
	protected Settings l;
	protected String value, placeholder;
	
	protected boolean edit;
	private String error;
	
	public static final Font PLACEHOLDER_FONT = new Font("", Font.ITALIC, 12), DEFAULT_FONT = new Font("", Font.PLAIN, 12);
	public static final Color OK_BACKGROUND = Color.white, OK_FOREGROUND = Color.black, WRONG_BACKGROUND = Color.pink, WRONG_FOREGROUND = Color.black;
	
	protected Font placeholder_font = new Font("", Font.ITALIC, 12), default_font = new Font("", Font.PLAIN, 12);
	protected Color ok_background = Color.white, ok_foreground = Color.black, wrong_background = Color.pink, wrong_foreground = Color.black;
	
	protected FocusListener focusListener;
	protected TextListener textListener;
	
	protected ExtendedTextField(){
		this.initListeners();
	}
	
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
		
		this.setBackground((parent == null)? ok_background : parent.textBackground);
		this.setForeground((parent == null)? ok_foreground : parent.panelColor);
		this.setFont((parent == null)? placeholder_font : parent.font_italic);
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
		if(text != null && !text.equals(" ")){
			this.onChangePrepare();
			
			String r = text.toString();
			this.value = (check(r))? r : null;
			super.setText(r);
		} else if(placeholder != null) setPlaceholder();
	}
	
	public void setValue(Object value){
		this.setText(value);
	}
	
	public void setValue(String value){
		this.setText(value);
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
		this.setBackground((parent == null)? wrong_background : parent.wrongColor);
		this.setForeground((parent == null)? wrong_foreground : parent.textForeground);
		
		if(parent != null && reason != null)
			this.parent.setError(reason);
		
		return false;
	}
	
	protected boolean ok(){		
		this.setBackground((parent == null)? ok_background : parent.textBackground);
		this.setForeground((parent == null)? ok_foreground : parent.textForeground);
		this.setFont((parent == null)? default_font : parent.font);
		if(parent != null) this.parent.setError(null);
		
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
	
	protected void onChange(){		
		if(!edit) return;
		
		value = super.getText();
		
		if(!check())
			value = null;
	}
	
	protected void onChangePrepare(){
		if(edit) return;
		
		this.edit = true;
		this.setFont(parent == null? default_font : parent.font);
		this.setForeground(parent == null? ok_foreground : parent.textForeground);
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
		
		this.getDocument().addDocumentListener(new DocumentListener(){
			public void insertUpdate(DocumentEvent e) {
				onChange();
			}
			public void removeUpdate(DocumentEvent e) {
				onChange();
			}
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}
		});
	}
	
	public void updateLocale(){ check(); }
	
	protected abstract boolean check(String text);
}
