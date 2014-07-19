package ru.turikhay.tlauncher.ui.editor;

public abstract class EditorFieldListener {
	protected abstract void onChange(EditorHandler handler, String oldValue,
			String newValue);
}
