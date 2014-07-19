package com.turikhay.tlauncher.downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.turikhay.util.FileUtil;
import com.turikhay.util.U;
import com.turikhay.util.async.ExtendedThread;

public class DownloaderThread extends ExtendedThread {
	private final static String ITERATION_BLOCK = Downloader.ITERATION_BLOCK;
	private final static int CONTAINER_SIZE = 100;

	private final int ID;
	private final String LOGGER_PREFIX;

	private final Downloader downloader;
	private final List<Downloadable> list;

	private final double[] averageSpeedContainer;
	private int speedCaret;

	private double currentProgress, lastProgress, doneProgress, eachProgress,
			speed;

	private Downloadable current;
	private boolean launched;

	DownloaderThread(Downloader d, int id) {
		super("DT#" + id);

		ID = id;
		LOGGER_PREFIX = "[D#" + id + "]";

		this.downloader = d;
		this.list = new ArrayList<Downloadable>();
		this.averageSpeedContainer = new double[CONTAINER_SIZE];

		this.startAndWait();
	}

	int getID() {
		return ID;
	}

	void add(Downloadable d) {
		list.add(d);
	}

	void startDownload() {
		this.launched = true;
		unblockThread(ITERATION_BLOCK);
	}

	void stopDownload() {
		this.launched = false;
	}

	@Override
	public void run() {
		while (true) {
			launched = true;

			eachProgress = 1.0 / list.size();
			currentProgress = doneProgress = 0.0;

			for (Downloadable d : list) {
				this.current = d;
				onStart();

				int attempt = 0, max, timeout;
				Throwable error = null;

				while (attempt < (max = downloader.getConfiguration().getTries(
						d.isFast()))) {
					++attempt;

					dlog("Attempting to download (repo: " + d.getRepository()
							+ ") [" + attempt + "/" + max + "]...");
					timeout = attempt
							* downloader.getConfiguration().getTimeout();

					try {
						download(timeout);
						break;
					} catch (GaveUpDownloadException e) {
						dlog("File is not reachable at all.");
						error = e;
					} catch (RetryDownloadException e) {
						dlog("Will attempt to re-download this file:",
								e.getMessage());
						error = e;
					} catch (AbortedDownloadException e) {
						dlog("This download process has been aborted.");
						error = e;
						break;
					}

					if (attempt < max)
						continue;

					FileUtil.deleteFile(d.getDestination());

					for (File file : d.getAdditionalDestinations())
						FileUtil.deleteFile(file);

					dlog("Gave up trying to download this file.", error);
					
					onError(error);
				}

				if (error instanceof AbortedDownloadException) {
					tlog("Thread is aborting...");

					for (Downloadable downloadable : this.list)
						downloadable.onAbort((AbortedDownloadException) error);

					break;
				}
			}

			Arrays.fill(averageSpeedContainer, 0.0);
			list.clear();

			blockThread(ITERATION_BLOCK);

			launched = false;
		}
	}

	private void download(int timeout) throws GaveUpDownloadException,
			RetryDownloadException, AbortedDownloadException {
		boolean hasRepo = current.hasRepository();
		int attempt = 0, max = hasRepo ? current.getRepository().getCount() : 1;
		
		IOException cause = null;

		while (attempt < max) {
			++attempt;

			String url = hasRepo ? current.getRepository().getSelectedRepo()
					+ current.getURL() : current.getURL();
			dlog("Trying to download from: " + url);

			try {
				downloadURL(url, timeout);
				return;
			} catch (IOException e) {
				dlog("Failed to download from: " + url, e);
				
				cause = e;
				
				if (hasRepo)
					current.getRepository().selectNext();
			}
		}

		throw new GaveUpDownloadException(current, cause);
	}

	private void downloadURL(String path, int timeout) throws IOException,
			AbortedDownloadException, RetryDownloadException {
		URL url = new URL(path);
		URLConnection urlConnection = url.openConnection();

		if (!(urlConnection instanceof HttpURLConnection))
			throw new IOException("Invalid protocol: " + url.getProtocol());

		HttpURLConnection connection = (HttpURLConnection) urlConnection;
		Downloadable.setUp(connection, timeout);

		if (!launched)
			throw new AbortedDownloadException();

		long reply_s = System.currentTimeMillis();
		connection.connect();
		long reply = System.currentTimeMillis() - reply_s;

		dlog("Got reply in " + reply + " ms.");

		InputStream in = new BufferedInputStream(connection.getInputStream());

		File file = current.getDestination();
		File temp = FileUtil.makeTemp(new File(file.getAbsolutePath()
				+ ".tlauncherdownload"));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));

		long read = 0L, length = connection.getContentLength(), downloaded_s = System
				.currentTimeMillis(), speed_s = downloaded_s, downloaded_e, speed_e;

		// Speed in kb/s:
		// read (bytes) / time (ms)

		// How does it works:
		// Every second calculates speed using general variables: (read at all /
		// download time)

		byte[] buffer = new byte[65536];

		int curread = in.read(buffer);
		while (curread > 0) {
			if (!launched) {
				out.close();
				throw new AbortedDownloadException();
			}

			read += curread;

			out.write(buffer, 0, curread);

			curread = in.read(buffer);

			if (curread == -1)
				break;

			speed_e = System.currentTimeMillis() - speed_s;
			if (speed_e < 50)
				continue;

			speed_s = System.currentTimeMillis(); // "clearing" variable for the
													// next calculating
			downloaded_e = speed_s - downloaded_s; // System.currentTimeMillis()
													// - downloaded_s:
													// calculating general
													// download time.

			double curdone = read / (float) length, curspeed = (read / (double) downloaded_e);

			onProgress(curread, curdone, curspeed);
		}
		downloaded_e = System.currentTimeMillis() - downloaded_s;

		double downloadSpeed = (downloaded_e != 0) ? (read / (double) downloaded_e)
				: 0.0;

		in.close();
		out.close();
		connection.disconnect();

		if (!temp.renameTo(file)) {
			FileUtil.copyFile(temp, file, true);
			FileUtil.deleteFile(temp);
		}

		List<File> copies = current.getAdditionalDestinations();
		if (copies.size() > 0) {
			dlog("Found additional destinations. Copying...");
			for (File copy : copies) {
				dlog("Copying " + copy + "...");

				FileUtil.copyFile(file, copy, current.isForce());

				dlog("Success!");
			}
			dlog("Copying completed.");
		}

		// current.setTime(downloaded_e);
		// current.setSize(read);

		dlog("Downloaded in " + downloaded_e + " ms. at " + downloadSpeed
				+ " kb/s");

		onComplete();
	}

	private void onStart() {
		current.onStart();
	}

	private void onError(Throwable e) {
		current.onError(e);
		downloader.onFileComplete(this, current);
	}

	private void onProgress(double curread, double curdone, double curspeed) {
		if (++speedCaret == CONTAINER_SIZE)
			speedCaret = 0;
		this.averageSpeedContainer[speedCaret] = curspeed;

		currentProgress = doneProgress + (eachProgress * curdone);

		if (currentProgress - lastProgress < 0.01)
			return; // Reduce update rate

		lastProgress = currentProgress;
		speed = U.getAverage(averageSpeedContainer);

		downloader.onProgress(this, currentProgress, speed);
	}

	private void onComplete() throws RetryDownloadException {
		this.doneProgress += eachProgress;

		downloader.onProgress(this, doneProgress, speed);
		downloader.onFileComplete(this, current);
		current.onComplete();
	}

	private void tlog(Object... o) {
		U.plog(LOGGER_PREFIX, o);
	}

	private void dlog(Object... o) {
		U.plog(LOGGER_PREFIX, "> " + current.getURL() + "\n ", o);

		if (current.hasConsole())
			current.getContainer().getConsole()
					.log("> " + current.getURL() + "\n  ", o);
	}
}
