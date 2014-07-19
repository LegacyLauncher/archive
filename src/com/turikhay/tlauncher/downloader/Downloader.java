package com.turikhay.tlauncher.downloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration.ConnectionQuality;
import com.turikhay.util.U;
import com.turikhay.util.async.ExtendedThread;

public class Downloader extends ExtendedThread {
	public final static int MAX_THREADS = 8;
	final static String ITERATION_BLOCK = "iteration";
	private final static String DOWNLOAD_BLOCK = "download";

	private final DownloaderThread[] threads;
	private final List<Downloadable> list;
	private final List<DownloaderListener> listeners;
	private ConnectionQuality configuration;

	private final AtomicInteger remainingObjects;
	private int runningThreads, workingThreads;
	private final double[] speedContainer, progressContainer;
	private double lastAverageProgress, averageProgress, averageSpeed;

	private final Object workLock;

	private Downloader(ConnectionQuality configuration) {
		super("MD");

		this.setConfiguration(configuration);

		this.remainingObjects = new AtomicInteger();
		this.threads = new DownloaderThread[MAX_THREADS];
		this.list = Collections.synchronizedList(new ArrayList<Downloadable>());
		this.listeners = Collections
				.synchronizedList(new ArrayList<DownloaderListener>());

		this.speedContainer = new double[MAX_THREADS];
		this.progressContainer = new double[MAX_THREADS];

		this.workLock = new Object();

		this.startAndWait();
	}

	public Downloader(TLauncher tlauncher) {
		this(tlauncher.getSettings().getConnectionQuality());
	}

	public ConnectionQuality getConfiguration() {
		return configuration;
	}

	public int getRemaining() {
		return remainingObjects.get();
	}

	public double getProgress() {
		return averageProgress;
	}

	public double getSpeed() {
		return averageSpeed;
	}

	public void add(Downloadable d) {
		if (d == null)
			throw new NullPointerException();

		list.add(d);
	}

	public void add(DownloadableContainer c) {
		if (c == null)
			throw new NullPointerException();

		list.addAll(c.list);
	}

	public void addAll(Downloadable... ds) {
		if (ds == null)
			throw new NullPointerException();

		for (int i = 0; i < ds.length; i++) {
			if (ds[i] == null)
				throw new NullPointerException("Downloadable at " + i
						+ " is NULL!");

			list.add(ds[i]);
		}
	}

	public void addAll(Collection<Downloadable> coll) {
		if (coll == null)
			throw new NullPointerException();

		int i = -1;

		for (Downloadable d : coll) {
			++i;

			if (d == null)
				throw new NullPointerException("Downloadable at" + i
						+ " is NULL!");

			list.add(d);
		}
	}

	public void addListener(DownloaderListener listener) {
		if (listener == null)
			throw new NullPointerException();

		listeners.add(listener);
	}

	public boolean startDownload() {
		boolean haveWork = !list.isEmpty();

		if (haveWork)
			unblockThread(ITERATION_BLOCK);

		return haveWork;
	}

	public void startDownloadAndWait() {
		if (startDownload())
			waitWork();
	}

	private void waitWork() {
		synchronized (workLock) {
			try {
				workLock.wait();
			} catch (InterruptedException e) {
			}
		}
	}

	private void notifyWork() {
		synchronized (workLock) {
			workLock.notifyAll();
		}
	}

	public void stopDownload() {
		if (!isThreadBlocked())
			throw new IllegalStateException();

		for (DownloaderThread thread : threads)
			thread.stopDownload();
	}

	public void stopDownloadAndWait() {
		if (!isThreadBlocked())
			throw new IllegalStateException();

		for (DownloaderThread thread : threads)
			thread.stopDownload();

		waitForThreads();
	}

	public void setConfiguration(ConnectionQuality configuration) {
		if (configuration == null)
			throw new NullPointerException();

		log("Loaded configuration:", configuration);
		this.configuration = configuration;
	}

	@Override
	public void run() {
		checkCurrent(); // Checks if this method is called from this Downloader
						// thread.

		while (true) {
			blockThread(ITERATION_BLOCK);

			log("Files in queue", list.size());

			synchronized (list) {
				sortOut();
			}

			for (int i = 0; i < runningThreads; i++)
				threads[i].startDownload();

			blockThread(DOWNLOAD_BLOCK);
			notifyWork();

			Arrays.fill(speedContainer, 0.0);
			Arrays.fill(progressContainer, 0.0);

			averageProgress = 0;
			lastAverageProgress = 0;
			workingThreads = 0;
		}
	}

	private void sortOut() {
		int size = list.size();

		if (size == 0)
			return;

		int downloadablesAtThread = U.getMaxMultiply(size, MAX_THREADS), x = 0, y = -1;

		log("Starting download " + size + " files...");
		this.onStart(size);

		while (size > 0) {
			for (int i = 0; i < configuration.getMaxThreads(); i++) {
				size -= downloadablesAtThread;
				++workingThreads;

				if (threads[i] == null)
					threads[i] = new DownloaderThread(this, ++runningThreads);

				for (y = x; y < x + downloadablesAtThread; y++)
					threads[i].add(list.get(y));

				x = y;

				if (size == 0)
					break;
			}
			downloadablesAtThread = U.getMaxMultiply(size, MAX_THREADS);
		}

		list.clear();
	}

	private void onStart(int size) {
		for (DownloaderListener listener : listeners)
			listener.onDownloaderStart(this, size);

		remainingObjects.addAndGet(size);
	}

	void onAbort() {
		for (DownloaderListener listener : listeners)
			listener.onDownloaderAbort(this);
	}

	void onProgress(DownloaderThread thread, double curprogress, double curspeed) {
		int id = thread.getID() - 1;

		this.progressContainer[id] = curprogress;
		this.speedContainer[id] = curspeed;

		averageProgress = U.getAverage(progressContainer, workingThreads);

		if (averageProgress - lastAverageProgress < 0.01)
			return; // Reduce update rate

		lastAverageProgress = averageProgress;
		averageSpeed = U.getSum(speedContainer);

		for (DownloaderListener listener : listeners)
			listener.onDownloaderProgress(this, averageProgress, averageSpeed);
	}

	void onFileComplete(DownloaderThread thread, Downloadable file) {
		int remaining = remainingObjects.decrementAndGet();

		for (DownloaderListener listener : listeners)
			listener.onDownloaderFileComplete(this, file);

		if (remaining == 0)
			onComplete();
	}

	private void onComplete() {
		for (DownloaderListener listener : listeners)
			listener.onDownloaderComplete(this);

		unblockThread(DOWNLOAD_BLOCK);
	}

	private void waitForThreads() {
		log("Waiting for threads...");
		for (int i = 0; i < runningThreads; i++)
			if (!threads[i].isThreadBlocked())
				i = -1;
		log("All threads are blocked by now");
	}

	private void log(Object... o) {
		U.log("[Downloader2]", o);
	}
}
