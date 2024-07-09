package net.legacylauncher.ui.swing.extended;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public abstract class ExtendedComponentListener implements ComponentListener {
    private final Component comp;
    private final QuickParameterListener resizeListener;
    private final QuickParameterListener moveListener;

    public ExtendedComponentListener(Component component) {
        if (component == null) {
            throw new NullPointerException();
        } else {
            comp = component;
            resizeListener = new QuickParameterListener(() ->
                    new int[]{comp.getWidth(), comp.getHeight()},
                    this::onComponentResized
            );
            moveListener = new QuickParameterListener(() -> {
                Point location = comp.getLocation();
                return new int[]{location.x, location.y};
            }, this::onComponentMoved);
        }
    }

    public final void componentResized(ComponentEvent e) {
        onComponentResizing();
        resizeListener.startListening();
    }

    public final void componentMoved(ComponentEvent e) {
        onComponentMoving();
        moveListener.startListening();
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    public void onComponentResizing() {
    }

    public void onComponentResized() {
    }

    public void onComponentMoving() {
    }

    public void onComponentMoved() {
    }

    public void dispose() {
        moveListener.dispose();
        resizeListener.dispose();
    }
}
