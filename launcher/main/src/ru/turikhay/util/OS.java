package ru.turikhay.util;

import com.sun.management.OperatingSystemMXBean;
import ru.turikhay.tlauncher.ui.alert.Alert;

import java.awt.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;

public enum OS {
    LINUX("linux", "unix"),
    WINDOWS("win"),
    OSX("mac"),
    SOLARIS("solaris", "sunos"),
    UNKNOWN("unknown");

    public static final String NAME;
    public static final String VERSION;
    public static final JavaVersion JAVA_VERSION;
    public static final OS CURRENT;
    private final String name;
    private final String[] aliases;

    static {
        NAME = System.getProperty("os.name");
        VERSION = System.getProperty("os.version");
        JAVA_VERSION = getJavaVersion();
        CURRENT = getCurrent();
    }

    OS(String... aliases) {
        if (aliases == null) {
            throw new NullPointerException();
        } else {
            name = toString().toLowerCase();
            this.aliases = aliases;
        }
    }

    public String getName() {
        return name;
    }

    public boolean isUnsupported() {
        return this == UNKNOWN;
    }

    public boolean isCurrent() {
        return this == CURRENT;
    }

    private static OS getCurrent() {
        String osName = NAME.toLowerCase();
        OS[] var4;
        int var3 = (var4 = values()).length;

        for (int var2 = 0; var2 < var3; ++var2) {
            OS os = var4[var2];
            String[] var8 = os.aliases;
            int var7 = os.aliases.length;

            for (int var6 = 0; var6 < var7; ++var6) {
                String alias = var8[var6];
                if (osName.contains(alias)) {
                    return os;
                }
            }
        }

        return UNKNOWN;
    }

    private static JavaVersion getJavaVersion() {
        JavaVersion version;

        try {
            version = JavaVersion.parse(System.getProperty("java.version"));
        } catch (Exception e) {
            version = JavaVersion.create(1, 6, 0, 45);

            log("Could not determine Java version:", System.getProperty("java.version"));
            log("Assuming it is 1.6.0_45");
        }

        return version;
    }

    public static boolean is(OS... any) {
        if (any == null) {
            throw new NullPointerException();
        } else if (any.length == 0) {
            return false;
        } else {
            OS[] var4 = any;
            int var3 = any.length;

            for (int var2 = 0; var2 < var3; ++var2) {
                OS compare = var4[var2];
                if (CURRENT == compare) {
                    return true;
                }
            }

            return false;
        }
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
        return NAME + " (" + VERSION + ") " + OS.Arch.CURRENT + ", Java " + System.getProperty("java.version") +", " + OS.Arch.TOTAL_RAM_MB + " MB RAM, " + Arch.AVAILABLE_PROCESSORS + "x CPU";
    }

    private static void rawOpenLink(URI uri) throws Throwable {
        Desktop.getDesktop().browse(uri);
    }

    public static boolean openLink(String _url, boolean alertError) {
        URL url;

        try {
            url = new URL(_url);
        } catch (Exception e) {
            log("Failed to parse link", _url, e);

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
        log("Trying to open link with default browser:", uri);

        try {
            Desktop.getDesktop().browse(uri);
            return true;
        } catch (Throwable var3) {
            log("Failed to open link with default browser:", uri, var3);
            if (alertError) {
                Alert.showLocError("ui.error.openlink", uri);
            }

            return false;
        }
    }

    public static boolean openLink(URI uri) {
        return openLink(uri, true);
    }

    public static boolean openLink(URL url, boolean alertError) {
        log("Trying to open URL with default browser:", url);
        URI uri = null;

        try {
            uri = url.toURI();
        } catch (Exception var4) {
        }

        return openLink(uri, alertError);
    }

    public static boolean openLink(URL url) {
        return openLink(url, true);
    }

    private static void openPath(File path, boolean appendSeparator) throws Throwable {
        String absPath = path.getAbsolutePath() + File.separatorChar;
        Runtime r = Runtime.getRuntime();
        Throwable t = null;

        switch (CURRENT) {
            case WINDOWS:
                String cmd = String.format("cmd.exe /C start \"Open path\" \"%s\"", absPath);

                try {
                    r.exec(cmd);
                    return;
                } catch (Throwable var9) {
                    t = var9;
                    log("Cannot open folder using CMD.exe:\n", cmd, var9);
                    break;
                }
            case OSX:
                String[] e = new String[]{"/usr/bin/open", absPath};

                try {
                    r.exec(e);
                    return;
                } catch (Throwable var10) {
                    t = var10;
                    log("Cannot open folder using:\n", e, var10);
                    break;
                }
            default:
                log("... will use desktop");
        }

        try {
            rawOpenLink(path.toURI());
        } catch (Throwable var8) {
            t = var8;
        }

        if (t != null) {
            throw t;
        }
    }

    public static boolean openFolder(File folder, boolean alertError) {
        log("Trying to open folder:", folder);
        if (!folder.isDirectory()) {
            log("This path is not a directory, sorry.");
            return false;
        } else {
            try {
                openPath(folder, true);
                return true;
            } catch (Throwable var3) {
                log("Failed to open folder:", var3);
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
        log("Trying to open file:", file);
        if (!file.isFile()) {
            log("This path is not a file, sorry.");
            return false;
        } else {
            try {
                openPath(file, false);
                return true;
            } catch (Throwable var3) {
                log("Failed to open file:", var3);
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

    protected static void log(Object... o) {
        U.log("[OS]", o);
    }

    public enum Arch {
        x86(32),
        x64(64),
        UNKNOWN(0);

        public static final OS.Arch CURRENT;
        public static final long TOTAL_RAM;
        public static final long TOTAL_RAM_MB;
        public static final int MIN_MEMORY = 512;
        public static final int PREFERRED_MEMORY;
        public static final int MAX_MEMORY;
        public static final int AVAILABLE_PROCESSORS;

        private final int bit, arch;
        private final String sBit, sArch;

        static {
            CURRENT = getCurrent();
            TOTAL_RAM = getTotalRam();
            TOTAL_RAM_MB = TOTAL_RAM / 1024L / 1024L;
            PREFERRED_MEMORY = getPreferredMemory();
            MAX_MEMORY = getMaximumMemory();
            AVAILABLE_PROCESSORS = getAvailableProcessors();
        }

        Arch(int bit) {
            this.bit = bit;
            sBit = String.valueOf(bit);

            if(bit == 0) {
                sArch = toString();
                arch = 0;
            } else {
                sArch = toString().substring(1);
                arch = Integer.parseInt(sArch);
            }
        }

        public String getBit() {
            return sBit;
        }

        public boolean isCurrent() {
            return this == CURRENT;
        }

        private static OS.Arch getCurrent() {
            String curArch = System.getProperty("sun.arch.data.model");
            OS.Arch[] var4;
            int var3 = (var4 = values()).length;

            for (int var2 = 0; var2 < var3; ++var2) {
                OS.Arch arch = var4[var2];
                if (arch.sBit.equals(curArch)) {
                    return arch;
                }
            }

            return UNKNOWN;
        }

        private static long getTotalRam() {
            try {
                return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
            } catch (Throwable var1) {
                U.log("[ERROR] Cannot allocate total physical memory size!", var1);
                return 0L;
            }
        }

        private static int getAvailableProcessors() {
            try {
                return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getAvailableProcessors();
            } catch (Throwable var1) {
                U.log("[ERROR] Cannot determine available processors", var1);
                return 1;
            }
        }

        private static int getPreferredMemory() {
            switch (CURRENT) {
                case x64:
                    return 1024;
                case x86:
                    if (TOTAL_RAM_MB > 4000L) {
                        return 768;
                    }
                default:
                    return MIN_MEMORY;
            }
        }

        private static int getMaximumMemory() {
            switch (CURRENT) {
                case x86:
                    if (TOTAL_RAM_MB > 4000L) {
                        return 1536;
                    }
                    return 1024;
                case x64:
                    if (TOTAL_RAM_MB > 6000L) {
                        return 3072;
                    } else {
                        if (TOTAL_RAM_MB > 3000L) {
                            return 1536;
                        }

                        return 1024;
                    }
                default:
                    return MIN_MEMORY;
            }
        }
    }
}
