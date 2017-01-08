package ru.turikhay.tlauncher.ui.center;

import java.awt.*;

public class TipPanelTheme extends DefaultCenterPanelTheme {
    {
        borderColor = Color.getHSBColor(0.0F, 0.3F, 1.0F);
    }

    public Color getBorder() {
        return borderColor;
    }
}
