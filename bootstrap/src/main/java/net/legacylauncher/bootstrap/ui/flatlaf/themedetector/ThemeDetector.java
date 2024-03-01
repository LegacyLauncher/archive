package net.legacylauncher.bootstrap.ui.flatlaf.themedetector;


import net.legacylauncher.portals.Portals;
import net.legacylauncher.util.shared.FlatLafConfiguration;

public final class ThemeDetector {
    private ThemeDetector() {
    }

    public static FlatLafConfiguration.Theme detectTheme() {
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
