package net.legacylauncher.downloader;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

@Slf4j
public class Downloader {
    public static final int MAX_THREADS = 6;
    static final double SMOOTHING_FACTOR = 0.005D;
    private final List<DownloaderListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger remainingObjects = new AtomicInteger(0);
    private final List<Downloadable> queue = new CopyOnWriteArrayList<>();
    private final int parallelism;
    private ExecutorService executor;
    private double[] progressContainer;
    private double lastAverageProgress;
    @Getter
    private double progress;
    @Getter
    private double lastProgress;
    @Getter
    private double speed;
    @Getter
    private boolean aborted;

    public Downloader(int parallelism) {
        this.parallelism = parallelism;
    }

    public Downloader() {
        this(MAX_THREADS);
    }

    public int getRemaining() {
        return remainingObjects.get();
    }

    public void add(Downloadable d) {
        if (d == null) {
            throw new NullPointerException();
        }
        queue.add(d);
    }

    public void add(DownloadableContainer c) {
        if (c == null) {
            throw new NullPointerException();
        }
        queue.addAll(c.list);
    }

    public void addAll(Downloadable... ds) {
        if (ds == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < ds.length; ++i) {
            if (ds[i] == null) {
                throw new NullPointerException("Downloadable at " + i + " is NULL!");
            }

            queue.add(ds[i]);
        }
    }

    public void addAll(Collection<Downloadable> coll) {
        if (coll == null) {
            throw new NullPointerException();
        }
        int i = -1;

        for (Downloadable d : coll) {
            ++i;
            if (d == null) {
                throw new NullPointerException("Downloadable at " + i + " is NULL!");
            }

            queue.add(d);
        }
    }

    public void addListener(DownloaderListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        listeners.add(listener);
    }

    public synchronized void download() {
        while (!queue.isEmpty()) {
            List<Downloadable> tasks = new ArrayList<>(queue);
            queue.clear();
            log.debug("Files in the queue: {}", tasks.size());
            onStart(tasks.size());

            executor = Executors.newFixedThreadPool(parallelism);

            IntStream.range(0, tasks.size()).mapToObj(i ->
                    new DownloaderTask(i, tasks.get(i), this)
            ).forEach(executor::submit);

            executor.shutdown();
            waitForThreads();

            if (aborted) {
                onAbort();
                aborted = false;
            }

            speed = 0.0D;
            progress = 0.0D;
            lastAverageProgress = 0.0D;
            remainingObjects.set(0);
        }
    }

    public void stopDownload() {
        aborted = true;

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    public void stopDownloadAndWait() {
        stopDownload();
        waitForThreads();
    }

    private void onStart(int size) {
        for (DownloaderListener listener : listeners) {
            listener.onDownloaderStart(this, size);
        }

        remainingObjects.addAndGet(size);
        progressContainer = new double[size];
    }

    void onAbort() {
        remainingObjects.set(0);
        for (DownloaderListener listener : listeners) {
            listener.onDownloaderAbort(this);
        }
    }

    void onProgress(DownloaderTask task, double curprogress, double curspeed) {
        lastProgress = curprogress;
        progressContainer[task.getId()] = curprogress;
        progress = DoubleStream.of(progressContainer).average().orElse(0);
        if (remainingObjects.get() == 1 || progress - lastAverageProgress >= 0.01D) {
            speed = SMOOTHING_FACTOR * speed + (1 - SMOOTHING_FACTOR) * curspeed;
            lastAverageProgress = progress;

            for (DownloaderListener listener : listeners) {
                listener.onDownloaderProgress(this, progress, speed);
            }
        }
    }

    void onFileComplete(DownloaderTask task, Downloadable file) {
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
        remainingObjects.set(0);
        for (DownloaderListener listener : listeners) {
            listener.onDownloaderComplete(this);
        }
    }

    private void waitForThreads() {
        log.debug("Waiting for download tasks...");

        try {
            //noinspection ResultOfMethodCallIgnored
            if (executor != null && !executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                log.warn("Cannot wait for download threads to finish");
            }
        } catch (InterruptedException ignored) {
        }

        log.debug("Download tasks stopped");
    }
}
