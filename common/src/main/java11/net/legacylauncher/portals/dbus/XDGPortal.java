package net.legacylauncher.portals.dbus;

import com.sun.jna.Library;
import com.sun.jna.Native;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.portals.Portal;
import org.freedesktop.dbus.DBusMatchRule;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.connections.IDisconnectCallback;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j
public class XDGPortal implements Portal {
    private final DBusConnection session;
    private final OpenURIInterface openURIInterface;
    private final SettingsInterface settingsInterface;
    private final int settingsVersion;
    private final List<AutoCloseable> closeables = new ArrayList<>();

    private XDGPortal(DBusConnection session) throws DBusException {
        this.session = session;
        this.openURIInterface = session.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", OpenURIInterface.class);
        this.settingsInterface = session.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", SettingsInterface.class);

        Properties props = session.getRemoteObject("org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", Properties.class);
        settingsVersion = props.<UInt32>Get("org.freedesktop.portal.Settings", "version").intValue();

        closeables.add(session.addSigHandler(
                new DBusMatchRule(RequestInterface.Response.class), (DBusSigHandler<RequestInterface.Response>) response -> {
                    log.debug("Received a signal: {}", response);
                    awaitResponseFor(new DBusPath(response.getPath())).complete(response);
                }
        ));

        closeables.add(session.addSigHandler(
                new DBusMatchRule(SettingsInterface.SettingChanged.class), (DBusSigHandler<SettingsInterface.SettingChanged>) event -> {
                    log.debug("Received a signal: {}", event);
                    if ("org.freedesktop.appearance".equals(event.getNamespace()) && "color-scheme".equals(event.getKey())) {
                        @SuppressWarnings("unchecked") ColorScheme colorScheme = parseColorScheme(((Variant<UInt32>) event.getValue()).getValue()).orLight();
                        colorSchemeListeners.forEach(callback -> {
                            callback.accept(colorScheme);
                        });
                    }
                }
        ));
    }

    @Override
    public void close() throws IOException {
        for (AutoCloseable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.warn("Unable to close closeable {}", closeable);
            }
        }
        closeables.clear();
        session.close();
    }

    @Override
    public boolean openURI(URI uri) {
        try {
            DBusPath requestPath = openURIInterface.OpenURI("", uri.toString(), Collections.emptyMap());
            return requestResult(requestPath);
        } catch (DBusExecutionException e) {
            return false;
        }
    }

    @Override
    public boolean openDirectory(Path path) {
        return openFileInternal(path, openURIInterface::OpenDirectory);
    }

    @Override
    public boolean openFile(Path path) {
        return openFileInternal(path, openURIInterface::OpenFile);
    }

    @FunctionalInterface
    private interface OpenFileFunction {
        DBusPath openFile(String parentWindow, FileDescriptor fd, Map<String, Variant<?>> options);
    }

    private boolean openFileInternal(Path path, OpenFileFunction function) {
        try {
            Map<String, Variant<?>> options = new HashMap<>();
            options.put("writable", new Variant<>(false));
            int fd = CLibrary.INSTANCE.open(path.toString(), CLibrary.O_RDONLY | CLibrary.O_CLOEXEC);
            if (fd < 0) {
                log.warn("Cannot open path {}", path);
                return false;
            }
            try {
                DBusPath requestPath = function.openFile("", new FileDescriptor(fd), options);
                return requestResult(requestPath);
            } finally {
                CLibrary.INSTANCE.close(fd);
            }
        } catch (DBusExecutionException e) {
            return false;
        }
    }

    private boolean requestResult(DBusPath requestPath) {
        CompletableFuture<RequestInterface.Response> future = awaitResponseFor(requestPath);
        try {
            RequestInterface.Response response = future.get(3, TimeUnit.SECONDS);
            log.debug("Got request result {}", response);
            return new UInt32(0).equals(response.getResponse());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Exception during waiting for response signal", e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public ColorScheme getColorScheme() {
        try {
            UInt32 colorScheme;
            if (settingsVersion >= 2) {
                colorScheme = settingsInterface.ReadOne("org.freedesktop.appearance", "color-scheme");
            } else {
                colorScheme = settingsInterface.<Variant<UInt32>>Read("org.freedesktop.appearance", "color-scheme").getValue();
            }
            return parseColorScheme(colorScheme).orLight();
        } catch (DBusExecutionException e) {
            log.warn("Cannot retrieve current color scheme from the desktop", e);
            return ColorScheme.NO_PREFERENCE;
        }
    }

    private final List<Consumer<ColorScheme>> colorSchemeListeners = new CopyOnWriteArrayList<>();

    @Override
    public AutoCloseable subscribeForColorSchemeChanges(Consumer<ColorScheme> callback) {
        colorSchemeListeners.add(callback);
        return () -> colorSchemeListeners.remove(callback);
    }

    private static ColorScheme parseColorScheme(UInt32 value) {
        switch (value.intValue()) {
            case 0:
            default:
                return ColorScheme.NO_PREFERENCE;
            case 1:
                return ColorScheme.PREFER_DARK;
            case 2:
                return ColorScheme.PREFER_LIGHT;
        }
    }


    public static Optional<Portal> tryToCreate() {
        Callable<DBusConnection> sessionFactory = () -> DBusConnectionBuilder.forSessionBus()
                .withDisconnectCallback(new DBusDisconnectionLogger())
                .build();
        try {
            return Optional.of(new XDGPortal(sessionFactory.call()));
        } catch (Throwable t) {
            log.warn("Couldn't open D-Bus connection", t); // TODO remove after release
            return Optional.empty();
        }
    }

    private static class DBusDisconnectionLogger implements IDisconnectCallback {
        @Override
        public void disconnectOnError(IOException e) {
            log.error("DBus session terminated due to an error", e);
        }
    }

    public interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("c", CLibrary.class);

        int open(String pathname, int flags);

        int close(int fd);

        int O_RDONLY = 0;
        int O_CLOEXEC = 0x80000;     // 002000000 in octal
    }

    private static final Map<DBusPath, CompletableFuture<RequestInterface.Response>> RESPONSES = new WeakHashMap<>();

    private static CompletableFuture<RequestInterface.Response> awaitResponseFor(DBusPath path) {
        synchronized (RESPONSES) {
            return RESPONSES.computeIfAbsent(path, p -> new CompletableFuture<>());
        }
    }
}
