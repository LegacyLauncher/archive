package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.*;

public class BorderPanel extends ExtendedPanel {
    private static final long serialVersionUID = -7641580330557833990L;

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

    public void setNorth(Component comp) {
        add(comp, "North");
    }

    public void setEast(Component comp) {
        add(comp, "East");
    }

    public void setSouth(Component comp) {
        add(comp, "South");
    }

    public void setWest(Component comp) {
        add(comp, "West");
    }

    public void setCenter(Component comp) {
        add(comp, "Center");
    }
}
