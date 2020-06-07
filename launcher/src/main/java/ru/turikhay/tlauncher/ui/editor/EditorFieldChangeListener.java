package ru.turikhay.tlauncher.ui.editor;

public abstract class EditorFieldChangeListener extends EditorFieldListener {
    protected void onChange(EditorHandler handler, String oldValue, String newValue) {
        if (newValue != null || oldValue != null) {
            if (newValue == null || !newValue.equals(oldValue)) {
                onChange(oldValue, newValue);
            }
        }
    }

    protected abstract void onChange(String var1, String var2);
}
