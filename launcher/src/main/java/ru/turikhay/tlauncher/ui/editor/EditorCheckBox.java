package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;

public class EditorCheckBox extends LocalizableCheckbox implements EditorField {
    private final String path;
    private final boolean hint;

    public EditorCheckBox(String path, boolean hint) {
        super(path);
        this.path = path;
        this.hint = hint;
        updateHint();
    }

    public EditorCheckBox(String path) {
        this(path, false);
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

    @Override
    public void updateLocale() {
        super.updateLocale();
        updateHint();
    }

    private void updateHint() {
        if (hint) {
            setToolTipText(Localizable.get(path + ".hint"));
        }
    }
}
