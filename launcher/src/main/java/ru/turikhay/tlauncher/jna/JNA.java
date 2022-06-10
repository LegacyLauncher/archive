package ru.turikhay.tlauncher.jna;

import com.sun.jna.Platform;
import ru.turikhay.util.OS;

import java.util.Optional;

public class JNA {
    static boolean ENABLED;
    static {
        boolean enabled = true;
        try {
            Class.forName("com.sun.jna.Platform");
        } catch (ClassNotFoundException e) {
            enabled = false;
        }
        ENABLED = enabled;
    }

    public static Optional<Boolean> is64Bit() {
        return ENABLED ? Optional.of(Platform.is64Bit()) : Optional.empty();
    }

    public static Optional<Boolean> isARM() {
        return ENABLED ? Optional.of(Platform.isARM()) : Optional.empty();
    }

    public static Optional<OS> getCurrentOs() {
        if (!ENABLED) {
            return Optional.empty();
        }
        OS current = OS.UNKNOWN;
        if (Platform.isWindows()) {
            current = OS.WINDOWS;
        } else if (Platform.isLinux()) {
            current = OS.LINUX;
        } else if (Platform.isMac()) {
            current = OS.OSX;
        }
        return Optional.of(current);
    }

    public static Optional<String> getArch() {
        return ENABLED ? Optional.of(Platform.ARCH) : Optional.empty();
    }

    public static boolean isEnabled() {
        return ENABLED;
    }
}
