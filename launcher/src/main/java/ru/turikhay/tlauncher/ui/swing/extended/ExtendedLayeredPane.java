package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.block.BlockableLayeredPane;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public abstract class ExtendedLayeredPane extends BlockableLayeredPane implements ResizeableComponent {
    private static final long serialVersionUID = -1L;
    private int layerCount = 0;
    protected final JComponent parent;

    protected ExtendedLayeredPane() {
        parent = null;
    }

    protected ExtendedLayeredPane(JComponent parent) {
        this.parent = parent;

        if (parent != null) {
            parent.addComponentListener(new ComponentListener() {
                public void componentResized(ComponentEvent e) {
                    onResize();
                }

                public void componentMoved(ComponentEvent e) {
                }

                public void componentShown(ComponentEvent e) {
                    onResize();
                }

                public void componentHidden(ComponentEvent e) {
                }
            });
        }
    }

    public Component add(Component comp) {
        super.add(comp, (Integer) layerCount++);
        return comp;
    }

    public void add(Component... components) {
        if (components == null) {
            throw new NullPointerException();
        } else {

            for (Component comp : components) {
                add(comp);
            }

        }
    }

    public void onResize() {
        if (parent != null) {
            setSize(parent.getWidth(), parent.getHeight());

            for (Component comp : getComponents()) {
                if (comp instanceof ResizeableComponent) {
                    ((ResizeableComponent) comp).onResize();
                }
            }
        }
    }
}
