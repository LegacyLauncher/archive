package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;

public class LocalizableLabel extends ExtendedLabel implements LocalizableComponent {
    private static final long serialVersionUID = 7628068160047735335L;
    protected String path;
    protected String[] variables;
    private boolean notEmpty;

    public LocalizableLabel(String path, Object... vars) {
        setText(path, vars);
        setFont(getFont().deriveFont(TLauncherFrame.getFontSize()));
    }

    public LocalizableLabel(String path) {
        this(path, Localizable.EMPTY_VARS);
    }

    public LocalizableLabel() {
        this(null);
    }

    public LocalizableLabel(int horizontalAlignment) {
        this(null);
        setHorizontalAlignment(horizontalAlignment);
    }

    public void setText(String path, Object... vars) {
        this.path = path;
        variables = Localizable.checkVariables(vars);

        if (path == null) {
            setRawText(notEmpty ? " " : "");
            return;
        }

        setRawText(Localizable.get(path, vars));
    }

    public void setText(String path) {
        setText(path, Localizable.EMPTY_VARS);
    }

    public void updateLocale() {
        setText(path, (Object[]) variables);
    }

    public void setNotEmpty(boolean flag) {
        this.notEmpty = flag;

        if (path == null) {
            setText(null);
        }
    }
}
