package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

public class VPanel extends ExtendedPanel {
    private VPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        setMagnifyGaps(true);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    public VPanel() {
        this(true);
    }

    protected void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);
        if (magnify) {
            super.addImpl(new Gap(comp), null, index);
        }
        checkMagnifyGaps();
    }

    public void remove(int index) {
        boolean wasGap = getComponent(index) instanceof Gap;

        super.remove(index);

        if (wasGap) {
            checkMagnifyGaps();
        }
    }

    public final BoxLayout getLayout() {
        return (BoxLayout) super.getLayout();
    }

    public final void setLayout(LayoutManager mgr) {
        if (mgr instanceof BoxLayout) {
            int axis = ((BoxLayout) mgr).getAxis();
            if (axis != BoxLayout.PAGE_AXIS && axis != BoxLayout.Y_AXIS) {
                throw new IllegalArgumentException("Illegal BoxLayout axis!");
            }
            super.setLayout(mgr);
        }
    }

    private boolean magnify;
    private final Hashtable<Component, Gap> emptyPanelMap = new Hashtable<>();

    public final boolean getMagnifyGaps() {
        return magnify;
    }

    public final void setMagnifyGaps(boolean magnify) {
        this.magnify = !OS.WINDOWS.isCurrent() && TLauncherFrame.magnifyDimensions > 1. && magnify;
        checkMagnifyGaps();
    }

    protected final void checkMagnifyGaps() {
        HashMap<Component, Gap> searching = new HashMap<>(emptyPanelMap);

        if (magnify) {
            Iterator<Component> compI = searching.keySet().iterator();
            while (compI.hasNext()) {
                Component compE = compI.next();

                for (Component comp : getComponents()) {
                    if (comp == compE) {
                        compI.remove();
                        break;
                    }
                }
            }
        }

        for (Gap removal : searching.values()) {
            remove(removal);
        }
    }

    private class Gap extends JComponent {
        private final WeakReference<Component> comp;

        Gap(Component comp) {
            emptyPanelMap.put(comp, this);

            this.comp = new WeakReference<>(comp);
            setOpaque(false);
            //setBackground(Color.green);

            Dimension size = new Dimension(1, SwingUtil.magnify(2));
            setMinimumSize(size);
            setPreferredSize(size);
        }

        void check() {
            final Component comp = this.comp.get();
            if (comp == null || comp.getParent() != VPanel.this) {
                VPanel.super.remove(this);
            }
        }
    }
}
