package ru.turikhay.tlauncher.portals;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.connections.IDisconnectCallback;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.tlauncher.portals.dbus.OpenURIInterface;
import ru.turikhay.tlauncher.portals.dbus.SettingsInterface;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class XDGPortal implements Portal, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(XDGPortal.class);

    private final DBusConnection session;
    private final OpenURIInterface openURIInterface;
    private final SettingsInterface settingsInterface;

    private XDGPortal(DBusConnection session) throws DBusException {
        this.session = session;
        this.openURIInterface = session.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", OpenURIInterface.class);
        this.settingsInterface = session.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", SettingsInterface.class);
    }

    @Override
    public void close() throws IOException {
        session.close();
    }

    @Override
    public boolean openURI(URI uri) {
        try {
            openURIInterface.OpenURI("", uri.toString(), Collections.emptyMap());
            return true;
        } catch (DBusExecutionException e) {
            return false;
        }
    }

    @Override
    public boolean openDirectory(Path path) {
        try {
            Map<String, Variant<?>> options = new HashMap<>();
            options.put("writable", new Variant<>(false));
            int fd = CLibrary.INSTANCE.open(path.toString(), CLibrary.O_RDONLY | CLibrary.O_CLOEXEC);
            openURIInterface.OpenDirectory("", new FileDescriptor(fd), options);
            CLibrary.INSTANCE.close(fd);
            return true;
        } catch (DBusExecutionException e) {
            return false;
        }
    }

    @Override
    public boolean openFile(Path path) {
        try {
            Map<String, Variant<?>> options = new HashMap<>();
            options.put("writable", new Variant<>(false));
            int fd = CLibrary.INSTANCE.open(path.toString(), CLibrary.O_RDONLY | CLibrary.O_CLOEXEC);
            openURIInterface.OpenFile("", new FileDescriptor(fd), options);
            CLibrary.INSTANCE.close(fd);
            return true;
        } catch (DBusExecutionException e) {
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
        } catch (DBusExecutionException e) {
            return ColorScheme.NO_PREFERENCE;
        }
    }


    public static Optional<Portal> tryToCreate() {
        Callable<DBusConnection> sessionFactory = () -> DBusConnectionBuilder.forSessionBus()
                .withDisconnectCallback(new DBusDisconnectionLogger())
                .build();
        try {
            return Optional.of(new XDGPortal(sessionFactory.call()));
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
