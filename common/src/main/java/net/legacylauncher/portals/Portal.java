package net.legacylauncher.portals;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public interface Portal extends Closeable {
    default boolean openURI(URI uri) {
        return false;
    }

    default boolean openFile(Path path) {
        return false;
    }

    default boolean openDirectory(Path path) {
        return false;
    }

    default ColorScheme getColorScheme() {
        return ColorScheme.NO_PREFERENCE;
    }

    @Override
    default void close() throws IOException {};

    enum ColorScheme {
        NO_PREFERENCE,
        PREFER_DARK,
        PREFER_LIGHT,
        ;
    }
}
