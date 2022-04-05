package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.*;
import java.awt.event.ComponentEvent;

public class ExtendedComponentAdapter extends ExtendedComponentListener {
    public ExtendedComponentAdapter(Component component, int tick) {
        super(component, tick);
    }

    public ExtendedComponentAdapter(Component component) {
        super(component);
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void onComponentResizing(ComponentEvent e) {
    }

    public void onComponentResized(ComponentEvent e) {
    }

    public void onComponentMoving(ComponentEvent e) {
    }

    public void onComponentMoved(ComponentEvent e) {
    }
}
