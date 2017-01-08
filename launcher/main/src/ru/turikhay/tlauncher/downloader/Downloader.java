package ru.turikhay.tlauncher.downloader;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Downloader extends ExtendedThread {
    public static final int MAX_THREADS = 6;
    public static final String DOWNLOAD_BLOCK = "download";
    static final double SMOOTHING_FACTOR = 0.005D;
    static final String ITERATION_BLOCK = "iteration";
    private final DownloaderThread[] threads;
    private final List<Downloadable> list;
    private final List<DownloaderListener> listeners;
    private final AtomicInteger remainingObjects;
    private int runningThreads;
    private int workingThreads;
    private int busyThreads;
    private final double[] progressContainer;
    private double lastAverageProgress;
    private double averageProgress;
    private double lastProgress;
    private double speed;
    private boolean aborted;
    private final Object workLock;
    private boolean haveWork;

    public Downloader() {
        super("MD");
        remainingObjects = new AtomicInteger();
        threads = new DownloaderThread[6];
        list = Collections.synchronizedList(new ArrayList());
        listeners = Collections.synchronizedList(new ArrayList());
        progressContainer = new double[6];
        workLock = new Object();
        startAndWait();
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
            Iterator var4 = coll.iterator();

            while (var4.hasNext()) {
                Downloadable d = (Downloadable) var4.next();
                ++i;
                if (d == null) {
                    throw new NullPointerException("Downloadable at" + i + " is NULL!");
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
            unlockThread("iteration");
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
            Object var1 = workLock;
            synchronized (workLock) {
                try {
                    workLock.wait();
                } catch (InterruptedException var3) {
                    var3.printStackTrace();
                }
            }
        }

    }

    private void notifyWork() {
        haveWork = false;
        Object var1 = workLock;
        synchronized (workLock) {
            workLock.notifyAll();
        }
    }

    public void stopDownload() {
        if (!isThreadLocked()) {
            throw new IllegalArgumentException();
        } else {
            for (int i = 0; i < runningThreads; ++i) {
                threads[i].stopDownload();
            }

            aborted = true;
            if (isThreadLocked()) {
                tryUnlock("download");
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
            lockThread("iteration");
            log("Files in queue", list.size());
            List i = list;
            synchronized (list) {
                sortOut();
            }

            for (int var3 = 0; var3 < runningThreads; ++var3) {
                threads[var3].startDownload();
            }

            lockThread("download");
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
        int size = list.size();
        if (size != 0) {
            int downloadablesAtThread = U.getMaxMultiply(size, 6);
            int x = 0;
            boolean y = true;
            log("Starting download " + size + " files...");
            onStart(size);
            int max = 6;

            boolean[] workers;
            for (workers = new boolean[max]; size > 0; downloadablesAtThread = U.getMaxMultiply(size, 6)) {
                for (int worker = 0; worker < max; ++worker) {
                    workers[worker] = true;
                    size -= downloadablesAtThread;
                    if (threads[worker] == null) {
                        threads[worker] = new DownloaderThread(this, ++runningThreads);
                    }

                    int var11;
                    for (var11 = x; var11 < x + downloadablesAtThread; ++var11) {
                        threads[worker].add(list.get(var11));
                    }

                    x = var11;
                    if (size == 0) {
                        break;
                    }
                }
            }

            boolean[] var10 = workers;
            int var9 = workers.length;

            for (int var8 = 0; var8 < var9; ++var8) {
                boolean var12 = var10[var8];
                if (var12) {
                    ++workingThreads;
                }
            }

            list.clear();
        }
    }

    private void onStart(int size) {
        Iterator var3 = listeners.iterator();

        while (var3.hasNext()) {
            DownloaderListener listener = (DownloaderListener) var3.next();
            listener.onDownloaderStart(this, size);
        }

        remainingObjects.addAndGet(size);
    }

    void onAbort() {
        Iterator var2 = listeners.iterator();

        while (var2.hasNext()) {
            DownloaderListener listener = (DownloaderListener) var2.next();
            listener.onDownloaderAbort(this);
        }

    }

    void onProgress(DownloaderThread thread, double curprogress, double curdone, double curspeed) {
        int id = thread.getID() - 1;
        lastProgress = curdone;
        progressContainer[id] = curprogress;
        averageProgress = U.getAverage(progressContainer, workingThreads);
        if (remainingObjects.get() == 1 || averageProgress - lastAverageProgress >= 0.01D) {
            speed = 0.005D * speed + 0.995D * curspeed;
            lastAverageProgress = averageProgress;
            Iterator var8 = listeners.iterator();

            while (var8.hasNext()) {
                DownloaderListener listener = (DownloaderListener) var8.next();
                listener.onDownloaderProgress(this, averageProgress, speed);
            }
        }
    }

    void onFileComplete(DownloaderThread thread, Downloadable file) {
        int remaining = remainingObjects.decrementAndGet();
        log("Objects remaining:", Integer.valueOf(remaining));
        Iterator var5 = listeners.iterator();

        while (var5.hasNext()) {
            DownloaderListener listener = (DownloaderListener) var5.next();
            listener.onDownloaderFileComplete(this, file);
        }

        if (remaining < 1) {
            onComplete();
        }
    }

    private void onComplete() {
        Iterator var2 = listeners.iterator();

        while (var2.hasNext()) {
            DownloaderListener listener = (DownloaderListener) var2.next();
            listener.onDownloaderComplete(this);
        }

        unlockThread("download");
    }

    private void waitForThreads() {
        log("Waiting for", Integer.valueOf(workingThreads), "threads...");

        boolean blocked;
        do {
            blocked = true;

            for (int i = 0; i < workingThreads; ++i) {
                if (!threads[i].isThreadLocked()) {
                    blocked = false;
                }
            }
        } while (!blocked);

        log("All threads are blocked by now");
    }

    private static void log(Object... o) {
        U.log("[Downloader2]", o);
    }
}
