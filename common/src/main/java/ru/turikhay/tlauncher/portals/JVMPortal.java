package ru.turikhay.tlauncher.portals;

import com.jthemedetecor.OsThemeDetector;
import ru.turikhay.util.JavaVersion;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class JVMPortal implements Portal {
    private final Desktop desktop = Desktop.getDesktop();

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
        if (JavaVersion.getCurrent().getMajor() >= 11) {
            OsThemeDetector detector = OsThemeDetector.getDetector();
            return detector.isDark() ? ColorScheme.PREFER_DARK : ColorScheme.PREFER_LIGHT;
        }
        return ColorScheme.NO_PREFERENCE;
    }
}
