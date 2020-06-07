package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.theme.Theme;

import javax.swing.*;
import java.awt.event.ItemListener;

public class ExtendedCheckbox extends JCheckBox {

    public ExtendedCheckbox() {
        init();
    }

    public ExtendedCheckbox(String text) {
        this();
        setText(text);
    }

    public ExtendedCheckbox(String text, boolean state) {
        this();
        setText(text);
        setState(state);
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

    private void init() {
        setForeground(Theme.getTheme().getForeground());
        setBackground(Theme.getTheme().getBackground());
        setFont(getFont().deriveFont(TLauncherFrame.getFontSize()));
        setOpaque(false);
    }
}
