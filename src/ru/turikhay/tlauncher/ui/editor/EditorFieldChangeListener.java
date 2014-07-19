package ru.turikhay.tlauncher.ui.editor;

public abstract class EditorFieldChangeListener extends EditorFieldListener {
	@Override
	protected void onChange(EditorHandler handler, String oldValue,
			String newValue) {
		if (newValue == null && oldValue == null)
			return;
		if (newValue != null && newValue.equals(oldValue))
			return;

		this.onChange(oldValue, newValue);
	}

	protected abstract void onChange(String oldValue, String newValue);
}
