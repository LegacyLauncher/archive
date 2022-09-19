package ru.turikhay.tlauncher.downloader;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.repository.IRepo;
import ru.turikhay.tlauncher.repository.RepositoryProxy;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class DownloaderThread extends ExtendedThread {
    private static final Logger LOGGER = LogManager.getLogger(DownloaderThread.class);

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
        unlockThread("iteration");
    }

    void stopDownload() {
        launched = false;
    }

    private boolean isHTML(File file) {
        byte[] buffer = new byte[HTML_SIGNATURE.length];

        try (InputStream is = new FileInputStream(file)) {
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
                current = d;
                onStart();
                int attempt = 0;
                Object error = null;

                int max = d.isFast() ? 2 : 5;
                long skip = 0, length = 0;

                while (attempt < max) {
                    ++attempt;
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Downloading {}{} [{} / {}]",
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
                        LOGGER.debug("Partially downloaded file: {}", partial.getMessage());
                        attempt = -1;
                        skip = partial.getNextSkip();
                        length = partial.getLength();
                    } catch (GaveUpDownloadException var9) {
                        LOGGER.debug("Cannot download file: {}", d.getURL());

                        skip = 0;
                        length = 0;

                        error = var9;
                        if (attempt >= max) {
                            FileUtil.deleteFile(d.getDestination());

                            for (File downloadable : d.getAdditionalDestinations()) {
                                FileUtil.deleteFile(downloadable);
                            }

                            LOGGER.debug("Gave up trying to download: {}", d.getURL());
                            onError(var9);
                        }
                    } catch (AbortedDownloadException var10) {
                        error = var10;
                        break;
                    }
                }

                if (error instanceof AbortedDownloadException) {
                    LOGGER.debug("Thread is aborting...");
                    for (Downloadable downloadable : list) {
                        downloadable.onAbort((AbortedDownloadException) error);
                    }
                }
            }

            speed = 0.0D;
            list.clear();
            lockThread("iteration");
            launched = false;
        }
    }

    private void download(int timeout, long skip, long length) throws PartialDownloadException, GaveUpDownloadException, AbortedDownloadException {
        Throwable cause = null;

        if (current.hasRepository()) {
            List<IRepo> list = current.getRepository().getRelevant().getList();
            int attempt = 1, max = list.size();

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
                    } catch (PartialDownloadException | AbortedDownloadException e) {
                        throw e;
                    } catch (IOException e) {
                        LOGGER.debug("Failed to download: {}",
                                connection == null ? current.getURL() : connection.getURL(), e);
                        current.getRepository().getList().markInvalid(repo);
                        if (cause == null) {
                            cause = e;
                        } else {
                            cause.addSuppressed(e);
                        }
                    } catch (Throwable e) {
                        Sentry.capture(new EventBuilder()
                                .withLevel(Event.Level.ERROR)
                                .withMessage("downloader exception: " + e)
                                .withSentryInterface(new ExceptionInterface(e))
                                .withExtra("current", current)
                        );
                        LOGGER.error("Unknown error occurred while downloading {}", current.getURL(), e);
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
            } catch (PartialDownloadException | AbortedDownloadException e) {
                throw e;
            } catch (IOException e) {
                LOGGER.debug("Failed to download: {}",
                        connection == null ? current.getURL() : connection.getURL(), e);
                cause = e;
            } catch (Throwable e) {
                Sentry.capture(new EventBuilder()
                        .withLevel(Event.Level.ERROR)
                        .withMessage("downloader exception: " + e)
                        .withSentryInterface(new ExceptionInterface(e))
                        .withExtra("current", current)
                );
                LOGGER.error("Unknown error occurred while downloading {}", current.getURL(), e);
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
        LOGGER.debug("Content type: {}", contentType);
        if (!current.getURL().endsWith("html") && "text/html".equalsIgnoreCase(contentType)) {
            throw new RetryDownloadException("requested file is html");
        }

        long reply = System.currentTimeMillis() - reply_s;
        LOGGER.debug("Replied in {} ms", reply);
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

        try (InputStream in = connection.getInputStream()) {
            int curread = in.read(buffer);
            try (OutputStream out = new FileOutputStream(temp, skip > 0)) {
                while (curread > -1) {
                    if (!launched) {
                        out.close();
                        throw new AbortedDownloadException();
                    }

                    totalRead += curread;
                    read += curread;
                    out.write(buffer, 0, curread);
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

                        if (speed_s - timer > 15000L) {
                            timer = speed_s;
                            LOGGER.info("Downloading {} [{}%, {} KiB/s]", connection.getURL(), downloadSpeed * 100., copies);
                        }

                        onProgress(downloadSpeed, copies);
                    }
                }
            }
        } finally {
            connection.disconnect();
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
        if (copies.size() > 0) {

            for (File copy : copies) {
                LOGGER.debug("Copying {} -> {}", file, copy);
                FileUtil.copyFile(file, copy, current.isForce());
            }

            LOGGER.debug("Copying completed.");
        }

        LOGGER.debug("Downloaded {} in {} at {} KiB/s",
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
                LOGGER.debug("Downloading: {}", connection.getURL());
                Downloadable.setUp(connection, timeout, current.getInsertUA());
                if (skip > 0) {
                    String range = skip + "-" + length;
                    LOGGER.debug("Requesting range {}", range);
                    connection.setRequestProperty("Range", "bytes=" + range);
                }
                if (!launched) {
                    throw new AbortedDownloadException();
                }
                connection.connect();
                if (skip > 0) {
                    if (connection.getResponseCode() != 206) {
                        throw new IOException("expected 206 response for partial content");
                    }
                }
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    if (responseCode == 301 || responseCode == 302 || responseCode == 307 || responseCode == 308) {
                        String newLocation = connection.getHeaderField("Location");
                        LOGGER.info("Following redirect ({}) {} -> {}", responseCode, connection.getURL(), newLocation);
                        if (!redirects.add(newLocation)) {
                            throw new IOException(String.format(Locale.ROOT, "circular redirect detected: %s (chain: %s)",
                                    newLocation, redirects));
                        }
                        connection.disconnect();
                        connection = openConnection(newLocation);
                        continue;
                    } else {
                        throw new IOException("expected 200 response; got " + connection.getResponseCode());
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
        speed = 0.005D * speed + 0.995D * curspeed;
        double lastProgress = currentProgress;
        downloader.onProgress(this, currentProgress, curdone, speed);
    }

    private void onComplete() throws RetryDownloadException {
        doneProgress += eachProgress;
        current.onComplete();
        downloader.onProgress(this, doneProgress, 1., speed);
        downloader.onFileComplete(this, current);
    }
}
