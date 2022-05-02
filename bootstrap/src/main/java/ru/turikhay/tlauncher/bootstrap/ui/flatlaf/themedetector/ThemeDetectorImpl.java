package ru.turikhay.tlauncher.bootstrap.ui.flatlaf.themedetector;

import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration.Theme;

public class ThemeDetectorImpl extends ThemeDetector {
    private static final Theme DEFAULT = Theme.LIGHT;

    @Override
    public Theme doDetectTheme() {
        log("Using fallback theme:", DEFAULT);
        return DEFAULT;
    }
}
