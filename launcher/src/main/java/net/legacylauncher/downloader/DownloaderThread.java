package net.legacylauncher.downloader;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.common.exceptions.LocalIOException;
import net.legacylauncher.repository.IRepo;
import net.legacylauncher.repository.RepositoryProxy;
import net.legacylauncher.util.FileUtil;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.ExtendedThread;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;

@Slf4j
public class DownloaderThread extends ExtendedThread {
    private static final double SMOOTHING_FACTOR = 0.005D;
    private static final String ITERATION_BLOCK = "iteration";
    private static final int NOTIFY_TIMER = 15000;
    private final int ID;
    private final Downloader downloader;
    private final List<Downloadable> list;
    private double currentProgress;
    private double doneProgress;
    private double eachProgress;
    private double speed;
    private Downloadable current;
    private boolean launched;
    private final byte[] HTML_SIGNATURE = {(byte) 0x3c, (byte) 0x21, (byte) 0x44, (byte) 0x4f, (byte) 0x43, (byte) 0x54, (byte) 0x59, (byte) 0x50, (byte) 0x45}; // <!DOCTYPE

    DownloaderThread(Downloader d, int id) {
        super("DT#" + id);
        ID = id;
        downloader = d;
        list = new ArrayList<>();
        startAndWait();
    }

    int getID() {
        return ID;
    }

    void add(Downloadable d) {
        list.add(d);
    }

    void startDownload() {
        launched = true;
        unlockThread(ITERATION_BLOCK);
    }

    void stopDownload() {
        launched = false;
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

    public void run() {
        while (true) {
            launched = true;
            eachProgress = 1.0D / (double) list.size();
            currentProgress = doneProgress = 0.0D;

            for (Downloadable d : list) {
                if (!launched) {
                    break;
                }

                current = d;
                onStart();
                int attempt = 0;
                Object error = null;

                int max = d.isFast() ? 2 : 5;
                long skip = 0, length = 0;

                while (attempt < max) {
                    ++attempt;
                    if (log.isTraceEnabled()) {
                        log.trace("Downloading {}{} [{} / {}]",
                                d.getURL(),
                                d.hasRepository() ? " (repo: " + d.getRepository().name() + ")" : "",
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
                        log.debug("Cannot download file: {}", d.getURL());

                        skip = 0;
                        length = 0;

                        error = e;
                        if (attempt >= max) {
                            FileUtil.deleteFile(d.getDestination());

                            for (File downloadable : d.getAdditionalDestinations()) {
                                FileUtil.deleteFile(downloadable);
                            }

                            log.debug("Gave up trying to download: {}", d.getURL());
                            onError(e);
                        }
                    } catch (AbortedDownloadException e) {
                        error = e;
                        break;
                    }
                }

                if (error instanceof AbortedDownloadException) {
                    log.debug("Thread is aborting...");
                    for (Downloadable downloadable : list) {
                        downloadable.onAbort((AbortedDownloadException) error);
                    }
                }
            }

            speed = 0.0D;
            list.clear();
            lockThread(ITERATION_BLOCK);
            launched = false;
        }
    }

    private void download(int timeout, long skip, long length) throws PartialDownloadException, GaveUpDownloadException, AbortedDownloadException {
        Throwable cause = null;

        if (current.hasRepository()) {
            List<IRepo> list = current.getRepository().getRelevant().getList();
            int attempt = 1, max = 2;

            while (attempt <= max) {
                for (IRepo repo : list) {
                    URLConnection connection = null;
                    try {
                        if (repo instanceof RepositoryProxy.ProxyRepo) {
                            connection = ((RepositoryProxy.ProxyRepo) repo)
                                    .get(current.getURL(), attempt * U.getConnectionTimeout(), U.getProxy(), attempt);
                        } else {
                            connection = repo.get(current.getURL(), attempt * U.getConnectionTimeout(), U.getProxy());
                        }
                        downloadURL(connection, timeout, skip, length);
                        return;
                    } catch (PartialDownloadException | GaveUpDownloadException | AbortedDownloadException e) {
                        throw e;
                    } catch (LocalIOException e) {
                        throw new GaveUpDownloadException(current, e);
                    } catch (IOException e) {
                        log.debug("Failed to download: {}",
                                connection == null ? current.getURL() : connection.getURL(), e);
                        if (!(e instanceof InvalidResponseCodeException) || !((InvalidResponseCodeException) e).isClientError()) {
                            // only mark repo as invalid if it's server error
                            current.getRepository().getList().markInvalid(repo);
                        }
                        if (cause == null) {
                            cause = e;
                        } else {
                            cause.addSuppressed(e);
                        }
                    } catch (Throwable e) {
                        log.error("Unknown error occurred while downloading {}", current.getURL(), e);
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
                connection = openConnection(current.getURL());
                downloadURL(connection, timeout, skip, length);
                return;
            } catch (LocalIOException e) {
                throw new GaveUpDownloadException(current, e);
            } catch (PartialDownloadException | GaveUpDownloadException | AbortedDownloadException e) {
                throw e;
            } catch (IOException e) {
                log.debug("Failed to download: {}",
                        connection == null ? current.getURL() : connection.getURL(), e);
                cause = e;
            } catch (Throwable e) {
                log.error("Unknown error occurred while downloading {}", current.getURL(), e);
                cause = e;
            }
        }

        throw new GaveUpDownloadException(current, cause);
    }

    private static HttpURLConnection openConnection(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection(U.getProxy());
    }

    private void downloadURL(URLConnection urlConnection, int timeout, long skip, long length) throws IOException, AbortedDownloadException {
        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IOException("invalid protocol");
        }

        long reply_s = System.currentTimeMillis();

        HttpURLConnection connection = setupConnectionFollowingRedirects((HttpURLConnection) urlConnection, timeout, skip, length);

        String contentType = connection.getHeaderField("Content-Type");
        log.debug("Content type: {}", contentType);
        if (!current.getURL().endsWith("html") && "text/html".equalsIgnoreCase(contentType)) {
            throw new RetryDownloadException("requested file is html");
        }

        long reply = System.currentTimeMillis() - reply_s;
        log.debug("Replied in {} ms", reply);
        File file = current.getDestination();

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
                if (!launched) {
                    out.close();
                    throw new AbortedDownloadException();
                }

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

        List<File> copies = current.getAdditionalDestinations();
        if (!copies.isEmpty()) {

            for (File copy : copies) {
                log.debug("Copying {} -> {}", file, copy);
                FileUtil.copyFile(file, copy, current.isForce());
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
                Downloadable.setUp(connection, timeout, current.getInsertUA());
                if (skip > 0) {
                    String range = skip + "-" + length;
                    log.debug("Requesting range {}", range);
                    connection.setRequestProperty("Range", "bytes=" + range);
                }
                if (!launched) {
                    throw new AbortedDownloadException();
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
        current.onStart();
    }

    private void onError(Throwable e) {
        current.onError(e);
        downloader.onFileComplete(this, current);
    }

    double curdone;

    private void onProgress(double curdone, double curspeed) {
        this.curdone = curdone;
        currentProgress = doneProgress + eachProgress * curdone;
        speed = SMOOTHING_FACTOR * speed + (1 - SMOOTHING_FACTOR) * curspeed;
        double lastProgress = currentProgress;
        downloader.onProgress(this, currentProgress, curdone, speed);
    }

    private void onComplete() throws IOException {
        doneProgress += eachProgress;
        current.onComplete();
        downloader.onProgress(this, doneProgress, 1., speed);
        downloader.onFileComplete(this, current);
    }
}
