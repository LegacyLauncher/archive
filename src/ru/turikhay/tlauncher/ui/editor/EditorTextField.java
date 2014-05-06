package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableTextField;

public class EditorTextField extends LocalizableTextField implements
		EditorField {
	private static final long serialVersionUID = 3920711425159165958L;

	private final boolean canBeEmpty;

	public EditorTextField(String prompt, boolean canBeEmpty) {
		super(prompt);

		this.canBeEmpty = canBeEmpty;
		this.setColumns(1);
	}

	public EditorTextField(String prompt) {
		this(prompt, false);
	}

	public EditorTextField(boolean canBeEmpty) {
		this(null, canBeEmpty);
	}

	public EditorTextField() {
		this(false);
	}

	@Override
	public String getSettingsValue() {
		return getValue();
	}

	@Override
	public void setSettingsValue(String value) {
		setText(value);
		setCaretPosition(0);
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
