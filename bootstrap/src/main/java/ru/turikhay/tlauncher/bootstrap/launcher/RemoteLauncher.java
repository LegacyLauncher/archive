package ru.turikhay.tlauncher.bootstrap.launcher;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.meta.RemoteLauncherMeta;
import ru.turikhay.tlauncher.bootstrap.task.DownloadTask;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.File;
import java.net.URL;
import java.util.List;

public class RemoteLauncher extends LocalCastingLauncher {
    private final RemoteLauncherMeta meta;

    public RemoteLauncher(RemoteLauncherMeta meta) {
        this.meta = U.requireNotNull(meta, "RemoteLauncherMeta");
    }

    public Task<LocalLauncher> toLocalLauncher(final File file, final File libFolder) {
        return new Task<LocalLauncher>("remoteToLocalLauncher") {
            @Override
            protected LocalLauncher execute() throws Exception {
                log("Replacing local launcher with remote one");

                List<URL> urlList = U.requireNotNull(meta.getDownloads(), "remoteLauncher download list");
                bindTo(new DownloadTask("remoteLauncherDownload", urlList, file, meta.getChecksum()), 0., 1.);

                return new LocalLauncher(file, libFolder);
            }
        };
    }

    @Override
    public RemoteLauncherMeta getMeta() {
        return meta;
    }

    @Override
    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("meta", meta);
    }
}
