package com.turikhay.tlauncher.ui.text;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.center.CenterPanelTheme;

public class ExtendedPasswordField extends JPasswordField {
	private static final long serialVersionUID = 3175896797135831502L;
	private static final String DEFAULT_PLACEHOLDER = "пассворд, лол";
	
	private CenterPanelTheme theme;	
	private String placeholder;
	
	public ExtendedPasswordField(CenterPanel panel, String placeholder){
		this.theme = (panel == null)? CenterPanel.defaultTheme : panel.getTheme();		
		this.placeholder = (placeholder == null)? DEFAULT_PLACEHOLDER : placeholder;
		
		this.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				onFocusGained();
			}

			@Override
			public void focusLost(FocusEvent e) {
				onFocusLost();
			}
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
		
		this.setText(null);
	}
	
	public ExtendedPasswordField(){
		this(null, null);
	}
	
	private String getValueOf(String value){
		if(value == null || value.isEmpty() || value.equals(placeholder))
			return null;
		
		return value;
	}
	
	@Override
	@Deprecated
	public String getText(){
		return super.getText();
	}
	
	
	@Override
    public char[] getPassword() {
		String value = getValue();
		
		if(value == null)
			return new char[0];
		
		return value.toCharArray();
    }
	
	public boolean hasPassword(){
		return getValue() != null;
	}
	
	private String getValue(){
		return getValueOf(getText());
	}
	
	@Override
	public void setText(String text){
		String value = getValueOf(text);
		
		if(value == null)
			setPlaceholder();
		else {
			setForeground(theme.getFocus());
			super.setText(value);
		}
	}
	
	private void setPlaceholder(){
		setForeground(theme.getFocusLost());
		super.setText(placeholder);
	}
	
	private void setEmpty(){
		setForeground(theme.getFocus());
		super.setText("");
	}
	
	protected void updateStyle(){
		setForeground(getValue() == null ? theme.getFocusLost() : theme.getFocus());
	}
	
	public String getPlaceholder(){
		return placeholder;
	}
	
	public void setPlaceholder(String placeholder){
		this.placeholder = placeholder == null? DEFAULT_PLACEHOLDER : placeholder;
		if(getValue() == null) setPlaceholder();
	}
	
	public CenterPanelTheme getTheme(){
		return theme;
	}
	
	public void setTheme(CenterPanelTheme theme){
		if(theme == null)
			theme = CenterPanel.defaultTheme;
		
		this.theme = theme;
		updateStyle();
	}
	
	protected void onFocusGained(){
		if(getValue() == null) setEmpty();
	}
	
	protected void onFocusLost(){
		if(getValue() == null) setPlaceholder();
	}
	
	protected void onChange(){
	}
}
