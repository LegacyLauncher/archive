package net.legacylauncher.minecraft.launcher.hooks;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.jna.JNAException;
import net.legacylauncher.jna.JNAWindows;
import net.legacylauncher.minecraft.launcher.ProcessHook;
import net.legacylauncher.util.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER;

public class WindowsGpuPreferenceHook implements ProcessHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsGpuPreferenceHook.class);
    private static final int GPU_PREFERENCE_WINDOWS_BUILD = 20190;
    private static final String GPU_PREFERENCE_REG_KEY = "Software\\Microsoft\\DirectX\\UserGpuPreferences";
    private static final String GPU_PREFERENCE_VALUE = "GpuPreference=%d;";

    private final Preference preference;

    public WindowsGpuPreferenceHook(Preference preference) {
        this.preference = preference;
    }

    @Override
    public void enrichProcess(ProcessBuilder process) {
        if (!isSupported()) return;
        Optional<Integer> buildOpt = JNAWindows.getBuildNumber();
        if (!buildOpt.isPresent()) {
            LOGGER.warn("Couldn't find current Windows build. Is JNA enabled? Setting GPU performance is disabled");
            return;
        }
        List<String> command = process.command();
        if (command.isEmpty()) {
            LOGGER.info("Process command is empty, wat?");
            return;
        }
        Path path = Paths.get(command.get(0));
        if (!path.isAbsolute()) {
            LOGGER.warn("JRE executable is not absolute ({}), " +
                    "setting GPU performance is disabled", path);
            return;
        }
        Optional<JNAWindows.Registry> registryOpt = JNAWindows.getRegistry();
        if (!registryOpt.isPresent()) {
            LOGGER.warn("Registry is not available");
            return;
        }
        JNAWindows.Registry reg = registryOpt.get();
        String expectedValue = String.format(GPU_PREFERENCE_VALUE, preference.registryValue);
        String currentValue;
        try {
            currentValue = reg.getString(HKEY_CURRENT_USER, GPU_PREFERENCE_REG_KEY, path.toString());
        } catch (JNAException e) {
            LOGGER.error("Couldn't fetch current GPU preference. Setting it was skipped.", e);
            return;
        }
        if (expectedValue.equals(currentValue)) {
            LOGGER.debug("Skipping setting GPU Preference. Current value is matched expected one for {}: {}",
                    path, currentValue);
            return;
        }
        LOGGER.info("Setting GpuPreference value for {}: {}", path, expectedValue);
        try {
            reg.setString(HKEY_CURRENT_USER, GPU_PREFERENCE_REG_KEY, path.toString(), expectedValue);
        } catch (JNAException e) {
            LOGGER.error("Couldn't set current GPU preference", e);
        }
    }

    public static boolean isSupported() {
        if (!OS.WINDOWS.isCurrent() || !LegacyLauncher.getInstance().getSettings().getBoolean("windows.gpuperf")) {
            return false;
        }
        Optional<Integer> buildOpt = JNAWindows.getBuildNumber();
        if (!buildOpt.isPresent()) {
            LOGGER.warn("Couldn't find current Windows build. Is JNA enabled? Setting GPU performance is disabled");
            return false;
        }
        if (buildOpt.get() < GPU_PREFERENCE_WINDOWS_BUILD) {
            LOGGER.info("Current Windows build ({}) doesn't support setting GPU preference " +
                    "through registry", buildOpt.get());
            return false;
        }
        return true;
    }

    public enum Preference {
        Unspecified(0),
        MinimumPower(1),
        HighPerformance(2),
        ;

        private final int registryValue;

        Preference(int registryValue) {
            this.registryValue = registryValue;
        }

        public int getRegistryValue() {
            return registryValue;
        }
    }
}
