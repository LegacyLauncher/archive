package ru.turikhay.tlauncher.ui.text;

import ru.turikhay.tlauncher.ui.center.CenterPanel;

public abstract class CheckableTextField extends ExtendedTextField {
    private static final long serialVersionUID = 2835507963141686372L;
    private final CenterPanel parent;

    protected CheckableTextField(CenterPanel panel, String placeholder, String value) {
        super(panel, placeholder, value);
        parent = panel;
    }

    public CheckableTextField(String placeholder, String value) {
        this(null, placeholder, value);
    }

    public CheckableTextField(String placeholder) {
        this(null, placeholder, null);
    }

    public CheckableTextField(CenterPanel panel) {
        this(panel, null, null);
    }

    boolean check() {
        String text = getValue();
        String result = check(text);
        return result == null ? setValid() : setInvalid(result);
    }

    public boolean setInvalid(String reason) {
        setBackground(getTheme().getFailure());
        setForeground(getTheme().getFocus());
        if (parent != null) {
            parent.setError(reason);
        }

        return false;
    }

    public boolean setValid() {
        setBackground(getTheme().getBackground());
        setForeground(getTheme().getFocus());
        if (parent != null) {
            parent.setError(null);
        }

        return true;
    }

    protected void updateStyle() {
        super.updateStyle();
        check();
    }

    protected void onChange() {
        check();
    }

    protected abstract String check(String var1);
}
