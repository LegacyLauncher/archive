package ru.turikhay.tlauncher.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import ru.turikhay.tlauncher.portals.XDGPortal;
import ru.turikhay.util.JavaVersion;

import java.util.Optional;
import java.util.concurrent.Callable;

public class LinuxGpuManager {
    private static final Logger LOGGER = LogManager.getLogger(LinuxGpuManager.class);

    public static Optional<GPUManager> tryToCreate() {
        if (JavaVersion.getCurrent().getMajor() >= 11) {
            try {
                Callable<DBusConnection> systemFactory = () -> DBusConnectionBuilder.forSystemBus()
                        .withDisconnectCallback(new XDGPortal.DBusDisconnectionLogger())
                        .build();
                try {
                    return Optional.of(new SwitcherooControlGPUManager(systemFactory.call()));
                } catch (Throwable t) {
                    LOGGER.info("SwitcherooControlGPUManager is not available: {}", t.toString());
                    return Optional.empty();
                }
            } catch (NoClassDefFoundError ignored) {
                LOGGER.info("SwitcherooControlGPUManager is not available because it requires " +
                        "org.freedesktop.dbus to be in the classpath");
                // java.lang.NoClassDefFoundError: org/freedesktop/dbus/**
                // => older bootstrap version
            }
        } else {
            LOGGER.info("SwitcherooControlGPUManager is not available because it requires Java 11+");
        }
        return Optional.empty();
    }
}
