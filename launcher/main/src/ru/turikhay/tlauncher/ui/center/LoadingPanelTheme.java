package ru.turikhay.tlauncher.ui.center;

import ru.turikhay.util.U;

import java.awt.*;

public class LoadingPanelTheme extends DefaultCenterPanelTheme {
    //protected final Color panelBackgroundColor = new Color(255, 255, 255, 168)
    {
        panelBackgroundColor = U.shiftAlpha(panelBackgroundColor, 40, 64, 176);
    }

    public Color getPanelBackground() {
        return panelBackgroundColor;
    }
}
