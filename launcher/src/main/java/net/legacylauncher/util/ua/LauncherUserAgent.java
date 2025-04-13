package net.legacylauncher.util.ua;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.util.OS;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class LauncherUserAgent {
    public static final String USER_AGENT = UserAgent.of(
            "LL",
            LegacyLauncher.getVersion().toString(),
            constructPlatform()
    );

    public static void set(URLConnection c) {
        c.setRequestProperty("User-Agent", USER_AGENT);
    }

    private static List<String> constructPlatform() {
        List<String> details = new ArrayList<>();
        switch (OS.CURRENT) {
            case WINDOWS:
                details.add("Windows NT " + OS.VERSION);
                if (OS.Arch.CURRENT == OS.Arch.x64) {
                    details.add("Win64");
                }
                details.add(OS.Arch.CURRENT.toString());
                break;
            case LINUX:
                details.add("Linux");
                OperatingSystem os = new SystemInfo().getOperatingSystem();
                if (os.getFamily() != null) {
                    StringBuilder distro = new StringBuilder();
                    distro.append(os.getFamily());
                    String version = os.getVersionInfo().getVersion();
                    if (version != null) {
                        distro.append(" ").append(version);
                    }
                    details.add(distro.toString());
                }
                details.add(System.getProperty("os.arch"));
                break;
            case OSX: {
                details.add("Macintosh");
                details.add("Mac OS " + OS.VERSION);
                if (OS.Arch.ARM64.isCurrent()) {
                    details.add("ARM64");
                } else {
                    details.add("Intel");
                }
                break;
            }
            case UNKNOWN:
                details.add(System.getProperty("os.name") + " " + System.getProperty("os.version"));
                details.add(System.getProperty("os.arch"));
                break;
        }
        details.add("Java/" + System.getProperty("java.version"));
        return details;
    }
}
