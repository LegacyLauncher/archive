package ru.turikhay.tlauncher.managers;

import org.freedesktop.dbus.connections.IDisconnectCallback;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.tlauncher.minecraft.launcher.ProcessHook;
import ru.turikhay.tlauncher.minecraft.launcher.hooks.EnvHook;
import ru.turikhay.util.JavaVersion;
import ru.turikhay.util.Lazy;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
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
                    return new GPUManager.GPU(name, vendor, isDefault) {
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

                        @Override
                        public ProcessHook getHook(GPUManager gpuManager) {
                            return new EnvHook(env);
                        }
                    };
                }).collect(Collectors.toList());
            } catch (DBusExecutionException ignored) {
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

    protected static Optional<GPUManager> tryToCreate() {
        try {
            Callable<DBusConnection> systemFactory = () -> DBusConnectionBuilder.forSystemBus()
                    .withDisconnectCallback(new DBusDisconnectionLogger())
                    .build();
            try {
                return Optional.of(new SwitcherooControlGPUManager(systemFactory.call()));
            } catch (Throwable t) {
                LOGGER.info("SwitcherooControlGPUManager is not available: {}", t.toString());
            }
        } catch (NoClassDefFoundError ignored) {
            LOGGER.info("SwitcherooControlGPUManager is not available because it requires " +
                    "org.freedesktop.dbus to be in the classpath");
            // java.lang.NoClassDefFoundError: org/freedesktop/dbus/**
            // => older bootstrap version
        }
        return Optional.empty();
    }

    private static class DBusDisconnectionLogger implements IDisconnectCallback {
        @Override
        public void disconnectOnError(IOException e) {
            LOGGER.error("DBus session terminated due to an error", e);
        }
    }


    public static class Loader {
        private static final Logger LOGGER = LoggerFactory.getLogger(Loader.class);

        public static Optional<GPUManager> tryToCreate() {
            if (JavaVersion.getCurrent().getMajor() >= 11) {
                return SwitcherooControlGPUManager.tryToCreate();
            } else {
                LOGGER.info("SwitcherooControlGPUManager is not available because it requires Java 11+");
            }
            return Optional.empty();
        }
    }
}
