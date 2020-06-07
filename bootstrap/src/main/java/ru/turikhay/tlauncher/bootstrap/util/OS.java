package ru.turikhay.tlauncher.bootstrap.util;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;

public enum OS {
    LINUX("linux", "unix"),
    WINDOWS("win"),
    OSX("mac"),
    SOLARIS("solaris", "sunos"),
    UNKNOWN("unknown");

    public static final OS CURRENT;

    static {
        String name = System.getProperty("os.name").toLowerCase();
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

    public static File getSystemRelatedFile(String path) {
        String userHome = System.getProperty("user.home", ".");
        File file;
        switch (CURRENT) {
            case LINUX:
            case SOLARIS:
                file = new File(userHome, path);
                break;
            case WINDOWS:
                String applicationData = System.getenv("APPDATA");
                String folder = applicationData != null ? applicationData : userHome;
                file = new File(folder, path);
                break;
            case OSX:
                file = new File(userHome, "Library/Application Support/" + path);
                break;
            default:
                file = new File(userHome, path);
        }

        return file;
    }

    public static File getSystemRelatedDirectory(String path, boolean hide) {
        if (hide && !OS.isAny(OSX, UNKNOWN)) {
            path = '.' + path;
        }
        return getSystemRelatedFile(path);
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

    private final String[] aliases;
    private final String lowerCase;

    OS(String... aliases) {
        this.aliases = aliases;
        this.lowerCase = name().toLowerCase();
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
            if(dataModel != null) {
                for(Arch arch : values()) {
                    if(dataModel.equals(arch.determiner)) {
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
            this.lowerCase = name().toLowerCase();
        }

        public String nameLowerCase() {
            return lowerCase;
        }
    }
}
