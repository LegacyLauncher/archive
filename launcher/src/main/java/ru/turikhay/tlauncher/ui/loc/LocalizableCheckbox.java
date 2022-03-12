package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.swing.extended.ExtendedCheckbox;

public class LocalizableCheckbox extends ExtendedCheckbox implements LocalizableComponent {
    private String path;
    private String[] variables = Localizable.checkVariables(Localizable.EMPTY_VARS);

    public LocalizableCheckbox() {
    }

    public LocalizableCheckbox(String path) {
        this();
        setText(path);
    }

    public LocalizableCheckbox(String path, boolean state) {
        this();
        setState(state);
        setText(path);
    }

    @Deprecated
    public void setLabel(String path) {
        setText(path);
    }

    public void setText(String path, Object... vars) {
        this.path = path;
        variables = Localizable.checkVariables(vars);
        super.setText(Localizable.get(path, vars));
    }

    public void setText(String path) {
        setText(path, Localizable.EMPTY_VARS);
    }

    public String getLangPath() {
        return path;
    }

    public void updateLocale() {
        setText(path, (Object[]) variables);
    }
}
