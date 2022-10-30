package ru.turikhay.tlauncher.portals;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.connections.IDisconnectCallback;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.errors.ServiceUnknown;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.tlauncher.portals.dbus.GameModeInterface;
import ru.turikhay.tlauncher.portals.dbus.OpenURIInterface;
import ru.turikhay.tlauncher.portals.dbus.SettingsInterface;
import ru.turikhay.util.JavaVersion;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

public class XDGPortal implements Portal, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(XDGPortal.class);

    private final DBusConnection session, fdSession, system;
    private final OpenURIInterface openURIInterface, fdOpenURIInterface;
    private final SettingsInterface settingsInterface;
    private final Properties switcherooControlProperties;
    private final GameModeInterface gameModeInterface;

    private XDGPortal(DBusConnection session, DBusConnection fdSession, DBusConnection system) throws DBusException {
        this.session = session;
        this.fdSession = fdSession;
        this.system = system;
        this.openURIInterface = session.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", OpenURIInterface.class);
        this.fdOpenURIInterface = fdSession.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", OpenURIInterface.class);
        this.settingsInterface = session.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", SettingsInterface.class);
        this.switcherooControlProperties = system.getRemoteObject("net.hadess.SwitcherooControl", "/net/hadess/SwitcherooControl", Properties.class);
        this.gameModeInterface = session.getRemoteObject("com.feralinteractive.GameMode", "/com/feralinteractive/GameMode", GameModeInterface.class);
    }

    @Override
    public void close() throws IOException {
        session.close();
        fdSession.close();
        system.close();
    }

    @Override
    public boolean openURI(URI uri) {
        try {
            openURIInterface.OpenURI("", uri.toString(), Collections.emptyMap());
            return true;
        } catch (ServiceUnknown e) {
            return false;
        }
    }

    @Override
    public boolean openDirectory(Path path) {
        try {
            Map<String, Variant<?>> options = new HashMap<>();
            options.put("writable", new Variant<>(false));
            int fd = CLibrary.INSTANCE.open(path.toString(), CLibrary.O_RDONLY | CLibrary.O_CLOEXEC);
            fdOpenURIInterface.OpenDirectory("", new FileDescriptor(fd), options);
            CLibrary.INSTANCE.close(fd);
            return true;
        } catch (ServiceUnknown e) {
            return false;
        }
    }

    @Override
    public boolean openFile(Path path) {
        try {
            Map<String, Variant<?>> options = new HashMap<>();
            options.put("writable", new Variant<>(false));
            int fd = CLibrary.INSTANCE.open(path.toString(), CLibrary.O_RDONLY | CLibrary.O_CLOEXEC);
            fdOpenURIInterface.OpenFile("", new FileDescriptor(fd), options);
            CLibrary.INSTANCE.close(fd);
            return true;
        } catch (ServiceUnknown e) {
            return false;
        }
    }

    @Override
    public ColorScheme getColorScheme() {
        try {
            Variant<UInt32> colorScheme = settingsInterface.Read("org.freedesktop.appearance", "color-scheme");
            switch (colorScheme.getValue().intValue()) {
                case 0:
                default:
                    return ColorScheme.NO_PREFERENCE;
                case 1:
                    return ColorScheme.PREFER_DARK;
                case 2:
                    return ColorScheme.PREFER_LIGHT;
            }
        } catch (ServiceUnknown e) {
            return ColorScheme.NO_PREFERENCE;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void enrichMinecraftProcess(ProcessBuilder process) {
        try {
            List<Map<String, Variant<?>>> gpus = switcherooControlProperties.Get("net.hadess.SwitcherooControl", "GPUs");
            gpus.stream().filter(it -> {
                Variant<Boolean> v = (Variant<Boolean>) it.get("Default");
                return v != null && !v.getValue();
            }).findFirst().ifPresent(m -> {
                String name = ((Variant<String>) m.get("Name")).getValue();
                List<String> env = ((Variant<List<String>>) m.get("Environment")).getValue();
                LOGGER.info("Launching on GPU: {}", name);
                for (int i = 0; i < env.size() - 1; i += 2) {
                    String key = env.get(i);
                    String value = env.get(i + 1);
                    process.environment().put(key, value);
                }
            });
        } catch (ServiceUnknown ignored) {
        }
    }

    @Override
    @SuppressWarnings("Since15") // XDGPortal itself only created on java11+
    public void minecraftProcessCreated(Process process) {
        try {
            int callerPid = (int) ProcessHandle.current().pid();
            int gamePid = (int) process.pid();
            gameModeInterface.RegisterGameByPID(callerPid, gamePid);
            LOGGER.info("Minecraft process registered in GameMode. Pids: {} / {}", callerPid, gamePid);
        } catch (ServiceUnknown ignored) {
        }
    }

    @Override
    @SuppressWarnings("Since15") // XDGPortal itself only created on java11+
    public void minecraftProcessDestroyed(Process process) {
        try {
            int callerPid = (int) ProcessHandle.current().pid();
            int gamePid = (int) process.pid();
            gameModeInterface.UnregisterGameByPID(callerPid, gamePid);
            LOGGER.info("Minecraft process unregistered in GameMode. Pids: {} / {}", callerPid, gamePid);
        } catch (ServiceUnknown ignored) {
        }
    }

    public static Optional<Portal> tryToCreate() {
        Callable<DBusConnection> sessionFactory = () -> DBusConnectionBuilder.forSessionBus()
                .withShared(false)
                .withDisconnectCallback(new DBusDisconnectionLogger())
                .build();

        try {
            DBusConnection session = sessionFactory.call();
            DBusConnection fdSession = sessionFactory.call();
            DBusConnection system = DBusConnectionBuilder.forSystemBus()
                    .withDisconnectCallback(new DBusDisconnectionLogger())
                    .build();
            return Optional.of(new XDGPortal(session, fdSession, system));
        } catch (Throwable t) {
            LOGGER.warn("Couldn't open D-Bus connection", t); // TODO remove after release
            return Optional.empty();
        }
    }

    private static class DBusDisconnectionLogger implements IDisconnectCallback {
        @Override
        public void disconnectOnError(IOException e) {
            LOGGER.error("DBus session terminated due to an error", e);
        }
    }

    public interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("c", CLibrary.class);

        int open(String pathname, int flags);

        int close(int fd);

        int O_RDONLY = 0;
        int O_CLOEXEC = 0x80000;     // 002000000 in octal
    }
}
