package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.loc.LocalizableTextField;

public class SettingsTextField extends LocalizableTextField implements SettingsField {
	private static final long serialVersionUID = 3920711425159165958L;
	
	private final boolean canBeEmpty;
	
	SettingsTextField(boolean canBeEmpty){
		this.canBeEmpty = canBeEmpty;
	}
	
	SettingsTextField(){
		this(false);
	}
	
	SettingsTextField(String prompt, boolean canBeEmpty){
		super(prompt);
		
		this.canBeEmpty = canBeEmpty;
	}
	
	SettingsTextField(String prompt){
		this(prompt, false);
	}

	@Override
	public String getSettingsValue() {
		return getValue();
	}

	@Override
	public void setSettingsValue(String value) {
		setText(value);
	}

	@Override
	public boolean isValueValid() {
		String text = getValue();
		return text != null || canBeEmpty;
	}

	@Override
	public void block(Object reason) {
		this.setEnabled(false);
	}

	@Override
	public void unblock(Object reason) {
		this.setEnabled(true);
	}

}
