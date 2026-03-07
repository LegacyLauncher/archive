package net.legacylauncher.downloader;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.common.exceptions.LocalIOException;
import net.legacylauncher.repository.IRepo;
import net.legacylauncher.repository.RepositoryProxy;
import net.legacylauncher.util.FileUtil;
import net.legacylauncher.util.U;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Slf4j
public class DownloaderTask implements Runnable {
    private static final double SMOOTHING_FACTOR = 0.005D;
    private static final int NOTIFY_TIMER = 15000;
    private static final byte[] HTML_SIGNATURE = "<!DOCTYPE".getBytes(StandardCharsets.UTF_8);

    private final int id;
    private final Downloadable downloadable;
    private final Downloader downloader;
    private double speed;

    DownloaderTask(int id, Downloadable downloadable, Downloader downloader) {
        this.id = id;
        this.downloadable = downloadable;
        this.downloader = downloader;
    }

    private static HttpURLConnection openConnection(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    public int getId() {
        return id;
    }

    private boolean isHTML(File file) {
        byte[] buffer = new byte[HTML_SIGNATURE.length];

        try (InputStream is = Files.newInputStream(file.toPath())) {
            int read = 0;
            while (read < HTML_SIGNATURE.length) {
                int i = is.read(buffer, read, buffer.length - read);
                if (i < 0) break;
                read += i;
            }
        } catch (IOException e) {
            return false;
        }

        return Arrays.equals(buffer, HTML_SIGNATURE);
    }

    @Override
    public void run() {
        if (downloader.isAborted()) return;

        onStart();
        int attempt = 0;
        Object error = null;

        int max = downloadable.isFast() ? 2 : 5;
        long skip = 0, length = 0;

        while (attempt < max) {
            ++attempt;
            if (log.isTraceEnabled()) {
                log.trace("Downloading {}{} [{} / {}]",
                        downloadable.getURL(),
                        downloadable.hasRepository() ? " (repo: " + downloadable.getRepository().name() + ")" : "",
                        attempt, max
                );
            }
            int timeout = attempt * U.getConnectionTimeout();

            try {
                download(timeout, skip, length);
                break;
            } catch (PartialDownloadException partial) {
                log.debug("Partially downloaded file: {}", partial.getMessage());
                attempt = -1;
                skip = partial.getNextSkip();
                length = partial.getLength();
            } catch (GaveUpDownloadException e) {
                log.debug("Cannot download file: {}", downloadable.getURL());

                skip = 0;
                length = 0;

                error = e;
                if (attempt >= max) {
                    FileUtil.deleteFile(downloadable.getDestination());

                    for (File downloadable : downloadable.getAdditionalDestinations()) {
                        FileUtil.deleteFile(downloadable);
                    }

                    log.debug("Gave up trying to download: {}", downloadable.getURL());
                    onError(e);
                }
            } catch (AbortedDownloadException e) {
                error = e;
                break;
            }
        }

        if (error instanceof AbortedDownloadException) {
            log.debug("Thread is aborting...");
            downloadable.onAbort((AbortedDownloadException) error);
        }

        speed = 0.0D;
    }

    private void download(int timeout, long skip, long length) throws PartialDownloadException, GaveUpDownloadException, AbortedDownloadException {
        Throwable cause = null;

        if (downloadable.hasRepository()) {
            List<IRepo> list = downloadable.getRepository().getRelevant().getList();
            int attempt = 1, max = 2;

            while (attempt <= max) {
                for (IRepo repo : list) {
                    URLConnection connection = null;
                    try {
                        if (repo instanceof RepositoryProxy.ProxyRepo) {
                            connection = ((RepositoryProxy.ProxyRepo) repo)
                                    .get(downloadable.getURL(), attempt * U.getConnectionTimeout(), attempt);
                        } else {
                            connection = repo.get(downloadable.getURL(), attempt * U.getConnectionTimeout());
                        }
                        downloadURL(connection, timeout, skip, length);
                        return;
                    } catch (PartialDownloadException | GaveUpDownloadException | AbortedDownloadException e) {
                        throw e;
                    } catch (LocalIOException e) {
                        throw new GaveUpDownloadException(downloadable, e);
                    } catch (IOException e) {
                        log.debug("Failed to download: {}",
                                connection == null ? downloadable.getURL() : connection.getURL(), e);
                        if (!(e instanceof InvalidResponseCodeException) || !((InvalidResponseCodeException) e).isClientError()) {
                            // only mark repo as invalid if it's server error
                            downloadable.getRepository().getList().markInvalid(repo);
                        }
                        if (cause == null) {
                            cause = e;
                        } else {
                            cause.addSuppressed(e);
                        }
                    } catch (Throwable e) {
                        log.error("Unknown error occurred while downloading {}", downloadable.getURL(), e);
                        if (cause == null) {
                            cause = e;
                        } else {
                            cause.addSuppressed(e);
                        }
                    }
                }
                attempt++;
            }
        } else {
            URLConnection connection = null;
            try {
                connection = openConnection(downloadable.getURL());
                downloadURL(connection, timeout, skip, length);
                return;
            } catch (LocalIOException e) {
                throw new GaveUpDownloadException(downloadable, e);
            } catch (PartialDownloadException | GaveUpDownloadException | AbortedDownloadException e) {
                throw e;
            } catch (IOException e) {
                log.debug("Failed to download: {}",
                        connection == null ? downloadable.getURL() : connection.getURL(), e);
                cause = e;
            } catch (Throwable e) {
                log.error("Unknown error occurred while downloading {}", downloadable.getURL(), e);
                cause = e;
            }
        }

        throw new GaveUpDownloadException(downloadable, cause);
    }

    private void downloadURL(URLConnection urlConnection, int timeout, long skip, long length) throws IOException, AbortedDownloadException {
        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IOException("invalid protocol");
        }

        long reply_s = System.currentTimeMillis();

        HttpURLConnection connection = setupConnectionFollowingRedirects((HttpURLConnection) urlConnection, timeout, skip, length);

        String contentType = connection.getHeaderField("Content-Type");
        log.debug("Content type: {}", contentType);
        if (!downloadable.getURL().endsWith("html") && "text/html".equalsIgnoreCase(contentType)) {
            throw new RetryDownloadException("requested file is html");
        }

        long reply = System.currentTimeMillis() - reply_s;
        log.debug("Replied in {} ms", reply);
        File file = downloadable.getDestination();

        File temp = new File(file.getAbsoluteFile() + ".download");
        if (skip == 0) {
            if (temp.isFile()) {
                FileUtil.deleteFile(temp);
            }
            FileUtil.createFile(temp);
        } else {
            if (!temp.isFile()) {
                throw new FileNotFoundException("no partial file: " + temp.getAbsolutePath());
            }
            if (temp.length() != 0 && temp.length() != skip) {
                throw new IOException("bad partial file length: " + temp.length() + " (" + skip + " required)");
            }
        }

        long totalRead = skip, read = 0;
        long contentLength = connection.getContentLengthLong();
        if (length == 0) {
            length = contentLength;
        }
        long downloaded_s = System.currentTimeMillis();
        long speed_s = downloaded_s;
        long timer = downloaded_s;
        byte[] buffer = new byte[8192];
        long downloaded_e;
        double downloadSpeed;

        FileOutputStream out = null;
        try (InputStream in = connection.getInputStream()) {
            int curread = in.read(buffer);
            try {
                out = new FileOutputStream(temp, skip > 0);
            } catch (IOException ioE) {
                throw new LocalIOException(temp.getAbsolutePath(), ioE);
            }
            while (curread > -1) {
                totalRead += curread;
                read += curread;
                try {
                    out.write(buffer, 0, curread);
                } catch (IOException ioE) {
                    throw new LocalIOException(temp.getAbsolutePath(), ioE);
                }
                curread = in.read(buffer);
                if (curread == -1) {
                    break;
                }

                long speed_e = System.currentTimeMillis() - speed_s;
                if (speed_e >= 50L) {
                    speed_s = System.currentTimeMillis();
                    downloaded_e = speed_s - downloaded_s;
                    downloadSpeed = length > 0L ? (double) ((float) totalRead / (float) length) : 0.0D;
                    double copies = downloaded_e > 0L ? (double) totalRead / (double) downloaded_e : 0.0D;

                    if (speed_s - timer > NOTIFY_TIMER) {
                        timer = speed_s;
                        log.info("Downloading {} [{}%, {} KiB/s]", connection.getURL(),
                                String.format(Locale.ROOT, "%.0f", downloadSpeed * 100.), copies);
                    }

                    onProgress(downloadSpeed, copies);
                }
            }
        } finally {
            connection.disconnect();
            IOUtils.closeQuietly(out);
        }

        if (length > 0 && totalRead != length) {
            if (skip == 0) {
                String partialInfo = "read " + totalRead + " out of " + length;
                if (!"bytes".equals(connection.getHeaderField("Accept-Ranges"))) {
                    throw new IOException("server doesn't support partial download. " + partialInfo);
                }
            }
            throw new PartialDownloadException(skip, read, length);
        }

        downloaded_e = System.currentTimeMillis() - downloaded_s;
        downloadSpeed = downloaded_e != 0L ? (double) totalRead / (double) downloaded_e : 0.0D;
        FileUtil.copyFile(temp, file, true);
        FileUtil.deleteFile(temp);

        if (isHTML(file)) {
            throw new RetryDownloadException("Downloaded file is HTML");
        }

        List<File> copies = downloadable.getAdditionalDestinations();
        if (!copies.isEmpty()) {

            for (File copy : copies) {
                log.debug("Copying {} -> {}", file, copy);
                FileUtil.copyFile(file, copy, downloadable.isForce());
            }

            log.debug("Copying completed.");
        }

        log.debug("Downloaded {} in {} at {} KiB/s",
                totalRead / 1024L + " KiB",
                downloaded_e + " ms",
                String.format(Locale.ROOT, "%.0f", downloadSpeed));
        onComplete();
    }

    private HttpURLConnection setupConnectionFollowingRedirects(HttpURLConnection connection, int timeout, long skip, long length) throws IOException, AbortedDownloadException {
        boolean connected = false;
        try {
            Set<String> redirects = new LinkedHashSet<>();
            do {
                log.debug("Downloading: {}", connection.getURL());
                Downloadable.setUp(connection, timeout, downloadable.getInsertUA());
                if (skip > 0) {
                    String range = skip + "-" + length;
                    log.debug("Requesting range {}", range);
                    connection.setRequestProperty("Range", "bytes=" + range);
                }
                connection.connect();
                if (skip > 0) {
                    if (connection.getResponseCode() != 206) {
                        throw new InvalidResponseCodeException(connection.getURL().toString(), connection.getResponseCode(), 206);
                    }
                }
                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode > 299) {
                    if (responseCode == 301 || responseCode == 302 || responseCode == 307 || responseCode == 308) {
                        String newLocation = connection.getHeaderField("Location");
                        log.info("Following redirect ({}) {} -> {}", responseCode, connection.getURL(), newLocation);
                        if (!redirects.add(newLocation)) {
                            throw new IOException(String.format(Locale.ROOT, "circular redirect detected: %s (chain: %s)",
                                    newLocation, redirects));
                        }
                        connection.disconnect();
                        connection = openConnection(newLocation);
                        continue;
                    } else {
                        throw new InvalidResponseCodeException(connection.getURL().toString(), connection.getResponseCode());
                    }
                }
                connected = true;
                return connection;
            } while (redirects.size() < 10);

            throw new IOException("too many redirects: " + redirects);
        } finally {
            if (!connected) {
                connection.disconnect();
            }
        }
    }

    private void onStart() {
        downloadable.onStart();
    }

    private void onError(Throwable e) {
        downloadable.onError(e);
        downloader.onFileComplete(this, downloadable);
    }

    private void onProgress(double curdone, double curspeed) {
        speed = SMOOTHING_FACTOR * speed + (1 - SMOOTHING_FACTOR) * curspeed;
        downloader.onProgress(this, curdone, speed);
    }

    private void onComplete() throws IOException {
        downloadable.onComplete();
        downloader.onProgress(this, 1.0D, speed);
        downloader.onFileComplete(this, downloadable);
    }
}
