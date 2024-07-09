package net.legacylauncher.ui.swing.extended;

import net.legacylauncher.ui.LegacyLauncherFrame;
import net.legacylauncher.ui.theme.Theme;

import javax.swing.*;
import java.awt.event.ItemListener;

public class ExtendedCheckbox extends JCheckBox {

    public ExtendedCheckbox() {
        setFont(getFont().deriveFont(LegacyLauncherFrame.getFontSize()));
        setOpaque(false);
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

    @Override
    public void updateUI() {
        setForeground(Theme.getTheme().getForeground());
        setBackground(Theme.getTheme().getBackground());
        super.updateUI();
    }
}
