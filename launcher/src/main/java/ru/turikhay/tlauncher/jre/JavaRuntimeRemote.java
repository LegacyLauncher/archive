package ru.turikhay.tlauncher.jre;

import net.minecraft.launcher.updater.DownloadInfo;
import ru.turikhay.tlauncher.repository.RepositoryProxy;
import ru.turikhay.util.async.AsyncThread;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class JavaRuntimeRemote implements JavaRuntime {
    private String name;
    private String platform;
    private Availability availability;
    private DownloadInfo manifest;
    private VersionInfo version;

    private volatile Future<JavaRuntimeManifest> manifestTimeout;

    public String getName() {
        return name;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    public Availability getAvailability() {
        return availability;
    }

    public JavaRuntimeManifest getManifest() throws ExecutionException, InterruptedException {
        if (manifestTimeout == null) {
            manifestTimeout = AsyncThread.future(this::getManifestNow);
        }
        return manifestTimeout.get();
    }

    private JavaRuntimeManifest getManifestNow() throws IOException {
        JavaRuntimeManifest manifest = JavaRuntimeManifest.getGson().fromJson(
                RepositoryProxy.requestMaybeProxy(this.manifest.getUrl()),
                JavaRuntimeManifest.class
        );
        return Objects.requireNonNull(manifest, "manifest");
    }

    public VersionInfo getVersion() {
        return version;
    }

    public File getRuntimeDir(File rootDir) {
        return new File(rootDir, name + File.separatorChar + platform);
    }

    public File getWorkingDir(File rootDir) {
        return new File(getRuntimeDir(rootDir), name);
    }

    public JavaRuntimeLocal toLocal(File rootDir) {
        return new JavaRuntimeLocal(
                name,
                platform,
                getRuntimeDir(rootDir)
        );
    }

    void setName(String name) {
        this.name = name;
    }

    void setPlatform(String platform) {
        this.platform = platform;
    }

    public static class Availability {
        private int group;
        private int progress;

        public int getGroup() {
            return group;
        }

        public int getProgress() {
            return progress;
        }
    }

    public static class VersionInfo {
        private String name;
        private Date released;

        public String getName() {
            return name;
        }

        public Date getReleased() {
            return released;
        }
    }
}
