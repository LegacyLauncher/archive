package net.legacylauncher.bootstrap.task;

import net.legacylauncher.bootstrap.exception.FileLockedException;
import net.legacylauncher.bootstrap.util.Sha256Sign;
import net.legacylauncher.bootstrap.util.U;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DownloadTask extends Task<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadTask.class);
    private final List<URL> urlList;
    private final Path file;
    private final String sha256;

    public DownloadTask(String name, List<URL> urlList, Path file, String sha256) {
        super(name);

        if (Objects.requireNonNull(urlList, "urlList").isEmpty()) {
            throw new IllegalArgumentException("url list is empty");
        }

        this.urlList = new ArrayList<>(urlList);
        this.file = file;
        this.sha256 = sha256;
    }

    public DownloadTask(String name, URL url, Path file, String sha256) {
        this(name, Collections.singletonList(url), file, sha256);
    }

    @Override
    protected Void execute() throws Exception {
        updateProgress(-1.);

        if (Files.isRegularFile(file) && sha256 != null) {
            LOGGER.info("File exists, checking checksum: {}", sha256);
            String hash = Sha256Sign.calc(file);
            if (sha256.equalsIgnoreCase(hash)) {
                LOGGER.info("File is the same. Download skipped.");
                return null;
            } else {
                LOGGER.warn("File might be corrupted: {}", hash);
            }
        }

        Exception error = null;

        for (URL url : urlList) {
            try {
                downloadUrl(url);
            } catch (FileLockedException e) {
                LOGGER.error("File is locked", e);
                throw e;
            } catch (IOException e) {
                LOGGER.error("Failed to download: {}", url, e);
                if (error == null) {
                    error = e;
                } else {
                    error.addSuppressed(e);
                }
                continue;
            }
            return null;
        }
        if (error != null) {
            throw error;
        } else {
            throw new RuntimeException("Failed to download");
        }
    }

    protected void downloadUrl(URL url) throws IOException, TaskInterruptedException {
        LOGGER.info("Downloading: {}", url);

        URLConnection connection = url.openConnection(U.getProxy());
        double contentLength;
        Path temp = Files.createTempFile("tlauncher", null);

        try (InputStream in = connection.getInputStream();
             OutputStream out = Files.newOutputStream(temp)) {

            byte[] buffer = new byte[8192];
            long read = 0L;
            int i;

            contentLength = (double) connection.getContentLengthLong();

            while ((i = in.read(buffer)) >= 0) {
                out.write(buffer, 0, i);

                read += i;

                if (contentLength != 0.) {
                    updateProgress(read / contentLength);
                }

                checkInterrupted();
            }

            LOGGER.info("Downloaded {}  bytes out of {}", read, connection.getContentLengthLong());

            if (sha256 != null) {
                LOGGER.info("Checking SHA256... Expected: {}", sha256);
                out.close();

                String gotSha256 = Sha256Sign.calc(temp);
                LOGGER.info("Got: {}", gotSha256);

                if (!sha256.equalsIgnoreCase(gotSha256)) {
                    LOGGER.error("Invalid checksum");
                    throw new IOException("invalid checksum. expected: " + sha256 + "; got: " + gotSha256);
                }
            }

            LOGGER.info("Downloaded successfully, copying back...");

            out.close();

            boolean tryCopy = false;
            ArrayList<IOException> copyAttemptFailures = new ArrayList<>();
            do {
                try {
                    Files.createDirectories(file.getParent());
                    Files.copy(temp, file, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    if (copyAttemptFailures.size() == 5) {
                        LOGGER.error("File is locked. Giving up trying to copy it", e);
                        copyAttemptFailures.forEach(e::addSuppressed);
                        throw e;
                    }
                    copyAttemptFailures.add(e);

                    LOGGER.warn("File is probably locked. Let's wait some time...", e);

                    try {
                        Thread.sleep(FileLockedException.LOCK_COOLDOWN);
                    } catch (InterruptedException interrupted) {
                        throw new TaskInterruptedException(this);
                    }

                    checkInterrupted();

                    tryCopy = true; // let's try again
                }
            } while (tryCopy);
        } finally {
            Files.delete(temp);
        }
    }
}
