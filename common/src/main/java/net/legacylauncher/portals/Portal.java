package net.legacylauncher.portals;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.function.Consumer;

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

    default AutoCloseable subscribeForColorSchemeChanges(Consumer<ColorScheme> callback) {
        return EmptyCloseable.INSTANCE;
    }

    final class EmptyCloseable implements AutoCloseable {
        public static final AutoCloseable INSTANCE = new EmptyCloseable();

        private EmptyCloseable() {

        }

        @Override
        public void close() throws Exception {

        }
    }

    @Override
    default void close() throws IOException {
    }

    enum ColorScheme {
        NO_PREFERENCE,
        PREFER_DARK,
        PREFER_LIGHT,
        ;

        public ColorScheme orLight() {
            return this == NO_PREFERENCE ? PREFER_LIGHT : this;
        }
    }
}
