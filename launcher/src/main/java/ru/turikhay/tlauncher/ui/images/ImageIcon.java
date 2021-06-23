package ru.turikhay.tlauncher.ui.images;

import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.Lazy;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class ImageIcon extends ExtendedLabel implements ExtendedIcon {
    private final Image image;
    private final int size;

    ImageIcon(Image image, int size) {
        this.image = Objects.requireNonNull(image, "image");
        this.size = size;
    }

    private final Lazy<DisabledImageIcon> disabledInstance = Lazy.of(() -> new DisabledImageIcon(this));
    @Override
    public DisabledImageIcon getDisabledInstance() {
        return disabledInstance.get();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(image, x, y, null);
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
        if(component == null) {
            return null;
        }

        if(component instanceof JLabel) {
            ((JLabel) component).setIcon(icon);
            ((JLabel) component).setDisabledIcon(icon == null?  null : icon.getDisabledInstance());
        } else if(component instanceof AbstractButton) {
            ((AbstractButton) component).setIcon(icon);
            ((AbstractButton) component).setDisabledIcon(icon == null? null : icon.getDisabledInstance());
        } else {
            throw new IllegalArgumentException();
        }

        return icon;
    }
}
