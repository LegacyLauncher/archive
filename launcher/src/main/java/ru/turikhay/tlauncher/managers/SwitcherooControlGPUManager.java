package ru.turikhay.tlauncher.managers;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.errors.ServiceUnknown;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.tlauncher.minecraft.launcher.hooks.EnvHook;
import ru.turikhay.tlauncher.portals.XDGPortal;
import ru.turikhay.util.JavaVersion;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class SwitcherooControlGPUManager implements GPUManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwitcherooControlGPUManager.class);
    private final Properties switcherooControlProperties;

    public SwitcherooControlGPUManager(DBusConnection system) throws DBusException {
        this.switcherooControlProperties = system.getRemoteObject("net.hadess.SwitcherooControl", "/net/hadess/SwitcherooControl", Properties.class);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public List<GPU> discoveryGPUs() {
        try {
            List<Map<String, Variant<?>>> gpus = switcherooControlProperties.Get("net.hadess.SwitcherooControl", "GPUs");
            return gpus.stream().map(it -> {
                String name = ((Variant<String>) it.get("Name")).getValue();
                boolean isDefault = ((Variant<Boolean>) it.get("Default")).getValue();
                List<String> envList = ((Variant<List<String>>) it.get("Environment")).getValue();
                Map<String, String> env = new HashMap<>();
                for (int i = 0; i < envList.size() - 1; i += 2) {
                    String key = envList.get(i);
                    String value = envList.get(i + 1);
                    env.put(key, value);
                }
                LOGGER.info("Found GPU: {}, isDefault: {}", name, isDefault);
                return new GPUManager.GPU(name, isDefault, new EnvHook(env));
            }).collect(Collectors.toList());
        } catch (ServiceUnknown ignored) {
            LOGGER.warn("Switcheroo-Control is unavailable");
            return Collections.emptyList();
        }
    }

    public static Optional<GPUManager> tryToCreate() {
        if (JavaVersion.getCurrent().getMajor() >= 11) {
            try {
                Callable<DBusConnection> systemFactory = () -> DBusConnectionBuilder.forSystemBus()
                        .withShared(false)
                        .withDisconnectCallback(new XDGPortal.DBusDisconnectionLogger())
                        .build();

                try {
                    return Optional.of(new SwitcherooControlGPUManager(systemFactory.call()));
                } catch (Throwable t) {
                    return Optional.empty();
                }
            } catch (NoClassDefFoundError ignored) {
                // java.lang.NoClassDefFoundError: org/freedesktop/dbus/**
                // => older bootstrap version
            }
        }

        return Optional.empty();
    }
}
