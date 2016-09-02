package ru.turikhay.tlauncher.ui.text;

import ru.turikhay.tlauncher.ui.center.CenterPanel;

public class InvalidateTextField extends CheckableTextField {
    private static final long serialVersionUID = -4076362911409776688L;

    protected InvalidateTextField(CenterPanel panel, String placeholder, String value) {
        super(panel, placeholder, value);
    }

    public InvalidateTextField(CenterPanel panel) {
        this(panel, null, null);
    }

    protected String check(String text) {
        return null;
    }
}
