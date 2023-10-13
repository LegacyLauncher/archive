package net.legacylauncher.bootstrap.ipc;

import net.legacylauncher.bootstrap.launcher.LocalLauncher;
import net.legacylauncher.bootstrap.launcher.SharedClassesStarter;
import net.legacylauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.bridge.BootException;
import ru.turikhay.tlauncher.bootstrap.bridge.BootListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BridgedBootstrapIPC implements BootstrapIPC {
    private final BootBridge bootBridge;

    public BridgedBootstrapIPC(String bootstrapVersion, String[] launcherArgs) {
        bootBridge = new BootBridge(bootstrapVersion, launcherArgs);
    }

    @Override
    public Object getMetadata(String key) {
        if ("client".equals(key)) {
            UUID client = bootBridge.getClient();
            if (client == null) return null;
            return client.toString();
        }
        return bootBridge.getCapabilities().get(key);
    }

    @Override
    public void setMetadata(String key, Object value) {
        if ("client".equals(key)) {
            throw new IllegalArgumentException("Client should be set by launcher, not bootstrap!");
        }
        bootBridge.addCapability(key, value);
    }

    @Override
    public void setLauncherConfiguration(String launcherConfiguration) {
        bootBridge.setOptions(launcherConfiguration);
    }

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
        bootBridge.addListener(new BootListener() {
            @Override
            public void onBootStarted() {
                listener.onBootStarted();
            }

            @Override
            public void onBootStateChanged(String stepName, double percentage) {
                listener.onBootProgress(stepName, percentage);
            }

            @Override
            public void onBootSucceeded() {
                listener.onBootSucceeded();
            }

            @Override
            public void onBootErrored(Throwable t) {
                listener.onBootError(t);
            }
        });
    }

    @Override
    public void addLauncherReleaseNotes(String launcherVersion, String title, Map<String, String> notes) {
        for (Map.Entry<String, String> entry : notes.entrySet()) {
            bootBridge.addMessage(entry.getKey(), title, entry.getValue());
        }
    }

    @Override
    public Task<Void> start(LocalLauncher localLauncher) {
        return SharedClassesStarter.start(localLauncher, bootBridge);
    }

    @Override
    public void waitUntilClose() {
        try {
            bootBridge.waitUntilClose();
            listeners.forEach(Listener::onClosing);
        } catch (InterruptedException e) {
            throw new RuntimeException("boot process interrupted", e);
        } catch (BootException e) {
            throw new RuntimeException("boot process has failed", e);
        }
    }
}
