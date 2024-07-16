package net.legacylauncher.ui.images;

import net.legacylauncher.ui.swing.extended.ExtendedLabel;
import net.legacylauncher.util.Lazy;

import javax.swing.*;
import java.awt.*;

public class ImageIcon extends ExtendedLabel implements ExtendedIcon {
    private Image image;
    private final String name;
    private final int size;

    protected ImageIcon(String name, int size) {
        this.name = name;
        this.size = size;
        this.updateIcon();
    }

    private final Lazy<DisabledImageIcon> disabledInstance = Lazy.of(() -> new DisabledImageIcon(this));

    @Override
    public DisabledImageIcon getDisabledInstance() {
        return disabledInstance.get();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (image != null) {
            g.drawImage(image, x, y, size, size, c);
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

    @Override
    public void updateUI() {
        if (this.name != null) {
            this.updateIcon();
        }
        super.updateUI();
    }

    private void updateIcon() {
        image = Images.loadIcon(this.name, this.size);
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
