package ru.turikhay.tlauncher.portals;

import java.net.URI;
import java.nio.file.Path;

public interface Portal {
    default boolean openURI(URI uri) {
        return false;
    }

    default boolean openFile(Path path) {
        return false;
    }

    default boolean openDirectory(Path path) {
        return false;
    }

    default void enrichMinecraftProcess(ProcessBuilder process) {}
    default void minecraftProcessCreated(Process process) {}
    default void minecraftProcessDestroyed(Process process) {}

    default ColorScheme getColorScheme() {
        return ColorScheme.NO_PREFERENCE;
    }

    enum ColorScheme {
        NO_PREFERENCE,
        PREFER_DARK,
        PREFER_LIGHT,
        ;
    }
}
