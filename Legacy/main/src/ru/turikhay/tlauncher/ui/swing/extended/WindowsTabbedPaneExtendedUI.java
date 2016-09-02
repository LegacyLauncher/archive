package ru.turikhay.tlauncher.ui.swing.extended;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;
import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;

import javax.swing.*;
import java.awt.*;

public class WindowsTabbedPaneExtendedUI extends WindowsTabbedPaneUI implements ExtendedUI {
    public static final int ARC_SIZE = 16;
    public static final int Y_PADDING = 5;
    private CenterPanelTheme theme;

    public WindowsTabbedPaneExtendedUI(CenterPanelTheme theme) {
        this.theme = theme;
    }

    public WindowsTabbedPaneExtendedUI() {
        this(null);
    }

    public CenterPanelTheme getTheme() {
        return theme;
    }

    public void setTheme(CenterPanelTheme theme) {
        this.theme = theme;
    }

    protected void installDefaults() {
        super.installDefaults();
        contentBorderInsets = new Insets(7, 7, 7, 7);
        tabRunOverlay = 1;
        LookAndFeel.installProperty(tabPane, "opaque", Boolean.FALSE);
    }

    protected void paintContentBorder(Graphics g0, int tabPlacement, int selectedIndex) {
        Insets insets = tabPane.getInsets();
        Insets tabAreaInsets = UIManager.getInsets("TabbedPane.tabAreaInsets");
        int x = insets.left;
        int y = insets.top;
        int w = tabPane.getWidth() - insets.right - insets.left;
        int h = tabPane.getHeight() - insets.top - insets.bottom;
        int g;
        if (tabPlacement != 2 && tabPlacement != 4) {
            g = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
            if (tabPlacement == 1) {
                y += g - tabAreaInsets.bottom;
            }

            h -= g - tabAreaInsets.bottom;
        } else {
            g = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
            if (tabPlacement == 2) {
                x += g - tabAreaInsets.bottom;
            }

            w -= g - tabAreaInsets.bottom;
        }

        Graphics2D var14 = (Graphics2D) g0;
        var14.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color background;
        Color border;
        if (theme == null) {
            background = tabPane.getBackground();
            border = tabPane.getForeground();
        } else {
            background = theme.getPanelBackground();
            border = theme.getBorder();
        }

        var14.setColor(background);
        var14.fillRoundRect(x, y - 5, w, h + 5, 16, 16);
        var14.setColor(border);

        for (int i = 1; i < 2; ++i) {
            var14.drawRoundRect(x + i - 1, y + i - 5 - 1, w - 2 * i + 1, h - 2 * i + 1 + 5, 16, 16);
        }

    }
}
