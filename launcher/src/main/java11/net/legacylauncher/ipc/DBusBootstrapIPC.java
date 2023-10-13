package net.legacylauncher.ipc;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DBusBootstrapIPC implements BootstrapIPC {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBusBootstrapIPC.class);
    private final DBusConnectionForwarder connection;
    private final Bootstrap1 ipc;
    private final Properties ipcProperties;

    public DBusBootstrapIPC(DBusConnectionForwarder connection) throws DBusException {
        this.connection = connection;
        this.ipc = connection.getRemoteObject(Bootstrap1.OBJECT_PATH, Bootstrap1.class);
        this.ipcProperties = connection.getRemoteObject(Bootstrap1.OBJECT_PATH, Properties.class);
    }

    @Override
    public BootstrapRelease getBootstrapRelease() {
        Object[] releaseRaw = ipcProperties.Get(Bootstrap1.INTERFACE_NAME, "BootstrapRelease");
        return new BootstrapRelease((String) releaseRaw[0], (String) releaseRaw[1]);
    }

    @Override
    public List<String> getLauncherArguments() {
        return ipcProperties.Get(Bootstrap1.INTERFACE_NAME, "LauncherArguments");
    }

    @Override
    public String getLauncherConfiguration() {
        return ipcProperties.Get(Bootstrap1.INTERFACE_NAME, "LauncherConfiguration");
    }

    @Override
    public Map<String, ReleaseNotes> getLauncherReleaseNotes(String launcherVersion) {
        return ipc.GetLauncherReleaseNotes(launcherVersion).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                (entry) -> new ReleaseNotes(entry.getValue().title, entry.getValue().body)));
    }

    @Override
    public void onBootStarted() {
        try {
            connection.sendMessage(new Launcher1.OnBootStarted(Launcher1.OBJECT_PATH));
        } catch (DBusException e) {
            // ¯\_(ツ)_/¯
            LOGGER.error("Failed to emit OnBootStarted event", e);
        }
    }

    @Override
    public void onBootProgress(String stepName, double percentage) {
        try {
            connection.sendMessage(new Launcher1.OnBootProgress(Launcher1.OBJECT_PATH, stepName, percentage));
        } catch (DBusException e) {
            // ¯\_(ツ)_/¯
            LOGGER.error("Failed to emit OnBootProgress event", e);
        }
    }

    @Override
    public void onBootSucceeded() {
        try {
            connection.sendMessage(new Launcher1.OnBootSucceeded(Launcher1.OBJECT_PATH));
        } catch (DBusException e) {
            // ¯\_(ツ)_/¯
            LOGGER.error("Failed to emit OnBootSucceeded event", e);
        }
    }

    @Override
    public void onBootError(String message) {
        try {
            connection.sendMessage(new Launcher1.OnBootError(Launcher1.OBJECT_PATH, message));
        } catch (DBusException e) {
            // ¯\_(ツ)_/¯
            LOGGER.error("Failed to emit OnBootError event", e);
        }
    }

    @Override
    public void requestClose() {
        try {
            connection.sendMessage(new Launcher1.OnCloseRequest(Launcher1.OBJECT_PATH));
        } catch (DBusException e) {
            // ¯\_(ツ)_/¯
            LOGGER.error("Failed to emit OnCloseRequest event", e);
        }
    }

    @Override
    public void setMetadata(String key, @Nullable Object value) {
        ipc.SetMetadata(key, new Variant<>(value == null ? "" : value));
    }

    @Override
    public Object getMetadata(String key) {
        Object metadata = ipc.GetMetadata(key).getValue();
        return "".equals(metadata) ? null : metadata;
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

    public void register(DBusConnectionForwarder forwarder) throws DBusException {
        forwarder.exportObject(Launcher1.OBJECT_PATH, new LauncherImpl());
    }

    private static class LauncherImpl implements Launcher1 {
    }
}
