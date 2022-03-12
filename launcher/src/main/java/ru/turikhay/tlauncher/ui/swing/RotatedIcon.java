package ru.turikhay.tlauncher.ui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class RotatedIcon implements Icon {
    private final Icon icon;
    private final RotatedIcon.Rotate rotate;
    private double angle;

    public RotatedIcon(Icon icon) {
        this(icon, RotatedIcon.Rotate.UP);
    }

    private RotatedIcon(Icon icon, RotatedIcon.Rotate rotate) {
        this.icon = icon;
        this.rotate = rotate;
    }

    public RotatedIcon(Icon icon, double angle) {
        this(icon, RotatedIcon.Rotate.ABOUT_CENTER);
        this.angle = angle;
    }

    public Icon getIcon() {
        return icon;
    }

    public RotatedIcon.Rotate getRotate() {
        return rotate;
    }

    public double getAngle() {
        return angle;
    }

    public int getIconWidth() {
        if (rotate == RotatedIcon.Rotate.ABOUT_CENTER) {
            double radians = Math.toRadians(angle);
            double sin = Math.abs(Math.sin(radians));
            double cos = Math.abs(Math.cos(radians));
            return (int) Math.floor((double) icon.getIconWidth() * cos + (double) icon.getIconHeight() * sin);
        } else {
            return rotate == RotatedIcon.Rotate.UPSIDE_DOWN ? icon.getIconWidth() : icon.getIconHeight();
        }
    }

    public int getIconHeight() {
        if (rotate == RotatedIcon.Rotate.ABOUT_CENTER) {
            double radians = Math.toRadians(angle);
            double sin = Math.abs(Math.sin(radians));
            double cos = Math.abs(Math.cos(radians));
            return (int) Math.floor((double) icon.getIconHeight() * cos + (double) icon.getIconWidth() * sin);
        } else {
            return rotate == RotatedIcon.Rotate.UPSIDE_DOWN ? icon.getIconHeight() : icon.getIconWidth();
        }
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        int cWidth = icon.getIconWidth() / 2;
        int cHeight = icon.getIconHeight() / 2;
        int xAdjustment = icon.getIconWidth() % 2 == 0 ? 0 : -1;
        int yAdjustment = icon.getIconHeight() % 2 == 0 ? 0 : -1;
        if (rotate == RotatedIcon.Rotate.DOWN) {
            g2.translate(x + cHeight, y + cWidth);
            g2.rotate(Math.toRadians(90.0D));
            icon.paintIcon(c, g2, -cWidth, yAdjustment - cHeight);
        } else if (rotate == RotatedIcon.Rotate.UP) {
            g2.translate(x + cHeight, y + cWidth);
            g2.rotate(Math.toRadians(-90.0D));
            icon.paintIcon(c, g2, xAdjustment - cWidth, -cHeight);
        } else if (rotate == RotatedIcon.Rotate.UPSIDE_DOWN) {
            g2.translate(x + cWidth, y + cHeight);
            g2.rotate(Math.toRadians(180.0D));
            icon.paintIcon(c, g2, xAdjustment - cWidth, yAdjustment - cHeight);
        } else if (rotate == RotatedIcon.Rotate.ABOUT_CENTER) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform original = g2.getTransform();
            AffineTransform at = new AffineTransform();
            at.concatenate(original);
            at.translate((getIconWidth() - icon.getIconWidth()) / 2f, (getIconHeight() - icon.getIconHeight()) / 2f);
            at.rotate(Math.toRadians(angle), x + cWidth, y + cHeight);
            g2.setTransform(at);
            icon.paintIcon(c, g2, x, y);
            g2.setTransform(original);
        }

    }

    public enum Rotate {
        DOWN,
        UP,
        UPSIDE_DOWN,
        ABOUT_CENTER
    }
}
