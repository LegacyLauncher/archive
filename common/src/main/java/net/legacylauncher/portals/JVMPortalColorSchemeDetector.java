package net.legacylauncher.portals;

import java.util.function.Consumer;

public class JVMPortalColorSchemeDetector {
    public static Portal.ColorScheme getColorScheme() {
        return Portal.ColorScheme.NO_PREFERENCE;
    }

    public static AutoCloseable subscribeForColorSchemeChanges(Consumer<Portal.ColorScheme> callback) {
        return Portal.EmptyCloseable.INSTANCE;
    }
}
