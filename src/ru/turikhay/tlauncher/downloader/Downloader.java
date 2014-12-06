package ru.turikhay.tlauncher.downloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration.ConnectionQuality;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

public class Downloader extends ExtendedThread {
	public final static int MAX_THREADS = 6;
	public final static String DOWNLOAD_BLOCK = "download";

	final static String ITERATION_BLOCK = "iteration";

	private final DownloaderThread[] threads;
	private final List<Downloadable> list;
	private final List<DownloaderListener> listeners;
	private ConnectionQuality configuration;

	private final AtomicInteger remainingObjects = new AtomicInteger();
	private int runningThreads, workingThreads;
	private final double[] speedContainer, progressContainer;
	private double lastAverageProgress, averageProgress, averageSpeed;

	private boolean aborted;

	private final Object workLock;
	private boolean haveWork;

	private Downloader(ConnectionQuality configuration) {
		super("MD");

		this.setConfiguration(configuration);

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
			unlockThread(ITERATION_BLOCK);

		return haveWork;
	}

	public void startDownloadAndWait() {
		if (startDownload())
			waitWork();
	}

	private void waitWork() {
		this.haveWork = true;

		while(haveWork)
			synchronized (workLock) {
				try {
					workLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	}

	private void notifyWork() {
		this.haveWork = false;

		synchronized (workLock) {
			workLock.notifyAll();
		}
	}

	public void stopDownload() {
		if(!isThreadLocked())
			throw new IllegalArgumentException();

		for (int i=0;i<runningThreads;i++)
			threads[i].stopDownload();

		aborted = true;

		if(isThreadLocked())
			tryUnlock(DOWNLOAD_BLOCK);
	}

	public void stopDownloadAndWait() {
		stopDownload();
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
		checkCurrent(); // Checks if this method is called from this Downloader thread.

		while (true) {
			lockThread(ITERATION_BLOCK);

			log("Files in queue", list.size());

			synchronized (list) {
				sortOut();
			}

			for (int i = 0; i < runningThreads; i++)
				threads[i].startDownload();

			lockThread(DOWNLOAD_BLOCK);

			if(aborted) {
				waitForThreads();
				onAbort();

				aborted = false;
			}

			notifyWork();

			Arrays.fill(speedContainer, 0.0);
			Arrays.fill(progressContainer, 0.0);

			averageProgress = 0;
			lastAverageProgress = 0;
			workingThreads = 0;
			remainingObjects.set(0);
		}
	}

	private void sortOut() {
		int size = list.size();

		if (size == 0)
			return;

		int downloadablesAtThread = U.getMaxMultiply(size, MAX_THREADS), x = 0, y = -1;

		log("Starting download " + size + " files...");
		this.onStart(size);

		int max = configuration.getMaxThreads();
		boolean[] workers = new boolean[max];

		while (size > 0) {
			for (int i = 0; i < max; i++) {
				workers[i] = true;
				size -= downloadablesAtThread;

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

		for(boolean worker : workers)
			if(worker) ++workingThreads;

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

		log("Objects remaining:", remaining);

		for (DownloaderListener listener : listeners)
			listener.onDownloaderFileComplete(this, file);

		if (remaining < 1)
			onComplete();
	}

	private void onComplete() {
		for (DownloaderListener listener : listeners)
			listener.onDownloaderComplete(this);

		unlockThread(DOWNLOAD_BLOCK);
	}

	private void waitForThreads() {
		log("Waiting for", workingThreads,"threads...");

		boolean blocked;

		while(true) {
			blocked = true;

			for (int i = 0; i < workingThreads; i++)
				if (!threads[i].isThreadLocked())
					blocked = false;

			if(blocked)
				break;
		}

		log("All threads are blocked by now");
	}

	private static void log(Object... o) {
		U.log("[Downloader2]", o);
	}
}
