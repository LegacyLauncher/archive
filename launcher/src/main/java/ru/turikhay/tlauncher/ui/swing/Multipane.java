package ru.turikhay.tlauncher.ui.swing;

import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Multipane extends ExtendedPanel implements Scrollable {

    private final Map<String, Component> compMap = new HashMap<>();
    private final CardLayout layout;

    private Dimension scrollSize;

    public Multipane() {
        this.layout = new CardLayout(0, 0);
        setLayout(layout);
    }

    public Component add(Component comp) {
        add(comp, null);
        return comp;
    }

    public void add(Component comp, Object key) {
        if (key instanceof String) {
            add(comp, (String) key);
        } else {
            throw new IllegalArgumentException("key is not a String");
        }
    }

    public void add(Component comp, String key) {
        Component old = compMap.get(key);
        if (old != null) {
            super.remove(old);
        }
        super.add(comp, key);
        compMap.put(key, comp);
    }

    public void show(String key) {
        Component comp = compMap.get(key);
        if (comp == null) {
            throw new IllegalArgumentException("component missing: " + key);
        }
        layout.show(this, key);
        validate();
        repaint();
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        if (mgr instanceof CardLayout) {
            super.setLayout(mgr);
        }
    }

    public void setScrollSize(Dimension scrollSize) {
        this.scrollSize = scrollSize;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return scrollSize == null ? getPreferredSize() : scrollSize;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return SwingUtil.magnify(10);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
