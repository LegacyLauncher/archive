package ru.turikhay.tlauncher.jna;

import com.sun.jna.Platform;

import java.util.Optional;

public class JNA {
    static boolean ENABLED = false;

    public static Optional<Boolean> is64Bit() {
        return ENABLED ? Optional.of(Platform.is64Bit()) : Optional.empty();
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static void enable() {
        ENABLED = true;
    }
}
