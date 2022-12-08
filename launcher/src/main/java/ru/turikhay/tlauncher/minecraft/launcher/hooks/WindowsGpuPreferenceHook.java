package ru.turikhay.tlauncher.minecraft.launcher.hooks;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.jna.JNAException;
import ru.turikhay.tlauncher.jna.JNAWindows;
import ru.turikhay.tlauncher.minecraft.launcher.ProcessHook;
import ru.turikhay.util.OS;

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
            Sentry.capture(new EventBuilder()
                    .withLevel(Event.Level.INFO)
                    .withMessage("jreExec is not absolute")
                    .withExtra("jreExec", path)
            );
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
            Sentry.capture(new EventBuilder()
                    .withLevel(Event.Level.ERROR)
                    .withMessage("couldn't get GpuPreference")
                    .withSentryInterface(new ExceptionInterface(e))
                    .withExtra("windowsBuild", buildOpt.get())
                    .withExtra("jreExec", path)
            );
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
            Sentry.capture(new EventBuilder()
                    .withLevel(Event.Level.ERROR)
                    .withMessage("couldn't set GpuPreference")
                    .withSentryInterface(new ExceptionInterface(e))
                    .withExtra("windowsBuild", buildOpt.get())
                    .withExtra("jreExec", path)
            );
            LOGGER.error("Couldn't set current GPU preference", e);
        }
    }

    public static boolean isSupported() {
        if (!OS.WINDOWS.isCurrent() || !TLauncher.getInstance().getSettings().getBoolean("windows.gpuperf")) {
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
