package ru.turikhay.tlauncher.jre;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.OS;

import java.util.Objects;

import static ru.turikhay.util.OS.Arch.IS_64_BIT;

public class JavaPlatform {
    private static final Logger LOGGER = LogManager.getLogger(JavaPlatform.class);

    public static final String CURRENT_PLATFORM = getCurrentPlatform();

    private static String getCurrentPlatform() {
        switch (OS.CURRENT) {
            case LINUX:
                return IS_64_BIT ? "linux" : "linux-i386";
            case WINDOWS:
                return IS_64_BIT ? "windows-x64" : "windows-x86";
            case OSX:
                switch (OS.Arch.CURRENT) {
                    case ARM64:
                        return "mac-os-arm64";
                    case x64:
                        return "mac-os";
                }
            default:
                LOGGER.warn("Current platform is unknown: {} {}", OS.CURRENT, OS.Arch.CURRENT);
                return null;
        }
    }

    public static OS getOSByPlatform(String platform) {
        Objects.requireNonNull(platform, "platform");

        if (platform.startsWith("linux")) {
            return OS.LINUX;
        } else if (platform.startsWith("windows")) {
            return OS.WINDOWS;
        } else if (platform.startsWith("mac-os")) {
            return OS.OSX;
        } else {
            throw new IllegalArgumentException("unknown platform: " + platform);
        }
    }
}
