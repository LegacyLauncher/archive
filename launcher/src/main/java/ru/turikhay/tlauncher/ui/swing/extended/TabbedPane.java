package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.swing.util.Orientation;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;

public class TabbedPane extends JTabbedPane {
    public TabbedPane(Orientation tabLocation, TabbedPane.TabLayout layout) {
        setTabLocation(tabLocation == null ? Orientation.TOP : tabLocation);
        setTabLayout(layout == null ? TabbedPane.TabLayout.SCROLL : layout);
        addChangeListener(e -> onTabChange(getSelectedIndex()));
    }

    public TabbedPane() {
        this(null, null);
    }

    public ExtendedUI getExtendedUI() {
        TabbedPaneUI ui = getUI();
        return ui instanceof ExtendedUI ? (ExtendedUI) ui : null;
    }

    public Orientation getTabLocation() {
        return Orientation.fromSwingConstant(getTabPlacement());
    }

    public void setTabLocation(Orientation direction) {
        if (direction == null) {
            throw new NullPointerException();
        } else {
            setTabPlacement(direction.getSwingAlias());
        }
    }

    public TabbedPane.TabLayout getTabLayout() {
        return TabbedPane.TabLayout.fromSwingConstant(getTabLayoutPolicy());
    }

    public void setTabLayout(TabbedPane.TabLayout layout) {
        if (layout == null) {
            throw new NullPointerException();
        } else {
            setTabLayoutPolicy(layout.getSwingAlias());
        }
    }

    public void onTabChange(int index) {
    }

    public enum TabLayout {
        WRAP(0),
        SCROLL(1);

        private final int swingAlias;

        TabLayout(int swingAlias) {
            this.swingAlias = swingAlias;
        }

        public int getSwingAlias() {
            return swingAlias;
        }

        public static TabbedPane.TabLayout fromSwingConstant(int orientation) {
            TabbedPane.TabLayout[] var4;
            int var3 = (var4 = values()).length;

            for (int var2 = 0; var2 < var3; ++var2) {
                TabbedPane.TabLayout current = var4[var2];
                if (orientation == current.getSwingAlias()) {
                    return current;
                }
            }

            return null;
        }
    }
}
