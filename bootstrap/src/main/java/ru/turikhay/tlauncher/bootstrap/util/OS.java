package ru.turikhay.tlauncher.bootstrap.util;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum OS {
    LINUX("linux", "unix"),
    WINDOWS("win"),
    OSX("mac"),
    SOLARIS("solaris", "sunos"),
    UNKNOWN("unknown");

    public static final OS CURRENT;

    static {
        String name = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT);
        OS current = UNKNOWN;

        for (OS os : values()) {
            for (String alias : os.aliases) {
                if (name.contains(alias)) {
                    current = os;
                    break;
                }
            }
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
            case SOLARIS:
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
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
            Desktop.getDesktop().open(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private final String[] aliases;
    private final String lowerCase;

    OS(String... aliases) {
        this.aliases = aliases;
        this.lowerCase = name().toLowerCase(java.util.Locale.ROOT);
    }

    public boolean isCurrent() {
        return this == CURRENT;
    }

    public String nameLowerCase() {
        return lowerCase;
    }

    private static void log(Object... o) {
        U.log("[OS]", o);
    }

    public enum Arch {
        x86("32"), x64("64"), UNKNOWN(null);

        public static final Arch CURRENT;

        static {
            String dataModel = System.getProperty("sun.arch.data.model");
            Arch current = UNKNOWN;
            if (dataModel != null) {
                for (Arch arch : values()) {
                    if (dataModel.equals(arch.determiner)) {
                        current = arch;
                        break;
                    }
                }
            }
            CURRENT = current;
        }

        private final String determiner, lowerCase;

        Arch(String determiner) {
            this.determiner = determiner;
            this.lowerCase = name().toLowerCase(java.util.Locale.ROOT);
        }

        public String nameLowerCase() {
            return lowerCase;
        }
    }
}
