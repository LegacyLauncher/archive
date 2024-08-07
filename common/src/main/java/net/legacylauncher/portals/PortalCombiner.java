package net.legacylauncher.portals;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public class PortalCombiner implements Portal {
    private final Collection<Portal> portals;

    public PortalCombiner(Collection<Portal> portals) {
        this.portals = portals;
    }

    public PortalCombiner(Portal... portals) {
        this(Arrays.asList(portals));
    }

    @Override
    public boolean openURI(URI uri) {
        for (Portal portal : portals) {
            if (portal.openURI(uri)) return true;
        }
        return false;
    }

    @Override
    public boolean openFile(Path path) {
        for (Portal portal : portals) {
            if (portal.openFile(path)) return true;
        }
        return false;
    }

    @Override
    public boolean openDirectory(Path path) {
        for (Portal portal : portals) {
            if (portal.openDirectory(path)) return true;
        }
        return false;
    }

    @Override
    public ColorScheme getColorScheme() {
        for (Portal portal : portals) {
            ColorScheme colorScheme = portal.getColorScheme();
            if (colorScheme != ColorScheme.NO_PREFERENCE) return colorScheme;
        }
        return ColorScheme.NO_PREFERENCE;
    }

    @Override
    public AutoCloseable subscribeForColorSchemeChanges(Consumer<ColorScheme> callback) {
        for (Portal portal : portals) {
            AutoCloseable closeable = portal.subscribeForColorSchemeChanges(callback);
            if (closeable != EmptyCloseable.INSTANCE) return closeable;
        }
        return EmptyCloseable.INSTANCE;
    }

    @Override
    public void close() throws IOException {
        IOException exception = null;
        for (Portal portal : portals) {
            try {
                portal.close();
            } catch (IOException e) {
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }
}
