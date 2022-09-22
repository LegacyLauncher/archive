package ru.turikhay.tlauncher.portals;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.freedesktop.dbus.FileDescriptor;
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

public class XDGPortal implements Portal, Closeable {
    private final DBusConnection connection;
    private final OpenURIInterface openURIInterface;
    private final SettingsInterface settingsInterface;

    private XDGPortal(DBusConnection connection) throws DBusException {
        this.connection = connection;
        this.openURIInterface = connection.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", OpenURIInterface.class);
        this.settingsInterface = connection.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", SettingsInterface.class);
    }

    @Override
    public void close() throws IOException {
        connection.close();
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
            openURIInterface.OpenDirectory("", new FileDescriptor(fd), Collections.emptyMap());
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
            openURIInterface.OpenFile("", new FileDescriptor(fd), Collections.emptyMap());
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
        try {
            DBusConnection connection = DBusConnectionBuilder.forSessionBus().build();
            return Optional.of(new XDGPortal(connection));
        } catch (Throwable t) {
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("c", CLibrary.class);

        int open(String pathname, int flags);

        int close(int fd);

        int O_PATH = 0x200000;     // 010000000 in octal
    }
}
