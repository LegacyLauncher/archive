package ru.turikhay.tlauncher.ui.images;

import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.Lazy;

import javax.swing.*;
import java.awt.*;

public class ImageIcon extends ExtendedLabel implements ExtendedIcon {
    private Image image;
    private final int size;

    protected ImageIcon(Image image, int size) {
        this.image = image;
        this.size = size;
    }

    protected void setImage(Image image) {
        this.image = image;
        repaint();
    }

    private final Lazy<DisabledImageIcon> disabledInstance = Lazy.of(() -> new DisabledImageIcon(this));

    @Override
    public DisabledImageIcon getDisabledInstance() {
        return disabledInstance.get();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (image != null) {
            g.drawImage(image, x, y, null);
        }
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    public void setup(JComponent comp) {
        setup(comp, this);
    }

    public static ImageIcon setup(JComponent component, ImageIcon icon) {
        if (component == null) {
            return null;
        }

        if (component instanceof JLabel) {
            ((JLabel) component).setIcon(icon);
            ((JLabel) component).setDisabledIcon(icon == null ? null : icon.getDisabledInstance());
        } else if (component instanceof AbstractButton) {
            ((AbstractButton) component).setIcon(icon);
            ((AbstractButton) component).setDisabledIcon(icon == null ? null : icon.getDisabledInstance());
        } else {
            throw new IllegalArgumentException();
        }

        return icon;
    }
}
