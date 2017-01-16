package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.theme.Theme;

import javax.swing.*;
import java.awt.event.ItemListener;

public class LocalizableCheckbox extends JCheckBox implements LocalizableComponent {
    private String path;
    private String[] variables = Localizable.checkVariables(Localizable.EMPTY_VARS);

    public LocalizableCheckbox() {
        init();
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

    /**
     * @deprecated
     */
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

    public boolean getState() {
        return super.getModel().isSelected();
    }

    public void setState(boolean state) {
        super.getModel().setSelected(state);
    }

    public void addListener(ItemListener l) {
        super.getModel().addItemListener(l);
    }

    public void removeListener(ItemListener l) {
        super.getModel().removeItemListener(l);
    }

    public void updateLocale() {
        setText(path, variables);
    }

    private void init() {
        setForeground(Theme.getTheme().getForeground());
        setBackground(Theme.getTheme().getBackground());
        setFont(getFont().deriveFont(TLauncherFrame.getFontSize()));
        setOpaque(false);
    }
}
