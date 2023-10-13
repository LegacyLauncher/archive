package net.legacylauncher.ui.swing.extended;

import net.legacylauncher.ui.TLauncherFrame;
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
        Theme.setup(this);
        setFont(getFont().deriveFont(TLauncherFrame.getFontSize()));
        setOpaque(false);
    }
}
