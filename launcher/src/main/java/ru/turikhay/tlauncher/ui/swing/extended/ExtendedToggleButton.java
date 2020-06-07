package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.theme.Theme;

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
