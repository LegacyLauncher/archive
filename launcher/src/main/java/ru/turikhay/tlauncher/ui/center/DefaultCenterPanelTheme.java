package ru.turikhay.tlauncher.ui.center;

import ru.turikhay.tlauncher.ui.theme.Theme;

import java.awt.*;

public class DefaultCenterPanelTheme implements CenterPanelTheme {
    public DefaultCenterPanelTheme() {
    }

    public Color getBackground() {
        return Theme.getTheme().getBackground();
    }

    public Color getPanelBackground() {
        return Theme.getTheme().getPanelBackground();
    }

    public Color getFocus() {
        return Theme.getTheme().getForeground();
    }

    public Color getFocusLost() {
        return Theme.getTheme().getSemiForeground();
    }

    public Color getSuccess() {
        return Theme.getTheme().getSuccess();
    }

    public Color getFailure() {
        return Theme.getTheme().getFailure();
    }

    public Color getBorder() {
        return Theme.getTheme().getBorder(getBorderType());
    }

    public Color getShadow() {
        return Theme.getTheme().getShadow(getBorderType());
    }

    public int getArc() {
        return Theme.getTheme().getArc(getBorderType());
    }

    protected Theme.Border getBorderType() {
        return Theme.Border.MAIN_PANEL;
    }
}
