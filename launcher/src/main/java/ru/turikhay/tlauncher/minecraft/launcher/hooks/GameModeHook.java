package ru.turikhay.tlauncher.minecraft.launcher.hooks;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.errors.ServiceUnknown;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.tlauncher.minecraft.launcher.ProcessHook;
import ru.turikhay.tlauncher.portals.XDGPortal;
import ru.turikhay.util.JavaVersion;

import java.util.Optional;
import java.util.concurrent.Callable;

public class GameModeHook implements ProcessHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameModeHook.class);

    private final GameModeInterface gameModeInterface;

    public GameModeHook(DBusConnection session) throws DBusException {
        this.gameModeInterface = session.getRemoteObject("com.feralinteractive.GameMode", "/com/feralinteractive/GameMode", GameModeInterface.class);
    }


    protected static Optional<ProcessHook> tryToCreate() {
        try {
            Callable<DBusConnection> systemFactory = () -> DBusConnectionBuilder.forSessionBus()
                    .withDisconnectCallback(new XDGPortal.DBusDisconnectionLogger())
                    .build();

            try {
                return Optional.of(new GameModeHook(systemFactory.call()));
            } catch (Throwable t) {
                return Optional.empty();
            }
        } catch (NoClassDefFoundError ignored) {
            // java.lang.NoClassDefFoundError: org/freedesktop/dbus/**
            // => older bootstrap version
        }

        return Optional.empty();
    }

    @Override
    @SuppressWarnings("Since15") // GameModeHook itself only created on java11+
    public void processCreated(Process process) {
        try {
            int callerPid = (int) ProcessHandle.current().pid();
            int gamePid = (int) process.pid();
            gameModeInterface.RegisterGameByPID(callerPid, gamePid);
            LOGGER.info("Minecraft process registered in GameMode. Pids: {} / {}", callerPid, gamePid);
        } catch (ServiceUnknown ignored) {
        }
    }

    @Override
    @SuppressWarnings("Since15") // GameModeHook itself only created on java11+
    public void processDestroyed(Process process) {
        try {
            int callerPid = (int) ProcessHandle.current().pid();
            int gamePid = (int) process.pid();
            gameModeInterface.UnregisterGameByPID(callerPid, gamePid);
            LOGGER.info("Minecraft process unregistered in GameMode. Pids: {} / {}", callerPid, gamePid);
        } catch (ServiceUnknown ignored) {
        }
    }

    public static class Loader {
        private static final Logger LOGGER = LoggerFactory.getLogger(Loader.class);

        public static Optional<ProcessHook> tryToCreate() {
            if (JavaVersion.getCurrent().getMajor() >= 11) {
                return GameModeHook.tryToCreate();
            } else {
                LOGGER.info("GameModeHook is not available because it requires Java 11+");
            }
            return Optional.empty();
        }
    }
}
