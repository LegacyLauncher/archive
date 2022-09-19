package ru.turikhay.tlauncher.bootstrap.util;

import com.sun.jna.Platform;
import ru.turikhay.tlauncher.portals.Portals;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public enum OS {
    LINUX,
    WINDOWS,
    OSX,
    UNKNOWN;

    public static final OS CURRENT;
    static {
        OS current = UNKNOWN;
        if (Platform.isWindows()) {
            current = WINDOWS;
        } else if (Platform.isLinux()) {
            current = LINUX;
        } else if (Platform.isMac()) {
            current = OSX;
        }
        CURRENT = current;
    }

    public static boolean isAny(OS... any) {
        for (OS os : any) {
            if (CURRENT == os) {
                return true;
            }
        }
        return false;
    }

    public static Path getSystemRelatedFile(String path) {
        String userHome = System.getProperty("user.home", ".");

        switch (CURRENT) {
            case WINDOWS:
                String applicationData = System.getenv("APPDATA");
                String folder = applicationData != null ? applicationData : userHome;
                return Paths.get(folder, path);
            case OSX:
                return Paths.get(userHome, "Library", "Application Support", path);
            case LINUX:
            default:
                return Paths.get(userHome, path);
        }
    }

    public static Path getSystemRelatedDirectory(String path, boolean hide) {
        if (hide && !OS.isAny(OSX, UNKNOWN)) {
            path = '.' + path;
        }
        return getSystemRelatedFile(path);
    }

    public static Path getDefaultFolder() {
        return OS.getSystemRelatedDirectory("tlauncher", true);
    }

    public static boolean openUri(URI uri) {
        log("Opening URL: " + uri.toASCIIString());
        try {
            return Portals.getPortal().openURI(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean openUrl(URL url) {
        URI uri;

        try {
            uri = url.toURI();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return openUri(uri);
    }

    public static boolean openPath(File path) {
        try {
            return Portals.getPortal().openFile(path.toPath());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isCurrent() {
        return this == CURRENT;
    }

    public String nameLowerCase() {
        return name().toLowerCase(Locale.ROOT);
    }

    private static void log(Object... o) {
        U.log("[OS]", o);
    }

    public enum Arch {
        x86, x64, ARM, ARM64;

        public static final Arch CURRENT;
        static {
            Arch current;
            if (Platform.is64Bit()) {
                if (Platform.isARM()) {
                    current = Arch.ARM64;
                } else {
                    current = Arch.x64;
                }
            } else {
                if (Platform.isARM()) {
                    current = Arch.ARM;
                } else {
                    // We'll hope that the current platform can emulate x86
                    current = Arch.x86;
                }
            }
            CURRENT = current;
        }

        public String nameLowerCase() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
