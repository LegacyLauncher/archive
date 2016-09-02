package ru.turikhay.tlauncher.ui.center;

import ru.turikhay.util.OS;

import java.awt.*;

public class SettingsPanelTheme extends DefaultCenterPanelTheme {
    //protected final Color panelBackgroundColor = new Color(255, 255, 255, 128);
    //protected final Color borderColor = OS.VERSION.startsWith("10.") ? new Color(217, 217, 217, 255) : new Color(172, 172, 172, 255);
    //protected final Color delPanelColor = new Color(50, 80, 190, 255);
    {
        borderColor = OS.VERSION.startsWith("10.") ? new Color(217, 217, 217, 255) : new Color(172, 172, 172, 255);
        delPanelColor = new Color(50, 80, 190, 255);
    }

    public Color getPanelBackground() {
        return panelBackgroundColor;
    }

    public Color getBorder() {
        return borderColor;
    }

    public Color getDelPanel() {
        return delPanelColor;
    }
}
