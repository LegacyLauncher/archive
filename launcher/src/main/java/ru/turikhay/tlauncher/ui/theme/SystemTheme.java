package ru.turikhay.tlauncher.ui.theme;

import ru.turikhay.util.OS;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SystemTheme extends Theme {
    static final int MAX_ARC = 64, MAX_BORDER = 24, BLACK_MIN = 64, WHITE_MAX = 192;

    private static final SystemTheme instance = new SystemTheme();

    public static SystemTheme getSystemTheme() {
        return instance;
    }

    private final JLabel component;
    private final Map<Border, Color> borderColorMap;

    private final Color
            success = new Color(78, 196, 78),
            failure = new Color(179, 0, 0),
            shadow, semiForeground, panelBackground;

    private SystemTheme() {
        super("system");
        this.component = new JLabel();

        this.borderColorMap = new HashMap<>();
        borderColorMap.put(Border.MAIN_PANEL, new Color(28, 128, 28, 255));
        borderColorMap.put(Border.ADDITIONAL_PANEL, new Color(255, 177, 177));
        borderColorMap.put(Border.SETTINGS_PANEL, OS.VERSION.startsWith("10.") ? new Color(217, 217, 217, 255) : new Color(172, 172, 172, 255));

        assert borderColorMap.size() == Border.values().length;

        this.semiForeground = U.shiftColor(getForeground(), 96, 64, 192);
        this.panelBackground = U.shiftAlpha(getBackground(), -176, 64, 192);
        this.shadow = U.shiftAlpha(useDarkTheme() ? U.shiftColor(getForeground(), -96) : getBackground(), -150);
    }

    @Override
    public Color getForeground() {
        return component.getForeground();
    }

    @Override
    public Color getSemiForeground() {
        return semiForeground;
    }

    @Override
    public Color getBackground() {
        return component.getBackground();
    }

    @Override
    public Color getPanelBackground() {
        return panelBackground;
    }

    @Override
    public Color getSuccess() {
        return success;
    }

    @Override
    public Color getFailure() {
        return failure;
    }

    @Override
    public int getBorderSize() {
        return 2;
    }

    @Override
    public Color getBorder(Border border) {
        return borderColorMap.get(Objects.requireNonNull(border, "border"));
    }

    @Override
    public Color getShadow(Border border) {
        return shadow;
    }

    @Override
    public int getArc(Border border) {
        return border == Border.SETTINGS_PANEL ? 16 : 24;
    }

    @Override
    public Color getIconColor(String iconName) {
        return useColorfulIcons() ? ColorfulIcons.getColor(iconName) : getForeground();
    }

    @Override
    public boolean useDarkTheme() {
        return !useColorfulIcons();
    }

    private boolean useColorfulIcons() {
        if (Boolean.getBoolean("tlauncher.ui.noColorfulIcons") || UIManager.getBoolean("laf.dark")) {
            return false;
        }
        Color background = getBackground();
        return background.getRed() > BLACK_MIN || background.getGreen() > BLACK_MIN || background.getBlue() > BLACK_MIN;
    }
}
