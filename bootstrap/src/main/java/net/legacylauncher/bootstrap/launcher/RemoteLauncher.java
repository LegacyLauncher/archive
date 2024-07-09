package net.legacylauncher.bootstrap.launcher;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.bootstrap.meta.RemoteLauncherMeta;
import net.legacylauncher.bootstrap.task.DownloadTask;
import net.legacylauncher.bootstrap.task.Task;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Slf4j
public class RemoteLauncher extends LocalCastingLauncher {
    private final RemoteLauncherMeta meta;

    public RemoteLauncher(RemoteLauncherMeta meta) {
        this.meta = Objects.requireNonNull(meta, "RemoteLauncherMeta");
    }

    public Task<LocalLauncher> toLocalLauncher(final Path file, final Path libFolder) {
        return new Task<LocalLauncher>("remoteToLocalLauncher") {
            @Override
            protected LocalLauncher execute() throws Exception {
                log.info("Replacing local launcher with remote one");

                List<URL> urlList = Objects.requireNonNull(meta.getDownloads(), "remoteLauncher download list");
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
