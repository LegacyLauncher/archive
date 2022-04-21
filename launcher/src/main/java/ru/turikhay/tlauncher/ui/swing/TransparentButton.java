package ru.turikhay.tlauncher.ui.swing;

import javax.swing.*;
import java.awt.*;

public class TransparentButton extends JButton {
    private static final long serialVersionUID = -5329305793566047719L;

    protected TransparentButton() {
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.white);
        setPreferredSize(new Dimension(27, 27));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public TransparentButton(String text) {
        this();
        setText(text);
    }

    protected void paintComponent(Graphics g) {
        ButtonModel buttonModel = getModel();
        Graphics2D gd = (Graphics2D) g.create();
        gd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gd.setPaint(new GradientPaint(0.0F, 0.0F, Color.decode("#67c7f4"), 0.0F, (float) getHeight(), Color.decode("#379fc9")));
        if (buttonModel.isRollover()) {
            gd.setPaint(new GradientPaint(0.0F, 0.0F, Color.decode("#7bd2f6"), 0.0F, (float) getHeight(), Color.decode("#43b3d5")));
            if (buttonModel.isPressed()) {
                gd.setPaint(new GradientPaint(0.0F, 0.0F, Color.decode("#379fc9"), 0.0F, (float) getHeight(), Color.decode("#4fb2dd")));
            } else {
                setForeground(Color.white);
            }
        }

        gd.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        gd.dispose();
        super.paintComponent(g);
    }
}
