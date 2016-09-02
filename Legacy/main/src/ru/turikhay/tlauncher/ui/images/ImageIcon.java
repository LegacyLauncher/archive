package ru.turikhay.tlauncher.ui.images;

import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;

import javax.swing.*;
import java.awt.*;

public class ImageIcon extends ExtendedLabel implements ExtendedIcon {
    private transient Image image;
    private int width;
    private int height;
    private DisabledImageIcon disabledInstance;

    public ImageIcon(Image image, int width, int height) {
        setImage(image, width, height);
    }

    public ImageIcon(Image image, int size, boolean ifTrueWidthElseHeight) {
        if (image == null) {
            setImage(null, 0, 0);
            setIconSize(width, width);
            return;
        }

        double ratio = (double) image.getWidth(null) / image.getHeight(null);

        if (ifTrueWidthElseHeight) {
            setImage(image, size, (int) (size / ratio));
        } else {
            setImage(image, (int) (size * ratio), size);
        }
    }

    public ImageIcon(Image image) {
        this(image, 0, 0);
    }

    public void setImage(Image image, int preferredWidth, int preferredHeight) {
        if (image == null) {
            this.image = null;
            setIcon(null);
        } else {
            this.image = image;
            setIconSize(preferredWidth, preferredHeight);
            setIcon(this);
        }
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        }
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }

    public void setIconSize(int width, int height) {
        this.width = width > 0 ? width : image == null ? 0 : image.getWidth(null);
        this.height = height > 0 ? height : image == null ? 0 : image.getHeight(null);

        if (image != null) {
            image = image.getScaledInstance(this.width, this.height, Image.SCALE_SMOOTH);
        }
    }

    public DisabledImageIcon getDisabledInstance() {
        if (disabledInstance == null) {
            disabledInstance = new DisabledImageIcon(this);
        }

        return disabledInstance;
    }

    public static ImageIcon setup(JLabel label, ImageIcon icon) {
        if (label == null) {
            return null;
        } else {
            label.setIcon(icon);
            if (icon != null) {
                label.setDisabledIcon(icon.getDisabledInstance());
            }

            return icon;
        }
    }
}