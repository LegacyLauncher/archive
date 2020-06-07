package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.swing.util.Orientation;

import javax.swing.plaf.TabbedPaneUI;

public class WinTabbedPane extends TabbedPane {
    public WinTabbedPane(Orientation orientation, TabLayout layout) {
        super(orientation, layout);

        TabbedPaneUI ui = getUI();
        if (ui instanceof com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI) {
            setUI(new WindowsTabbedPaneExtendedUI());
        }
    }

    public WinTabbedPane() {
        this(null, null);
    }
}
