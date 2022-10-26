package ru.turikhay.tlauncher.portals;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.connections.IDisconnectCallback;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.errors.ServiceUnknown;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import ru.turikhay.tlauncher.portals.dbus.OpenURIInterface;
import ru.turikhay.tlauncher.portals.dbus.SettingsInterface;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;

public class XDGPortal implements Portal, Closeable {
    private final DBusConnection connection, fdConnection;
    private final OpenURIInterface openURIInterface, fdOpenURIInterface;
    private final SettingsInterface settingsInterface;

    private XDGPortal(DBusConnection connection, DBusConnection fdConnection) throws DBusException {
        this.connection = connection;
        this.fdConnection = fdConnection;
        this.openURIInterface = connection.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", OpenURIInterface.class);
        this.fdOpenURIInterface = fdConnection.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", OpenURIInterface.class);
        this.settingsInterface = connection.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", SettingsInterface.class);
    }

    @Override
    public void close() throws IOException {
        connection.close();
        fdConnection.close();
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
            int fd = CLibrary.INSTANCE.open(path.toString(), CLibrary.O_PATH);
            fdOpenURIInterface.OpenDirectory("", new FileDescriptor(fd), Collections.emptyMap());
            CLibrary.INSTANCE.close(fd);
            return true;
        } catch (ServiceUnknown e) {
            return false;
        }
    }

    @Override
    public boolean openFile(Path path) {
        try {
            int fd = CLibrary.INSTANCE.open(path.toString(), CLibrary.O_PATH);
            fdOpenURIInterface.OpenFile("", new FileDescriptor(fd), Collections.emptyMap());
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

    public static Optional<Portal> tryToCreate() {
        Callable<DBusConnection> connectionFactory = () -> DBusConnectionBuilder.forSessionBus()
                .withShared(false)
                .withDisconnectCallback(new DBusDisconnectionLogger())
                .build();

        try {
            DBusConnection connection = connectionFactory.call();
            DBusConnection fdConnection = connectionFactory.call();
            return Optional.of(new XDGPortal(connection, fdConnection));
        } catch (Throwable t) {
            t.printStackTrace(); // TODO remove after release
            return Optional.empty();
        }
    }

    private static class DBusDisconnectionLogger implements IDisconnectCallback {
        @Override
        public void disconnectOnError(IOException e) {
            System.err.println("DBus session terminated due to error:");
            e.printStackTrace(System.err);
        }
    }

    public interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("c", CLibrary.class);

        int open(String pathname, int flags);

        int close(int fd);

        int O_PATH = 0x200000;     // 010000000 in octal
    }
}
