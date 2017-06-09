package ru.turikhay.tlauncher.ui.swing;

import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Multipane extends ExtendedPanel {

    private final Map<String, Component> compMap = new HashMap<>();
    private final CardLayout layout;

    public Multipane() {
        this.layout = new CardLayout(0, 0);
        setLayout(layout);
    }

    public Component add(Component comp) {
        add(comp, (String) null);
        return comp;
    }

    public void add(Component comp, Object key) {
        if(key instanceof String) {
            add(comp, (String) key);
        } else {
            throw new IllegalArgumentException("key is not a String");
        }
    }

    public void add(Component comp, String key) {
        Component old = compMap.get(key);
        if(old != null) {
            super.remove(old);
        }
        super.add(comp, key);
        compMap.put(key, comp);
    }

    public void show(String key) {
        Component comp = compMap.get(key);
        if(comp == null) {
            throw new IllegalArgumentException("component missing: " + key);
        }
        layout.show(this, key);
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        if(mgr instanceof CardLayout) {
            super.setLayout(mgr);
        }
    }
}
