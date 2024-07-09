package net.legacylauncher.portals;

import com.jthemedetecor.OsThemeDetector;

import java.util.function.Consumer;

@SuppressWarnings("unused") // multi-release override
public class JVMPortalColorSchemeDetector {
    public static Portal.ColorScheme getColorScheme() {
        OsThemeDetector detector = OsThemeDetector.getDetector();
        return detector.isDark() ? Portal.ColorScheme.PREFER_DARK : Portal.ColorScheme.PREFER_LIGHT;
    }

    public static AutoCloseable subscribeForColorSchemeChanges(Consumer<Portal.ColorScheme> callback) {
        OsThemeDetector detector = OsThemeDetector.getDetector();
        Consumer<Boolean> detectorListener = (isDarkTheme) -> {
            callback.accept(isDarkTheme ? Portal.ColorScheme.PREFER_DARK : Portal.ColorScheme.PREFER_LIGHT);
        };
        detector.registerListener(detectorListener);
        return () -> detector.removeListener(detectorListener);
    }
}
