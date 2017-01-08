package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;

public class EditorCheckBox extends LocalizableCheckbox implements EditorField {
    private static final long serialVersionUID = -2540132118355226609L;

    public EditorCheckBox(String path) {
        super(path);
    }

    public String getSettingsValue() {
        return isSelected() ? "true" : "false";
    }

    public void setSettingsValue(String value) {
        setSelected(Boolean.parseBoolean(value));
    }

    public boolean isValueValid() {
        return true;
    }

    public void block(Object reason) {
        setEnabled(false);
    }

    public void unblock(Object reason) {
        setEnabled(true);
    }
}
