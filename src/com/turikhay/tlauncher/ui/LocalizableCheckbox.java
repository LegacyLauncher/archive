package com.turikhay.tlauncher.ui;

import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

import com.turikhay.tlauncher.settings.Settings;

public class LocalizableCheckbox extends JCheckBox implements LocalizableComponent {
	private static final long serialVersionUID = 1L;
	static Settings l;
	
	private String path;
	
	public LocalizableCheckbox(String path) {
		init();
		this.setLabel(path);
	}

	public LocalizableCheckbox(String path, boolean state) {
		super("", state); init();
		
		this.setText(path);
	}
	
	@Deprecated
	public void setLabel(String path){
		this.setText(path);
	}
	
	public void setText(String path){
		this.path = path;
		super.setText((l == null)? path : l.get(path));
	}
	
	public String getLangPath(){
		return path;
	}
	
	public boolean getState(){
		return super.getModel().isSelected();
	}
	
	public void setState(boolean state){
		super.getModel().setSelected(state);
	}
	
	public void addListener(ItemListener l){
		super.getModel().addItemListener(l);
	}
	
	public void removeListener(ItemListener l){
		super.getModel().removeItemListener(l);
	}

	public void updateLocale() {
		this.setLabel(path);
	}
	
	private void init(){
		this.setOpaque(false);
		//this.setBackground(new Color(0, 0, 0, 0));
	}
}
