package ru.turikhay.tlauncher.ui.editor;

public class EditorIntegerField extends EditorTextField {
	private static final long serialVersionUID = -7930510655707946312L;

	public EditorIntegerField() {
		super();
	}

	public EditorIntegerField(String prompt) {
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
