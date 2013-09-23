package com.turikhay.tlauncher.ui;


public class ArgsField extends LocalizableTextField {
	private static final long serialVersionUID = -5279771273100196802L;

	ArgsField(SettingsForm sf, String placeholder) {
		super(sf, placeholder, null, 0);
	}

	protected boolean check(String text) {
		return true;
	}

}
