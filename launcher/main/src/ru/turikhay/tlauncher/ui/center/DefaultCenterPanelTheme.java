package ru.turikhay.tlauncher.ui.center;

import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;

public class DefaultCenterPanelTheme extends CenterPanelTheme {
    private final JLabel label = new JLabel();

    protected Color backgroundColor = label.getBackground(); //new Color(255, 255, 255, 255);
    protected Color panelBackgroundColor = U.shiftAlpha(backgroundColor, -128, 64, 192); //new Color(255, 255, 255, 128);
    protected Color focusColor = label.getForeground(); //new Color(0, 0, 0, 255);
    protected Color focusLostColor = U.shiftColor(focusColor, 96, 64, 192); //new Color(128, 128, 128, 255);
    protected Color successColor = new Color(78, 196, 78, 255);
    protected Color failureColor = Color.getHSBColor(0.0F, 1.0F, 0.7F);
    protected Color borderColor = new Color(28, 128, 28, 255);
    protected Color delPanelColor;

    public DefaultCenterPanelTheme() {
        delPanelColor = successColor;
    }

    public Color getBackground() {
        return backgroundColor;
    }

    public Color getPanelBackground() {
        return panelBackgroundColor;
    }

    public Color getFocus() {
        return focusColor;
    }

    public Color getFocusLost() {
        return focusLostColor;
    }

    public Color getSuccess() {
        return successColor;
    }

    public Color getFailure() {
        return failureColor;
    }

    public Color getBorder() {
        return borderColor;
    }

    public Color getDelPanel() {
        return delPanelColor;
    }
}
