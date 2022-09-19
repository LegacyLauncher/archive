package ru.turikhay.tlauncher.bootstrap.ui.flatlaf.themedetector;

import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration;
import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration.Theme;
import ru.turikhay.tlauncher.portals.Portals;

public final class ThemeDetector {
    private ThemeDetector() {
    }

    public static Theme detectTheme() {
        switch (Portals.getPortal().getColorScheme()) {
            case PREFER_LIGHT:
            case NO_PREFERENCE:
            default:
                return FlatLafConfiguration.Theme.LIGHT;
            case PREFER_DARK:
                return FlatLafConfiguration.Theme.DARK;
        }
    }
}
