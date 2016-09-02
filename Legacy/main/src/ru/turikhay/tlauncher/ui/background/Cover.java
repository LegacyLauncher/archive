package ru.turikhay.tlauncher.ui.background;

import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;

import javax.swing.*;
import java.awt.*;

final class Cover extends JComponent implements ResizeableComponent {
    private Color color;
    private float opacity;

    Cover() {
        setColor(null);
        setOpacity(0.0f);
    }

    void setColor(Color color) {
        this.color = color == null ? Color.black : color;
    }

    float getOpacity() {
        return opacity;
    }

    void setOpacity(float opacity) {
        if (opacity < 0.f) {
            opacity = 0.f;
        } else if (opacity > 1.f) {
            opacity = 1.f;
        }
        this.opacity = opacity;
        repaint();
    }

    @Override
    public void paint(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;

        g.setComposite(AlphaComposite.getInstance(3, opacity));

        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void onResize() {
        if (getParent() != null) {
            setSize(getParent().getSize());
        }
    }
}
