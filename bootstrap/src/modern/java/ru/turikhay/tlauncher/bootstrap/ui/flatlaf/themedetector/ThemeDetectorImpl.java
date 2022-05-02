package ru.turikhay.tlauncher.bootstrap.ui.flatlaf.themedetector;

import com.jthemedetecor.OsThemeDetector;
import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration;
import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration.Theme;

public class ThemeDetectorImpl extends ThemeDetector {
    @Override
    public Theme doDetectTheme() {
        log("Using OsThemeDetector");
        OsThemeDetector detector = OsThemeDetector.getDetector();
        Theme theme = detector.isDark() ? FlatLafConfiguration.Theme.DARK : FlatLafConfiguration.Theme.LIGHT;
        log("Detected theme:", theme);
        return theme;
    }
}
