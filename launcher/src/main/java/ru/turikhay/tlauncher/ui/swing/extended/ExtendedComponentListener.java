package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public abstract class ExtendedComponentListener implements ComponentListener {
    private final Component comp;
    private final QuickParameterListenerThread resizeListener;
    private final QuickParameterListenerThread moveListener;
    private ComponentEvent lastResizeEvent;
    private ComponentEvent lastMoveEvent;

    public ExtendedComponentListener(Component component, int tick) {
        if (component == null) {
            throw new NullPointerException();
        } else {
            comp = component;
            resizeListener = new QuickParameterListenerThread(() -> new int[]{comp.getWidth(), comp.getHeight()}, () -> onComponentResized(lastResizeEvent), tick);
            moveListener = new QuickParameterListenerThread(() -> {
                Point location = comp.getLocation();
                return new int[]{location.x, location.y};
            }, () -> onComponentMoved(lastMoveEvent), tick);
        }
    }

    public ExtendedComponentListener(Component component) {
        this(component, 500);
    }

    public final void componentResized(ComponentEvent e) {
        onComponentResizing(e);
        resizeListener.startListening();
    }

    public final void componentMoved(ComponentEvent e) {
        onComponentMoving(e);
        moveListener.startListening();
    }

    public boolean isListening() {
        return resizeListener.isIterating() || moveListener.isIterating();
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
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

    public void dispose() {
        moveListener.dispose();
        resizeListener.dispose();
    }
}
