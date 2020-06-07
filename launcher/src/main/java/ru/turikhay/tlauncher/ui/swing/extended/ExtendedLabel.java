package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

public class ExtendedLabel extends JLabel {
    private static final AlphaComposite disabledAlphaComposite = AlphaComposite.getInstance(3, 0.5F);

    public ExtendedLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        Theme.setup(this);
        setFont(getFont().deriveFont(TLauncherFrame.getFontSize()));
        setOpaque(false);
    }

    public ExtendedLabel(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }

    public ExtendedLabel(String text) {
        this(text, null, 10);
    }

    public ExtendedLabel(Icon image, int horizontalAlignment) {
        this(null, image, horizontalAlignment);
    }

    public ExtendedLabel(Icon image) {
        this(null, image, 0);
    }

    public ExtendedLabel() {
        this(null, null, 10);
    }

    protected void setRawText(String value) {
        super.setText(value);
    }

    public void setIcon(ImageIcon icon) {
        super.setIcon(icon);
        super.setDisabledIcon(icon == null ? null : icon.getDisabledInstance());
    }

    public void paintComponent(Graphics g0) {
        if (isEnabled()) {
            super.paintComponent(g0);
        } else {
            Graphics2D g = (Graphics2D) g0;
            Composite oldComposite = g.getComposite();
            g.setComposite(disabledAlphaComposite);
            super.paintComponent(g);
            g.setComposite(oldComposite);
        }
    }
}
