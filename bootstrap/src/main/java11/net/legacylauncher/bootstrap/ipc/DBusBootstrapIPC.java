package net.legacylauncher.bootstrap.ipc;

import net.legacylauncher.bootstrap.launcher.ForkStarter;
import net.legacylauncher.bootstrap.launcher.InProcessStarter;
import net.legacylauncher.bootstrap.launcher.LocalLauncher;
import net.legacylauncher.bootstrap.task.Task;
import net.legacylauncher.ipc.Bootstrap1;
import net.legacylauncher.ipc.DBusConnectionForwarder;
import net.legacylauncher.ipc.Launcher1;
import org.apache.commons.lang3.tuple.Pair;
import org.freedesktop.dbus.errors.PropertyReadOnly;
import org.freedesktop.dbus.errors.UnknownInterface;
import org.freedesktop.dbus.errors.UnknownProperty;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class DBusBootstrapIPC implements BootstrapIPC {

    private final String bootstrapVersion;
    private final List<String> launcherArgs;
    private final boolean fork;
    private String launcherConfiguration = "{}";
    private final Collection<Listener> listeners = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, Variant<?>> metadata = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, Bootstrap1.ReleaseNotes>> releaseNotes = new ConcurrentHashMap<>();

    private volatile State state = Running.INSTANCE;
    private final Lock stateLock = new ReentrantLock();
    private final Condition stoppedCondition = stateLock.newCondition();

    public DBusBootstrapIPC(String bootstrapVersion, String[] launcherArgs, boolean fork) {
        this.bootstrapVersion = bootstrapVersion;
        this.launcherArgs = Collections.unmodifiableList(Arrays.asList(launcherArgs));
        this.fork = fork;
    }

    @Override
    public void setMetadata(String key, Object value) {
        if (value == null) {
            metadata.remove(key);
        } else {
            metadata.put(key, new Variant<>(value));
        }
    }

    @Override
    public Object getMetadata(String key) {
        Variant<?> variant = metadata.get(key);
        if (variant == null || "".equals(variant.getValue())) return null;
        return variant.getValue();
    }

    @Override
    public void setLauncherConfiguration(String launcherConfiguration) {
        this.launcherConfiguration = launcherConfiguration;
    }

    @Override
    public void waitUntilClose() {
        stateLock.lock();
        try {
            if (state == Stopped.INSTANCE) {
                return;
            }
            stoppedCondition.await();
        } catch (InterruptedException ignored) {
        } finally {
            stateLock.unlock();
        }
    }

    public void requestClose() {
        stateLock.lock();
        try {
            if (!(state instanceof Running)) {
                // Don't override errors
                return;
            }
            state = Stopped.INSTANCE;
            listeners.forEach(Listener::onClosing);
            stoppedCondition.signalAll();
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public Task<Void> start(LocalLauncher localLauncher) {
        if (fork) {
            return ForkStarter.start(localLauncher, this);
        } else {
            return InProcessStarter.start(localLauncher, this);
        }
    }

    @Override
    public void addLauncherReleaseNotes(String launcherVersion, String title, Map<String, String> notes) {
        Map<String, Bootstrap1.ReleaseNotes> releaseNotes = notes.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), new Bootstrap1.ReleaseNotes(title, entry.getValue())))
                .collect(Collectors.toUnmodifiableMap(Pair::getKey, Pair::getValue));
        this.releaseNotes.put(launcherVersion, releaseNotes);
    }

    public void register(DBusConnectionForwarder forwarder) throws DBusException {
        forwarder.exportObject(Bootstrap1.OBJECT_PATH, new BootstrapImpl());
        forwarder.addSigHandler(Launcher1.OnBootStarted.class, signal -> {
            stateLock.lock();
            try {
                if (state != Running.INSTANCE) {
                    return;
                }
                listeners.forEach(Listener::onBootStarted);
            } finally {
                stateLock.unlock();
            }
        });
        forwarder.addSigHandler(Launcher1.OnBootProgress.class, signal -> {
            stateLock.lock();
            try {
                if (state != Running.INSTANCE) {
                    return;
                }
                listeners.forEach(listener -> listener.onBootProgress(signal.getStepName(), signal.getPercentage()));
            } finally {
                stateLock.unlock();
            }
        });
        forwarder.addSigHandler(Launcher1.OnBootSucceeded.class, signal -> {
            stateLock.lock();
            try {
                if (state != Running.INSTANCE) {
                    return;
                }
                listeners.forEach(Listener::onBootSucceeded);
            } finally {
                stateLock.unlock();
            }
        });
        forwarder.addSigHandler(Launcher1.OnBootError.class, signal -> {
            stateLock.lock();
            try {
                if (state instanceof Error) {
                    // Don't override errors
                    return;
                }
                state = new Error(signal.getMessage());
                listeners.forEach(listener -> listener.onBootError(signal.getMessage()));
                stoppedCondition.signalAll();
            } finally {
                stateLock.unlock();
            }
        });
        forwarder.addSigHandler(Launcher1.OnCloseRequest.class, signal -> DBusBootstrapIPC.this.requestClose());
    }

    private static abstract class State {

    }

    private final static class Running extends State {
        public static final Running INSTANCE = new Running();

        private Running() {
        }
    }

    private final static class Stopped extends State {
        public static final Stopped INSTANCE = new Stopped();

        private Stopped() {
        }
    }

    private final static class Error extends State {
        public final String message;

        public Error(String message) {
            this.message = message;
        }
    }

    private class BootstrapImpl implements Bootstrap1, Properties {
        @Override
        public Map<String, ReleaseNotes> GetLauncherReleaseNotes(String launcherVersion) {
            Map<String, ReleaseNotes> releaseNotes = DBusBootstrapIPC.this.releaseNotes.get(launcherVersion);
            return Objects.requireNonNullElse(releaseNotes, Collections.emptyMap());
        }

        @Override
        public void SetMetadata(String key, Variant<?> value) {
            metadata.put(key, value);
        }

        @Override
        public Variant<?> GetMetadata(String key) {
            Variant<?> variant = metadata.get(key);
            return variant == null ? new Variant<>("") : variant;
        }

        @Override
        @SuppressWarnings({"unchecked", "SwitchStatementWithTooFewBranches"})
        public <A> A Get(String _interfaceName, String _propertyName) {
            switch (_interfaceName) {
                case Bootstrap1.INTERFACE_NAME:
                    switch (_propertyName) {
                        case "BootstrapRelease":
                            return (A) getBootstrapRelease();
                        case "LauncherArguments":
                            return (A) launcherArgs.toArray(String[]::new);
                        case "LauncherConfiguration":
                            return (A) launcherConfiguration;
                        default:
                            throw new UnknownProperty("Unknown property '" + _propertyName + "' of interface '" + _interfaceName + "'");
                    }
                default:
                    throw new UnknownProperty("Unknown interface '" + _interfaceName + "'");
            }
        }

        private BootstrapRelease getBootstrapRelease() {
            return new BootstrapRelease("bootstrap-java-dbus", bootstrapVersion == null ? "unknown" : bootstrapVersion);
        }

        @Override
        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        public <A> void Set(String _interfaceName, String _propertyName, A _value) {
            switch (_interfaceName) {
                case Bootstrap1.INTERFACE_NAME:
                    switch (_propertyName) {
                        default:
                            throw new PropertyReadOnly("Property '" + _propertyName + "' is not writable.");
                    }
                default:
                    throw new UnknownProperty("Unknown interface '" + _interfaceName + "'");
            }

        }

        @Override
        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        public Map<String, Variant<?>> GetAll(String _interfaceName) {
            switch (_interfaceName) {
                case Bootstrap1.INTERFACE_NAME:
                    return Map.of(
                            "BootstrapRelease", new Variant<>(getBootstrapRelease()),
                            "LauncherArguments", new Variant<>(launcherArgs),
                            "LauncherConfiguration", new Variant<>(launcherConfiguration)
                    );
                default:
                    throw new UnknownInterface("Unknown interface '" + _interfaceName + "'");
            }
        }
    }
}
