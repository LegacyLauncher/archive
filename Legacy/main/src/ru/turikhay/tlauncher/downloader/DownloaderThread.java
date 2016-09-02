package ru.turikhay.tlauncher.downloader;

import ru.turikhay.tlauncher.exceptions.IOExceptionList;
import ru.turikhay.tlauncher.repository.IRepo;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class DownloaderThread extends ExtendedThread {
    private static final double SMOOTHING_FACTOR = 0.005D;
    private static final String ITERATION_BLOCK = "iteration";
    private static final int NOTIFY_TIMER = 15000;
    private final int ID;
    private final String LOGGER_PREFIX;
    private final Downloader downloader;
    private final List<Downloadable> list;
    private double currentProgress;
    private double lastProgress;
    private double doneProgress;
    private double eachProgress;
    private double speed;
    private Downloadable current;
    private boolean launched;
    private final StringBuilder b = new StringBuilder();
    private final Formatter formatter;

    DownloaderThread(Downloader d, int id) {
        super("DT#" + id);
        formatter = new Formatter(b, Locale.US);
        ID = id;
        LOGGER_PREFIX = "[D#" + id + "]";
        downloader = d;
        list = new ArrayList();
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

    public void run() {
        while (true) {
            launched = true;
            eachProgress = 1.0D / (double) list.size();
            currentProgress = doneProgress = 0.0D;
            Iterator var2 = list.iterator();

            label54:
            while (var2.hasNext()) {
                Downloadable d = (Downloadable) var2.next();
                current = d;
                onStart();
                int attempt = 0;
                Object error = null;

                int max = d.isFast()? 2 : 5;
                Iterator var8;
                while (attempt < max) {
                    ++attempt;
                    dlog("Attempting to download (repo: " + d.getRepository() + ") [" + attempt + "/" + max + "]...");
                    int timeout = attempt * U.getConnectionTimeout();

                    try {
                        download(timeout);
                        break;
                    } catch (GaveUpDownloadException var9) {
                        dlog("File is not reachable at all.");
                        error = var9;
                        if (attempt >= max) {
                            FileUtil.deleteFile(d.getDestination());
                            var8 = d.getAdditionalDestinations().iterator();

                            while (var8.hasNext()) {
                                File downloadable = (File) var8.next();
                                FileUtil.deleteFile(downloadable);
                            }

                            dlog("Gave up trying to download this file.");
                            onError(var9);
                        }
                    } catch (AbortedDownloadException var10) {
                        dlog("This download process has been aborted.");
                        error = var10;
                        break;
                    }
                }

                if (error instanceof AbortedDownloadException) {
                    tlog("Thread is aborting...");
                    var8 = list.iterator();

                    while (true) {
                        if (!var8.hasNext()) {
                            break label54;
                        }

                        Downloadable var11 = (Downloadable) var8.next();
                        var11.onAbort((AbortedDownloadException) error);
                    }
                }
            }

            speed = 0.0D;
            list.clear();
            lockThread("iteration");
            launched = false;
        }
    }

    private void download(int timeout) throws GaveUpDownloadException, AbortedDownloadException {
        List<IOException> exL = new ArrayList<IOException>();
        Throwable cause = null;

        if (current.hasRepository()) {
            List<IRepo> list = current.getRepository().getRelevant().getList();
            int attempt = 0, max = list.size();


            while (attempt < max) {
                cause = null;
                for (IRepo repo : list) {
                    URLConnection connection = null;
                    try {
                        connection = repo.get(current.getURL(), attempt * U.getConnectionTimeout(), U.getProxy());
                        dlog("Downloading:", connection);

                        downloadURL(connection, timeout);
                        return;
                    } catch (IOException ioE) {
                        dlog("Failed:", connection.getURL(), current.getURL(), ioE.getMessage());
                        current.getRepository().getList().markInvalid(repo);
                        exL.add(ioE);
                    } catch (AbortedDownloadException var9) {
                        throw var9;
                    } catch (Throwable var10) {
                        dlog("Unknown error occurred:", var10);
                        cause = var10;
                    }
                }
                attempt++;
            }
        } else {
            URLConnection connection = null;
            try {
                connection = new URL(current.getURL()).openConnection();
                dlog("Downloading:", connection);
                downloadURL(connection, timeout);
                return;
            } catch (IOException ioE) {
                dlog("Failed:", connection.getURL(), current.getURL(), ioE.getMessage());
                exL.add(ioE);
            } catch (AbortedDownloadException var9) {
                throw var9;
            } catch (Throwable var10) {
                dlog("Unknown error occurred:", var10);
                cause = var10;
            }
        }

        throw new GaveUpDownloadException(current, cause == null ? new IOExceptionList(exL) : cause);
    }

    private void downloadURL(URLConnection urlConnection, int timeout) throws IOException, AbortedDownloadException {
        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IOException("invalid protocol");
        } else {
            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            Downloadable.setUp(connection, timeout, current.getInsertUA());
            if (!launched) {
                throw new AbortedDownloadException();
            } else {
                long reply_s = System.currentTimeMillis();
                connection.connect();
                long reply = System.currentTimeMillis() - reply_s;
                dlog("Replied in " + reply + " ms.");
                BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                File file = current.getDestination();

                File temp = new File(file.getAbsoluteFile() + ".download");
                if (temp.isFile()) {
                    FileUtil.deleteFile(temp);
                } else {
                    FileUtil.createFile(temp);
                }

                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
                long read = 0L;
                long length = (long) connection.getContentLength();
                long downloaded_s = System.currentTimeMillis();
                long speed_s = downloaded_s;
                long timer = downloaded_s;
                byte[] buffer = new byte[65536];
                int curread = in.read(buffer);

                long downloaded_e;
                double downloadSpeed;
                while (curread > 0) {
                    if (!launched) {
                        out.close();
                        throw new AbortedDownloadException();
                    }

                    read += (long) curread;
                    out.write(buffer, 0, curread);
                    curread = in.read(buffer);
                    if (curread == -1) {
                        break;
                    }

                    long speed_e = System.currentTimeMillis() - speed_s;
                    if (speed_e >= 50L) {
                        speed_s = System.currentTimeMillis();
                        downloaded_e = speed_s - downloaded_s;
                        downloadSpeed = length > 0L ? (double) ((float) read / (float) length) : 0.0D;
                        double copies = downloaded_e > 0L ? (double) read / (double) downloaded_e : 0.0D;

                        if (speed_s - timer > 15000L) {
                            timer = speed_s;
                            b.setLength(0);
                            formatter.format("Still downloading: %.0f%% at speed %.1f kb/s", Double.valueOf(downloadSpeed * 100.0D), Double.valueOf(copies));
                            dlog(b.toString());
                        }

                        onProgress(downloadSpeed, copies);
                    }
                }

                downloaded_e = System.currentTimeMillis() - downloaded_s;
                downloadSpeed = downloaded_e != 0L ? (double) read / (double) downloaded_e : 0.0D;
                in.close();
                out.close();
                connection.disconnect();
                FileUtil.copyFile(temp, file, true);
                FileUtil.deleteFile(temp);
                List copies1 = current.getAdditionalDestinations();
                if (copies1.size() > 0) {
                    dlog("Found additional destinations. Copying...");
                    Iterator var34 = copies1.iterator();

                    while (var34.hasNext()) {
                        File copy = (File) var34.next();
                        dlog("Copying " + copy + "...");
                        FileUtil.copyFile(file, copy, current.isForce());
                        dlog("Success!");
                    }

                    dlog("Copying completed.");
                }

                dlog("Downloaded " + read / 1024L + " kb in " + downloaded_e + " ms. at " + U.setFractional(downloadSpeed, 2) + " kb/s");
                onComplete();
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
        lastProgress = currentProgress;
        downloader.onProgress(this, currentProgress, curdone, speed);
    }

    private void onComplete() throws RetryDownloadException {
        doneProgress += eachProgress;
        current.onComplete();
        downloader.onProgress(this, doneProgress, 1., speed);
        downloader.onFileComplete(this, current);
    }

    private void tlog(Object... o) {
        U.plog(LOGGER_PREFIX, o);
    }

    private void dlog(Object... o) {
        U.plog(LOGGER_PREFIX, "> " + current.getURL() + "\n ", o);
        if (current.hasLogger()) {
            current.getContainer().getLogger().log("> " + current.getURL() + "\n  ", o);
        }

    }
}