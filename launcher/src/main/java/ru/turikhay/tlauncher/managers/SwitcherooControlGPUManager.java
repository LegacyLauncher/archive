package ru.turikhay.tlauncher.managers;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.errors.ServiceUnknown;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.tlauncher.minecraft.launcher.hooks.EnvHook;
import ru.turikhay.util.Lazy;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SwitcherooControlGPUManager implements GPUManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwitcherooControlGPUManager.class);
    private static final Pattern NAME_PATTERN = Pattern.compile(".*\\[([^]]+)]");
    private final Properties switcherooControlProperties;
    private final Lazy<List<GPU>> gpus;

    @SuppressWarnings("unchecked")
    public SwitcherooControlGPUManager(DBusConnection system) throws DBusException {
        this.switcherooControlProperties = system.getRemoteObject("net.hadess.SwitcherooControl", "/net/hadess/SwitcherooControl", Properties.class);

        gpus = Lazy.of(() -> {
            try {
                List<Map<String, Variant<?>>> gpus = switcherooControlProperties.Get("net.hadess.SwitcherooControl", "GPUs");
                return gpus.stream().map(it -> {
                    String name = ((Variant<String>) it.get("Name")).getValue();
                    Vendor vendor = Vendor.guessVendorByName(name);
                    boolean isDefault = ((Variant<Boolean>) it.get("Default")).getValue();
                    List<String> envList = ((Variant<List<String>>) it.get("Environment")).getValue();
                    Map<String, String> env = new HashMap<>();
                    for (int i = 0; i < envList.size() - 1; i += 2) {
                        String key = envList.get(i);
                        String value = envList.get(i + 1);
                        env.put(key, value);
                    }
                    LOGGER.info("Found GPU: {}, vendor: {}, isDefault: {}", name, vendor, isDefault);
                    return new GPUManager.GPU(name, vendor, isDefault, new EnvHook(env)) {
                        @Override
                        public String getDisplayName(GPUManager gpuManager) {
                            String name = getName();
                            Matcher matcher = NAME_PATTERN.matcher(name);
                            if (matcher.matches()) {
                                return matcher.group(1);
                            } else {
                                return name;
                            }
                        }
                    };
                }).collect(Collectors.toList());
            } catch (ServiceUnknown ignored) {
                LOGGER.warn("Switcheroo-Control is unavailable");
                return Collections.emptyList();
            }
        });
    }

    @Nonnull
    @Override
    public List<GPU> discoveryGPUs() {
        return gpus.get();
    }

    @Override
    public String toString() {
        return "SwitcherooControlGPUManager";
    }
}
