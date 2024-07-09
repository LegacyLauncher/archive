package net.legacylauncher.ui.swing.extended;

import net.legacylauncher.ui.LegacyLauncherFrame;
import net.legacylauncher.ui.theme.Theme;

import javax.swing.*;

public class ExtendedToggleButton extends JToggleButton {

    public ExtendedToggleButton() {
        init();
    }

    public ExtendedToggleButton(Icon icon) {
        super(icon);
        init();
    }

    private void init() {
        setFont(getFont().deriveFont(LegacyLauncherFrame.getFontSize()));
        setOpaque(false);
    }

    @Override
    public void updateUI() {
        Theme.setup(this);
        super.updateUI();
    }
}
