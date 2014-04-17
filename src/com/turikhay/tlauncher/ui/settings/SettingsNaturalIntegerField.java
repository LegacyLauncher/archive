package com.turikhay.tlauncher.ui.settings;

public class SettingsNaturalIntegerField extends SettingsTextField {
	private static final long serialVersionUID = -7930510655707946312L;

	SettingsNaturalIntegerField() {
		super();
	}

	SettingsNaturalIntegerField(String prompt) {
		super(prompt);
	}

	@Override
	public boolean isValueValid() {
		try {
			Integer.parseInt(getSettingsValue());
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
