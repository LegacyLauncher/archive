package ru.turikhay.util;

import com.sun.management.OperatingSystemMXBean;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.jna.JNA;
import ru.turikhay.tlauncher.jna.JNAMacOs;
import ru.turikhay.tlauncher.portals.Portals;
import ru.turikhay.tlauncher.ui.alert.Alert;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

public enum OS {
    LINUX("linux", "unix"),
    WINDOWS("win"),
    OSX("mac"),
    UNKNOWN("unknown");

    private static final Logger LOGGER = LogManager.getLogger(OS.class);

    public static final String NAME = System.getProperty("os.name");
    public static final String VERSION = System.getProperty("os.version");

    public static final OS CURRENT = JNA.getCurrentOs().orElseGet(OS::detectOSFallback);

    public static final JavaVersion JAVA_VERSION;

    static {
        JavaVersion version;
        try {
            version = JavaVersion.parse(System.getProperty("java.version"));
        } catch (Exception e) {
            version = JavaVersion.create(1, 8, 0, 45);

            LOGGER.warn("Could not parse Java version: {}", System.getProperty("java.version"));
            LOGGER.warn("Assuming it is 1.8.0_45");
        }
        JAVA_VERSION = version;
    }

    private final String[] aliases;

    OS(String... aliases) {
        this.aliases = aliases;
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean isUnsupported() {
        return this == UNKNOWN;
    }

    public boolean isCurrent() {
        return this == CURRENT;
    }

    public static boolean is(OS... any) {
        if (any == null) {
            throw new NullPointerException();
        }
        if (any.length == 0) {
            return false;
        }
        for (OS compare : any) {
            if (CURRENT == compare) {
                return true;
            }
        }
        return false;
    }

    private static OS detectOSFallback() {
        String osName = NAME.toLowerCase(java.util.Locale.ROOT);
        for (OS os : values()) {
            for (String alias : os.aliases) {
                if (osName.contains(alias)) {
                    return os;
                }
            }
        }
        return UNKNOWN;
    }

    public static String getJavaPath(boolean appendBinFolder) {
        char separator = File.separatorChar;
        String path = System.getProperty("java.home") + separator;
        if (appendBinFolder) {
            path = path + "bin" + separator + "java";
            if (CURRENT == WINDOWS) {
                path = path + "w.exe";
            }
        }

        return path;
    }

    public static String getJavaPath() {
        return getJavaPath(true);
    }

    public static String getSummary() {
        return String.format(Locale.ROOT, "%s (%s) %s, Java %s %s (%s), %s MB RAM, %sx CPU",
                NAME,
                VERSION,
                JNA.getArch().orElse("n/a"),
                System.getProperty("java.version", "unknown"),
                System.getProperty("os.arch", "unknown"),
                Arch.CURRENT,
                Arch.TOTAL_RAM_MB,
                Arch.AVAILABLE_PROCESSORS
        );
    }

    public static boolean openLink(String _url, boolean alertError) {
        URL url;

        try {
            url = new URL(_url);
        } catch (Exception e) {
            LOGGER.warn("Failed to parse link: \"{}\"", _url, e);

            if (alertError) {
                Alert.showLocError("ui.error.openlink", _url);
            }

            return false;
        }

        return openLink(url);
    }

    public static boolean openLink(String url) {
        return openLink(url, true);
    }

    public static boolean openLink(URI uri, boolean alertError) {
        if (Portals.getPortal().openURI(uri)) return true;

        LOGGER.log(alertError ? Level.ERROR : Level.WARN, "Opening the link failed: \"{}\"", uri);
        if (alertError) {
            Alert.showLocError("ui.error.openlink", uri);
        }

        return false;
    }

    public static boolean openLink(URI uri) {
        return openLink(uri, true);
    }

    public static boolean openLink(URL url, boolean alertError) {
        URI uri = null;

        try {
            uri = url.toURI();
        } catch (Exception var4) {
            LOGGER.warn("Couldn't convert URL to URI: {}", url, var4);
        }

        return openLink(uri, alertError);
    }

    public static boolean openLink(URL url) {
        return openLink(url, true);
    }

    public static boolean openFolder(File folder, boolean alertError) {
        LOGGER.info("Trying to open folder: {}", folder);
        if (!folder.isDirectory()) {
            LOGGER.warn("This path is not a directory, sorry.");
            return false;
        } else {
            try {
                return Portals.getPortal().openFile(folder.toPath());
            } catch (Throwable var3) {
                LOGGER.log(alertError ? Level.ERROR : Level.WARN, "Failed to open folder: {}", folder, var3);
                if (alertError) {
                    Alert.showLocError("ui.error.openfolder", folder);
                }

                return false;
            }
        }
    }

    public static boolean openFolder(File folder) {
        return openFolder(folder, true);
    }

    public static boolean openFile(File file, boolean alertError) {
        LOGGER.info("Trying to open file: {}", file);
        if (!file.isFile()) {
            LOGGER.warn("This path is not a file, sorry.");
            return false;
        } else {
            try {
                return Portals.getPortal().openFile(file.toPath());
            } catch (Throwable var3) {
                LOGGER.log(alertError ? Level.ERROR : Level.WARN, "Failed to open file: {}", file, var3);
                if (alertError) {
                    Alert.showLocError("ui.error.openfolder", file);
                }

                return false;
            }
        }
    }

    public static boolean openFile(File file) {
        return openFile(file, true);
    }

    public enum Arch {
        x86,
        x64,
        ARM,
        ARM64;

        public static final Arch CURRENT;
        public static final boolean IS_64_BIT = JNA.is64Bit().orElseGet(Arch::is64BitFallback);

        static {
            Arch current;
            boolean isMacOsARM;
            if (OS.OSX.isCurrent() && IS_64_BIT) {
                isMacOsARM = JNA.isARM().orElse(false) || JNAMacOs.isUnderRosetta().orElse(false);
            } else {
                isMacOsARM = false;
            }
            if (isMacOsARM) {
                current = Arch.ARM64;
            } else {
                if (IS_64_BIT) {
                    current = Arch.x64;
                } else {
                    // We'll hope that the current platform can emulate x86
                    current = Arch.x86;
                }
            }
            CURRENT = current;
        }

        public static final long TOTAL_RAM;
        public static final long TOTAL_RAM_MB;
        public static final int AVAILABLE_PROCESSORS;

        static {
            TOTAL_RAM = getTotalRam();
            TOTAL_RAM_MB = TOTAL_RAM / 1024L / 1024L;
            AVAILABLE_PROCESSORS = getAvailableProcessors();
        }

        public boolean isCurrent() {
            return this == CURRENT;
        }

        public boolean isARM() {
            return this == ARM || this == ARM64;
        }

        public boolean is64Bit() {
            return this == x64 || this == ARM64;
        }

        private static long getTotalRam() {
            try {
                return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
            } catch (Throwable var1) {
                LOGGER.warn("Cannot query total physical memory size!", var1);
                return 0L;
            }
        }

        private static int getAvailableProcessors() {
            try {
                return ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
            } catch (Throwable var1) {
                LOGGER.warn("Cannot query the number of available processors", var1);
                return 1;
            }
        }

        private static boolean is64BitFallback() {
            String model = System.getProperty("sun.arch.data.model");
            if (model != null) {
                return "64".equals(model);
            }
            String arch = System.getProperty("os.arch");
            if (arch != null) {
                return arch.contains("64");
            }
            return false;
        }
    }
}
