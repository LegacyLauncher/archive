package net.legacylauncher.downloader;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.ExtendedThread;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Downloader extends ExtendedThread {
    public static final int MAX_THREADS = 6;
    public static final String DOWNLOAD_BLOCK = "download";
    static final double SMOOTHING_FACTOR = 0.005D;
    static final String ITERATION_BLOCK = "iteration";
    private final DownloaderThread[] threads;
    private final List<Downloadable> list;
    private final List<DownloaderListener> listeners;
    private final AtomicInteger remainingObjects;
    private int workingThreads;
    private final double[] progressContainer;
    private double lastAverageProgress;
    private double averageProgress;
    private double lastProgress;
    private double speed;
    private boolean aborted;
    private final Object workLock;
    private boolean haveWork;

    public Downloader(int numOfThreads) {
        super("MD");
        remainingObjects = new AtomicInteger();
        threads = new DownloaderThread[numOfThreads];
        list = Collections.synchronizedList(new ArrayList<>());
        listeners = Collections.synchronizedList(new ArrayList<>());
        progressContainer = new double[numOfThreads];
        workLock = new Object();
        startAndWait();
    }

    public Downloader() {
        this(MAX_THREADS);
    }

    public int getRemaining() {
        return remainingObjects.get();
    }

    public double getLastProgress() {
        return lastProgress;
    }

    public double getProgress() {
        return averageProgress;
    }

    public double getSpeed() {
        return speed;
    }

    public void add(Downloadable d) {
        if (d == null) {
            throw new NullPointerException();
        } else {
            list.add(d);
        }
    }

    public void add(DownloadableContainer c) {
        if (c == null) {
            throw new NullPointerException();
        } else {
            list.addAll(c.list);
        }
    }

    public void addAll(Downloadable... ds) {
        if (ds == null) {
            throw new NullPointerException();
        } else {
            for (int i = 0; i < ds.length; ++i) {
                if (ds[i] == null) {
                    throw new NullPointerException("Downloadable at " + i + " is NULL!");
                }

                list.add(ds[i]);
            }

        }
    }

    public void addAll(Collection<Downloadable> coll) {
        if (coll == null) {
            throw new NullPointerException();
        } else {
            int i = -1;

            for (Downloadable d : coll) {
                ++i;
                if (d == null) {
                    throw new NullPointerException("Downloadable at " + i + " is NULL!");
                }

                list.add(d);
            }

        }
    }

    public void addListener(DownloaderListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        } else {
            listeners.add(listener);
        }
    }

    public boolean startDownload() {
        boolean haveWork = !list.isEmpty();
        if (haveWork) {
            unlockThread(ITERATION_BLOCK);
        }

        return haveWork;
    }

    public void startDownloadAndWait() {
        if (startDownload()) {
            waitWork();
        }
    }

    private void waitWork() {
        haveWork = true;

        while (haveWork) {
            synchronized (workLock) {
                try {
                    workLock.wait();
                } catch (InterruptedException e) {
                    log.warn("Interrupted during waiting", e);
                }
            }
        }

    }

    private void notifyWork() {
        haveWork = false;
        synchronized (workLock) {
            workLock.notifyAll();
        }
    }

    public void stopDownload() {
        if (!isThreadLocked()) {
            throw new IllegalArgumentException();
        } else {
            for (int i = 0; i < threads.length; i++) {
                DownloaderThread thread = threads[i];
                if (thread != null) {
                    thread.stopDownload();
                    threads[i] = null;
                }
            }

            aborted = true;
            if (isThreadLocked()) {
                tryUnlock(DOWNLOAD_BLOCK);
            }
        }
    }

    public void stopDownloadAndWait() {
        stopDownload();
        waitForThreads();
    }

    public void run() {
        checkCurrent();

        while (true) {
            lockThread(ITERATION_BLOCK);
            log.debug("Files in the queue: {}", list.size());
            synchronized (list) {
                sortOut();
            }

            for (DownloaderThread thread : threads) {
                if (thread != null) {
                    thread.startDownload();
                }
            }

            lockThread(DOWNLOAD_BLOCK);
            if (aborted) {
                waitForThreads();
                onAbort();
                aborted = false;
            }

            notifyWork();
            Arrays.fill(progressContainer, 0.0D);
            speed = 0.0D;
            averageProgress = 0.0D;
            lastAverageProgress = 0.0D;
            workingThreads = 0;
            remainingObjects.set(0);
        }
    }

    private void sortOut() {
        if (list.isEmpty()) return;

        log.info("Starting to download {} files...", list.size());
        onStart(list.size());

        boolean[] workers = new boolean[threads.length];
        Lists.partition(list, threads.length).forEach(chunk -> {
            for (int i = 0; i < chunk.size(); i++) {
                if (!workers[i]) {
                    workers[i] = true;
                    threads[i] = new DownloaderThread(this, ++workingThreads);
                }
                threads[i].add(chunk.get(i));
            }
        });

        list.clear();
    }

    private void onStart(int size) {
        for (DownloaderListener listener : listeners) {
            listener.onDownloaderStart(this, size);
        }

        remainingObjects.addAndGet(size);
    }

    void onAbort() {
        for (DownloaderListener listener : listeners) {
            listener.onDownloaderAbort(this);
        }
    }

    void onProgress(DownloaderThread thread, double curprogress, double curdone, double curspeed) {
        int id = thread.getID() - 1;
        lastProgress = curdone;
        progressContainer[id] = curprogress;
        averageProgress = U.getAverage(progressContainer, workingThreads);
        if (remainingObjects.get() == 1 || averageProgress - lastAverageProgress >= 0.01D) {
            speed = SMOOTHING_FACTOR * speed + (1 - SMOOTHING_FACTOR) * curspeed;
            lastAverageProgress = averageProgress;

            for (DownloaderListener listener : listeners) {
                listener.onDownloaderProgress(this, averageProgress, speed);
            }
        }
    }

    void onFileComplete(DownloaderThread thread, Downloadable file) {
        int remaining = remainingObjects.decrementAndGet();
        log.debug("Remaining: {}", remaining);

        for (DownloaderListener listener : listeners) {
            listener.onDownloaderFileComplete(this, file);
        }

        if (remaining < 1) {
            onComplete();
        }
    }

    private void onComplete() {

        for (DownloaderListener listener : listeners) {
            listener.onDownloaderComplete(this);
        }

        unlockThread(DOWNLOAD_BLOCK);
    }

    private void waitForThreads() {
        log.debug("Waiting for download threads...");

        outer:
        do {
            for (DownloaderThread thread : threads) {
                if (thread != null && !thread.isThreadLocked()) {
                    continue outer;
                }
            }

            break;
        } while (true);

        log.debug("All threads are blocked by now");
    }
}
