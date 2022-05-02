package ru.turikhay.tlauncher.jna;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import ru.turikhay.util.Lazy;

import java.util.Optional;

import static ru.turikhay.tlauncher.jna.JNA.ENABLED;

public class JNAWindows {

    private static final Lazy<WinNT.OSVERSIONINFOEX> OSVERSIONINFOEX = Lazy.of(() -> {
        WinNT.OSVERSIONINFOEX vex = new WinNT.OSVERSIONINFOEX();
        if (Kernel32.INSTANCE.GetVersionEx(vex)) {
            return vex;
        }
        return null;
    });

    private static final Lazy<Integer> BUILD_NUMBER = Lazy.of(() -> OSVERSIONINFOEX.get().getBuildNumber());

    public static Optional<Integer> getBuildNumber() {
        return ENABLED ? BUILD_NUMBER.value() : Optional.empty();
    }
}
