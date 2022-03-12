package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.*;

public class BorderPanel extends ExtendedPanel {
    private static final long serialVersionUID = -7641580330557833990L;

    private Component north, east, south, west, center;

    private BorderPanel(BorderLayout layout, boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        if (layout == null) {
            layout = new BorderLayout();
        }
        setLayout(layout);
    }

    public BorderPanel() {
        this(null, true);
    }

    public BorderPanel(int hgap, int vgap) {
        this();
        setHgap(hgap);
        setVgap(vgap);
    }

    public BorderLayout getLayout() {
        return (BorderLayout) super.getLayout();
    }

    public void setLayout(LayoutManager mgr) {
        if (mgr instanceof BorderLayout) {
            super.setLayout(mgr);
        }

    }

    public int getHgap() {
        return getLayout().getHgap();
    }

    public void setHgap(int hgap) {
        getLayout().setHgap(hgap);
    }

    public int getVgap() {
        return getLayout().getVgap();
    }

    public void setVgap(int vgap) {
        getLayout().setVgap(vgap);
    }

    private Component set(Component oldComp, Component comp, String location) {
        if (comp == null) {
            if (oldComp != null) {
                remove(oldComp);
            }
            return null;
        }
        if (oldComp == comp) {
            return comp;
        }
        add(comp, location);
        return comp;
    }

    public void setNorth(Component comp) {
        north = set(north, comp, "North");
    }

    public void setEast(Component comp) {
        east = set(east, comp, "East");
    }

    public void setSouth(Component comp) {
        south = set(south, comp, "South");
    }

    public void setWest(Component comp) {
        west = set(west, comp, "West");
    }

    public void setCenter(Component comp) {
        center = set(center, comp, "Center");
    }
}
