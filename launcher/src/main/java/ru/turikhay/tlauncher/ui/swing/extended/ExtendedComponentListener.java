package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.swing.util.IntegerArrayGetter;

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
            resizeListener = new QuickParameterListenerThread(new IntegerArrayGetter() {
                public int[] getIntegerArray() {
                    return new int[]{comp.getWidth(), comp.getHeight()};
                }
            }, new Runnable() {
                public void run() {
                    onComponentResized(lastResizeEvent);
                }
            }, tick);
            moveListener = new QuickParameterListenerThread(new IntegerArrayGetter() {
                public int[] getIntegerArray() {
                    Point location = comp.getLocation();
                    return new int[]{location.x, location.y};
                }
            }, new Runnable() {
                public void run() {
                    onComponentMoved(lastMoveEvent);
                }
            }, tick);
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

    public abstract void onComponentResizing(ComponentEvent var1);

    public abstract void onComponentResized(ComponentEvent var1);

    public abstract void onComponentMoving(ComponentEvent var1);

    public abstract void onComponentMoved(ComponentEvent var1);

    public void dispose() {
        moveListener.dispose();
        resizeListener.dispose();
    }
}
