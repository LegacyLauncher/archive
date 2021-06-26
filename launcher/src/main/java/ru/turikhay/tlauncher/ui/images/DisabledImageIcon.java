package ru.turikhay.tlauncher.ui.images;

import javax.swing.*;
import java.awt.*;

public class DisabledImageIcon implements Icon {
    private final Icon parent;

    private float disabledOpacity;
    private AlphaComposite opacityComposite;

    public DisabledImageIcon(Icon parent, float opacity) {
        if (parent == null)
            throw new NullPointerException();

        this.parent = parent;
        setDisabledOpacity(opacity);
    }

    public DisabledImageIcon(Icon parent) {
        this(parent, 0.5f);
    }

    public final float getDisabledOpacity() {
        return disabledOpacity;
    }

    public final void setDisabledOpacity(float f) {
        if (f < 0.0)
            throw new IllegalArgumentException();

        disabledOpacity = f;
        opacityComposite = AlphaComposite.getInstance(3, disabledOpacity);
    }

    public void paintIcon(Component c, Graphics g0, int x, int y) {
        Graphics2D g = (Graphics2D) g0;
        Composite oldComposite = g.getComposite();
        g.setComposite(opacityComposite);
        parent.paintIcon(c, g, x, y);
        g.setComposite(oldComposite);
    }

    @Override
    public final int getIconWidth() {
        return parent.getIconWidth();
    }

    @Override
    public final int getIconHeight() {
        return parent.getIconHeight();
    }
}
