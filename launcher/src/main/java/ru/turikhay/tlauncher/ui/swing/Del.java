package ru.turikhay.tlauncher.ui.swing;

import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import java.awt.*;

public class Del extends ExtendedPanel {
    public static final int SIZE = 15;

    private static final int TOP = -1;
    public static final int CENTER = 0;
    private static final int BOTTOM = 1;
    private static final long serialVersionUID = -2252007533187803762L;
    private final int size;
    private final int aligment;
    private final Color color;

    public Del(int size, int aligment, Color color) {
        this.size = SwingUtil.magnify(size);
        this.aligment = aligment;
        this.color = color;
        setMaximumSize(new Dimension(Integer.MAX_VALUE, size + SIZE));
    }

    public Del(int size, int aligment, int width, int height, Color color) {
        this.size = SwingUtil.magnify(size);
        this.aligment = aligment;
        this.color = color;
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    public void paint(Graphics g) {
        g.setColor(color);
        switch (aligment) {
            case TOP:
                g.fillRect(0, 0, getWidth(), size);
                break;
            case CENTER:
                g.fillRect(0, getHeight() / 2 - size, getWidth(), size);
                break;
            case BOTTOM:
                g.fillRect(0, getHeight() - size, getWidth(), size);
        }

    }
}
