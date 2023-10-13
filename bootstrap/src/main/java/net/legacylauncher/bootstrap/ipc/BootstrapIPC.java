package net.legacylauncher.bootstrap.ipc;

import net.legacylauncher.bootstrap.launcher.LocalLauncher;
import net.legacylauncher.bootstrap.task.Task;

import java.util.Map;

public interface BootstrapIPC {

    void setMetadata(String key, Object value);

    Object getMetadata(String key);

    void setLauncherConfiguration(String launcherConfiguration);

    void waitUntilClose();

    void addListener(Listener listener);

    Task<Void> start(LocalLauncher localLauncher);

    void addLauncherReleaseNotes(String launcherVersion, String title, Map<String, String> notes);

    interface Listener {
        default void onBootStarted() {
        }

        default void onBootProgress(String stepName, double percentage) {
        }

        default void onBootSucceeded() {
        }

        default void onBootError(String message) {
        }

        default void onBootError(Throwable t) {
            onBootError(t.getMessage());
        }

        default void onClosing() {}
    }
}
