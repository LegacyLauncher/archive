package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.converter.StringConverter;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;

public class SettingsComboBox<T> extends ExtendedComboBox<T> implements SettingsField {
	private static final long serialVersionUID = -2320340434786516374L;
	
	public SettingsComboBox(StringConverter<T> converter, T... values){
		super(converter);
		
		if(values == null) return;
		
		for(T value : values)
			addItem(value);
	}

	@Override
	public String getSettingsValue() {
		T value = getSelectedValue();
		return convert(value);
	}

	@Override
	public void setSettingsValue(String string) {
		T value = convert(string);
		setSelectedValue(value);
	}

	@Override
	public boolean isValueValid() {
		return true;
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
