package net.legacylauncher.ipc;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * Basically a copy of {@link net.legacylauncher.ipc.Bootstrap1} interface, but we should not use Bootstrap1 directly as it requires java 11
 */
public interface BootstrapIPC extends Closeable {
    // general metadata block

    /**
     * @return general info about bootstrap in use
     */
    BootstrapRelease getBootstrapRelease();

    /**
     * @return arguments for a launcher
     */
    List<String> getLauncherArguments();

    /**
     * @return json object string with a launcher configuration
     */
    String getLauncherConfiguration();

    /**
     * Returns known release notes for given launcher version
     *
     * @param launcherVersion launcher version
     * @return release notes map, where keys are locales of the notes
     */
    Map<String, ReleaseNotes> getLauncherReleaseNotes(String launcherVersion);

    // launcher lifecycle block

    /**
     * Called early when launcher is about to start
     */
    void onBootStarted();

    /**
     * Called when launcher reached some stage in the boot procedure
     *
     * @param stepName   current step
     * @param percentage total boot progress, in range [0,1]
     */
    void onBootProgress(String stepName, double percentage);

    /**
     * Called after successful launch
     */
    void onBootSucceeded();


    /**
     * Launcher encountered unrecoverable error and cannot continue boot procedure
     *
     * @param message error message
     */
    void onBootError(String message);

    default void onBootError(Throwable e) {
        onBootError(e == null ? "unknown error" : e.getMessage());
    }

    /**
     * Called when launcher normally closing, bootstrap should shut down itself
     */
    void requestClose();

    // general purpose metadata block

    void setMetadata(String key, @Nullable Object value);

    @Nullable
    Object getMetadata(String key);

    class BootstrapRelease {
        public final String name;
        public final String version;

        public BootstrapRelease(String name, String version) {
            this.name = name;
            this.version = version;
        }
    }

    class ReleaseNotes {
        public final String title;
        public final String body;

        public ReleaseNotes(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }
}
