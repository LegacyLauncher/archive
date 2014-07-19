package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;

public class EditorCheckBox extends LocalizableCheckbox implements
		EditorField {
	private static final long serialVersionUID = -2540132118355226609L;

	public EditorCheckBox(String path) {
		super(path);
	}

	@Override
	public String getSettingsValue() {
		return isSelected() ? "true" : "false";
	}

	@Override
	public void setSettingsValue(String value) {
		this.setSelected(Boolean.parseBoolean(value));
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
