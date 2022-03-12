package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;

public class LocalizableButton extends ExtendedButton implements LocalizableComponent {
    private String path;
    private String[] variables = Localizable.checkVariables(Localizable.EMPTY_VARS);

    private String hint;
    private String[] hintVars = Localizable.checkVariables(Localizable.EMPTY_VARS);

    public LocalizableButton() {
    }

    public LocalizableButton(ImageIcon icon, String hint) {
        this();
        setIcon(icon);
        setToolTipText(hint);
    }

    public LocalizableButton(String path) {
        this();
        setText(path);
    }

    public LocalizableButton(String path, Object... vars) {
        this();
        setText(path, vars);
    }

    public void setText(String path, Object... vars) {
        this.path = path;
        variables = Localizable.checkVariables(vars);
        super.setText(Localizable.get(path, vars));
    }

    public void setText(String path) {
        setText(path, Localizable.EMPTY_VARS);
    }

    public void setToolTipText(String hint, Object... vars) {
        this.hint = hint;
        hintVars = Localizable.checkVariables(vars);
        super.setToolTipText(Localizable.get(hint, vars));
    }

    public void setToolTipText(String hint) {
        setToolTipText(hint, Localizable.EMPTY_VARS);
    }

    public void updateLocale() {
        setText(path, (Object[]) variables);
        setToolTipText(hint, (Object[]) hintVars);
    }
}