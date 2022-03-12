package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.swing.TransparentButton;

public class LocalizableTransparentButton extends TransparentButton implements LocalizableComponent {
    private static final long serialVersionUID = -1357535949476677157L;
    private String path;
    private String[] variables;

    public LocalizableTransparentButton(String path, Object... vars) {
        setOpaque(false);
        setText(path, vars);
    }

    void setText(String path, Object... vars) {
        this.path = path;
        variables = Localizable.checkVariables(vars);
        String value = Localizable.get(path);

        for (int i = 0; i < variables.length; ++i) {
            value = value.replace("%" + i, variables[i]);
        }

        super.setText(value);
    }

    public void setText(String path) {
        setText(path, Localizable.EMPTY_VARS);
    }

    public void updateLocale() {
        setText(path, (Object[]) variables);
    }
}
