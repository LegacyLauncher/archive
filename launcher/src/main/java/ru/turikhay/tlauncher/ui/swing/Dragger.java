package ru.turikhay.tlauncher.ui.swing;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class Dragger extends BorderPanel implements LocalizableComponent {
    private static final List<SoftReference<Dragger>> draggers = new ArrayList<>();
    private static final Color enabledColor = new Color(0, 0, 0, 32);
    private static final Color disabledColor = new Color(0, 0, 0, 16);
    private static Configuration config;
    private static Point maxPoint;
    private static boolean ready;
    private final JComponent parent;
    private final String key;
    private final ExtendedLabel label;
    private String tooltip;

    public Dragger(JComponent parent, String name) {
        if (parent == null) {
            throw new NullPointerException("parent");
        } else if (name == null) {
            throw new NullPointerException("name");
        } else if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name is empty");
        } else {
            this.parent = parent;
            key = "dragger." + name;
            DraggerMouseListener listener = new DraggerMouseListener();
            setCursor(SwingUtil.getCursor(13));
            label = new ExtendedLabel();
            label.addMouseListener(listener);
            setCenter(label);
            if (!ready) {
                draggers.add(new SoftReference<>(this));
            }

            setEnabled(true);
        }
    }

    public void paint(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setColor(isEnabled() ? enabledColor : disabledColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    public void setEnabled(boolean b) {
        tooltip = b ? "dragger.label" : null;
        updateLocale();
        super.setEnabled(b);
    }

    private void dragComponent(int x, int y) {
        if (ready) {
            if (x + parent.getWidth() > maxPoint.x) {
                x = maxPoint.x - parent.getWidth();
            }

            if (x < 0) {
                x = 0;
            }

            if (y + parent.getHeight() > maxPoint.y) {
                y = maxPoint.y - parent.getHeight();
            }

            if (y < 0) {
                y = 0;
            }

            parent.setLocation(x, y);
            config.set(key, new IntegerArray(x, y));
        }
    }

    public void updateCoords() {
        dragComponent(parent.getX(), parent.getY());
    }

    public void loadCoords() {
        IntegerArray arr;
        try {
            arr = IntegerArray.parseIntegerArray(config.get(key));
            if (arr.size() != 2) {
                throw new IllegalArgumentException("illegal size");
            }
        } catch (Exception var3) {
            var3.printStackTrace();
            return;
        }

        dragComponent(arr.get(0), arr.get(1));
    }

    private void ready() {
        updateLocale();
        loadCoords();
    }

    public void updateLocale() {
        label.setToolTipText(Localizable.get(tooltip));
    }

    public static synchronized void ready(Configuration config, Point maxPoint) {
        if (!ready) {
            if (config == null) {
                throw new NullPointerException("config");
            } else if (maxPoint == null) {
                throw new NullPointerException("maxPoint");
            } else {
                ready = true;

                for (SoftReference<Dragger> draggerRef : draggers) {
                    Dragger dragger = draggerRef.get();
                    if (dragger != null) {
                        dragger.ready();
                    }
                }
            }
        }
    }

    public static synchronized void update() {
        for (SoftReference<Dragger> draggerRef : draggers) {
            Dragger dragger = draggerRef.get();
            if (dragger != null) {
                dragger.updateCoords();
            }
        }
    }

    public class DraggerMouseListener extends MouseAdapter {
        private final int[] startPoint = new int[2];

        public void mousePressed(MouseEvent e) {
            if (isEnabled()) {
                startPoint[0] = e.getX();
                startPoint[1] = e.getY();
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (isEnabled()) {
                dragComponent(parent.getX() + e.getX() - startPoint[0], parent.getY() + e.getY() - startPoint[1]);
            }
        }
    }
}
