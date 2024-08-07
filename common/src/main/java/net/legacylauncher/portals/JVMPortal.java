package net.legacylauncher.portals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public class JVMPortal implements Portal {
    private static final Logger LOGGER = LoggerFactory.getLogger(JVMPortal.class);

    private final Desktop desktop;

    private JVMPortal(Desktop desktop) {
        this.desktop = desktop;
    }

    @Override
    public boolean openURI(URI uri) {
        try {
            desktop.browse(uri);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean openFile(Path path) {
        try {
            desktop.open(path.toFile());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean openDirectory(Path path) {
        return openFile(path.getParent());
    }

    @Override
    public ColorScheme getColorScheme() {
        return JVMPortalColorSchemeDetector.getColorScheme();
    }

    @Override
    public AutoCloseable subscribeForColorSchemeChanges(Consumer<ColorScheme> callback) {
        return JVMPortalColorSchemeDetector.subscribeForColorSchemeChanges(callback);
    }

    public static Optional<JVMPortal> tryToCreate() {
        if (!Desktop.isDesktopSupported()) {
            LOGGER.warn("Desktop API is not supported");
            return Optional.empty();
        }

        return Optional.of(new JVMPortal(Desktop.getDesktop()));
    }
}
