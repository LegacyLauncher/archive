package net.legacylauncher.portals;

import com.jthemedetecor.OsThemeDetector;

@SuppressWarnings("unused") // multi-release override
public class JVMPortalColorSchemeDetector {
    public static Portal.ColorScheme getColorScheme() {
        OsThemeDetector detector = OsThemeDetector.getDetector();
        return detector.isDark() ? Portal.ColorScheme.PREFER_DARK : Portal.ColorScheme.PREFER_LIGHT;
    }
}
