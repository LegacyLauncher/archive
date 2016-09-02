package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.meta.RemoteLauncherMeta;
import ru.turikhay.tlauncher.bootstrap.task.DownloadTask;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.File;
import java.net.URL;
import java.util.List;

public class RemoteLauncher extends Launcher {
    private final RemoteLauncherMeta meta;

    public RemoteLauncher(RemoteLauncherMeta meta) {
        this.meta = U.requireNotNull(meta, "RemoteLauncherMeta");
    }

    public Task<LocalLauncher> toLocalLauncher(final File file) {
        return new Task<LocalLauncher>("remoteToLocalLauncher") {
            @Override
            protected LocalLauncher execute() throws Exception {
                log("Replacing local launcher with remote one");

                List<URL> urlList = U.requireNotNull(meta.getDownloads(), "remoteLauncher download list");

                for (URL url : urlList) {
                    log("Downloading remote one:" + url);
                    bindTo(new DownloadTask(url, file, meta.getChecksum()), 0., 1.);
                }

                return new LocalLauncher(file);
            }
        };
    }

    @Override
    public RemoteLauncherMeta getMeta() {
        return meta;
    }
}
