package ru.turikhay.tlauncher.ui.images;

import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.U;

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
        setImage(image, size, ifTrueWidthElseHeight);
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

    public void setImage(Image image, int size, boolean ifTrueWidthElseHeight) {
        double ratio = (double) image.getWidth(null) / image.getHeight(null);
        if (ifTrueWidthElseHeight) {
            setImage(image, size, (int) (size / ratio));
        } else {
            setImage(image, (int) (size * ratio), size);
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

    public void setIconHeight(int height) {
        double ratio = (double) this.width / this.height;
        setIconSize((int) (ratio * height), height);
    }

    public DisabledImageIcon getDisabledInstance() {
        if (disabledInstance == null) {
            disabledInstance = new DisabledImageIcon(this);
        }

        return disabledInstance;
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
