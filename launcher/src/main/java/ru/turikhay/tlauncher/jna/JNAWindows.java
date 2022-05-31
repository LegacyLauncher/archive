package ru.turikhay.tlauncher.jna;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import ru.turikhay.util.Lazy;

import java.util.Optional;

import static ru.turikhay.tlauncher.jna.JNA.ENABLED;

public class JNAWindows {

    private static final Lazy<?> OSVERSIONINFOEX = Lazy.of(() -> {
        WinNT.OSVERSIONINFOEX vex = new WinNT.OSVERSIONINFOEX();
        if (Kernel32.INSTANCE.GetVersionEx(vex)) {
            return (Object) vex;
        }
        return null;
    });

    private static final Lazy<Integer> BUILD_NUMBER = Lazy.of(() ->
            ((WinNT.OSVERSIONINFOEX) OSVERSIONINFOEX.get()).getBuildNumber()
    );

    private static final Lazy<Registry> REGISTRY = Lazy.of(() -> Platform.isWindows() ? new Registry() : null);

    public static Optional<Integer> getBuildNumber() {
        return ENABLED ? BUILD_NUMBER.value() : Optional.empty();
    }

    public static Optional<Registry> getRegistry() {
        return ENABLED ? REGISTRY.value() : Optional.empty();
    }

    public static class Registry {
        private Registry() {
        }

        public boolean exists(WinReg.HKEY root, String key) throws JNAException {
            try {
                return Advapi32Util.registryKeyExists(root, key);
            } catch (Exception e) {
                throw new JNAException(e);
            }
        }

        public boolean exists(WinReg.HKEY root, String key, String name) throws JNAException {
            try {
                return Advapi32Util.registryValueExists(root, key, name);
            } catch (Exception e) {
                throw new JNAException(e);
            }
        }

        public String getString(WinReg.HKEY root, String key, String name) throws JNAException {
            try {
                if (!exists(root, key, name)) {
                    return null;
                }
                return Advapi32Util.registryGetStringValue(root, key, name);
            } catch (JNAException e) {
                throw e;
            } catch (Exception e) {
                throw new JNAException(e);
            }
        }

        public void setString(WinReg.HKEY root, String key, String name, String value) throws JNAException {
            try {
                if (!exists(root, key)) {
                    Advapi32Util.registryCreateKey(root, key);
                }
                Advapi32Util.registrySetStringValue(root, key, name, value);
            } catch (JNAException e) {
                throw e;
            } catch (Exception e) {
                throw new JNAException(e);
            }
        }
    }
}
