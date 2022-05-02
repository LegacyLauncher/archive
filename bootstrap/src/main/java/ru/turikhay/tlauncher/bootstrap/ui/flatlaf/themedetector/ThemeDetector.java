package ru.turikhay.tlauncher.bootstrap.ui.flatlaf.themedetector;

import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration.Theme;
import ru.turikhay.tlauncher.bootstrap.util.U;

public abstract class ThemeDetector {
    protected abstract Theme doDetectTheme();

    public static Theme detectTheme() {
        return new ThemeDetectorImpl().doDetectTheme();
    }

    protected static void log(Object... o) {
        U.log("[ThemeDetector]", o);
    }
}
