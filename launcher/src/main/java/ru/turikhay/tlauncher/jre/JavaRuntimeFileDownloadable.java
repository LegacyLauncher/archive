package ru.turikhay.tlauncher.jre;

import net.minecraft.launcher.updater.DownloadInfo;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tukaani.xz.LZMAInputStream;
import ru.turikhay.tlauncher.downloader.RetryDownloadException;
import ru.turikhay.tlauncher.downloader.Sha1Downloadable;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.async.AsyncThread;

import java.io.*;

public class JavaRuntimeFileDownloadable extends Sha1Downloadable {
    private static final Logger LOGGER = LogManager.getLogger(JavaRuntimeFileDownloadable.class);

    private final File lzmaDestination, destination;
    private final boolean executable;

    JavaRuntimeFileDownloadable(DownloadInfo info, boolean isLzma, File destination, boolean executable,
                                boolean forceDownload) {
        super(
                Repository.PROXIFIED_REPO,
                info.getUrl(),
                isLzma ? new File(destination.getAbsolutePath() + ".lzma") : destination,
                forceDownload
        );
        this.lzmaDestination = isLzma ? getDestination() : null;
        this.destination = destination;
        this.executable = executable;
        this.sha1 = info.getSha1();
        this.length = info.getSize();
    }

    @Override
    protected void onComplete() throws RetryDownloadException {
        super.onComplete();
        if (lzmaDestination != null) {
            syncExtract();
        }
        if (executable) {
            LOGGER.debug("Setting as executable: {}", destination.getAbsolutePath());
            destination.setExecutable(true); // probably safe to ignore
        }
    }

    private void syncExtract() throws RetryDownloadException {
        synchronized (JavaRuntimeFileDownloadable.class) {
            doExtract();
        }
    }

    private void doExtract() throws RetryDownloadException {
        LOGGER.debug("Extracting {}", lzmaDestination.getAbsolutePath());
        try (LZMAInputStream input = new LZMAInputStream(new BufferedInputStream(
                new FileInputStream(lzmaDestination)));
             OutputStream output = new BufferedOutputStream(new FileOutputStream(destination))
        ) {
            IOUtils.copy(input, output);
        } catch (IOException e) {
            throw new RetryDownloadException("couldn't unpack file", e);
        } finally {
            if (OS.WINDOWS.isCurrent()) {
                // Windows Defender or sth like that doesn't let us remove these files right away
                // It is tolerable if these files are not deleted. They just take up the space.
                AsyncThread.afterSeconds(10, () -> FileUtil.deleteFile(lzmaDestination));
            } else {
                FileUtil.deleteFile(lzmaDestination);
            }
        }
    }
}
