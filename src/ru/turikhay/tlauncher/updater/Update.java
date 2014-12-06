package ru.turikhay.tlauncher.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.turikhay.tlauncher.Bootstrapper;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.SimpleConfiguration;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.DownloadableHandler;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.downloader.RetryDownloadException;
import ru.turikhay.util.U;

public class Update {
	public static final int NONE = 0, DOWNLOADING = 1, DOWNLOADED = 2,
			UPDATING = 3;

	private int step;

	private double version;
	private String code, description;
	private boolean required;

	private Map<PackageType, URI> links = new HashMap<PackageType, URI>();

	private final Updater upd;
	private final Downloader d;

	private final List<UpdateListener> listeners = Collections
			.synchronizedList(new ArrayList<UpdateListener>());

	public void addListener(UpdateListener l) {
		listeners.add(l);
	}

	public void removeListener(UpdateListener l) {
		listeners.remove(l);
	}

	Update(Updater upd, Downloader d, SimpleConfiguration settings) {
		if (upd == null)
			throw new NullPointerException("Updater is NULL!");
		if (d == null)
			throw new NullPointerException("Downloader is NULL!");
		if (settings == null)
			throw new NullPointerException("Settings is NULL!");

		this.upd = upd;
		this.d = d;

		setVersion(settings.getDouble("latest"));
		setCode(settings.get("code"));
		setDescription(settings.get("description"));
		setRequired(settings.getBoolean("required"));

		for (String key : settings.getKeys())
			try {
				links.put(PackageType.valueOf(key.toUpperCase()),
						U.makeURI(settings.get(key)));
			} catch (Exception e) {
			}
	}

	public Updater getUpdater() {
		return upd;
	}

	public URI getDownloadLinkFor(PackageType pt) {
		return links.get(pt);
	}

	public URI getDownloadLink() {
		return getDownloadLinkFor(PackageType.getCurrent());
	}

	public double getVersion() {
		return version;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRequired() {
		return required;
	}

	public int getStep() {
		return step;
	}

	public void download(boolean async) {
		downloadFor(PackageType.getCurrent(), async);
	}

	public void download() {
		download(false);
	}

	public void asyncDownload() {
		download(true);
	}

	void downloadFor(PackageType pt, boolean async) {
		try {
			downloadFor_(pt, async);
		} catch (Exception e) {
			onUpdateError(e);
		}
	}

	private void downloadFor_(PackageType pt, boolean async) throws Exception {
		if (step > NONE)
			throw new IllegalStepException(step);

		URI download_link = getDownloadLinkFor(pt);
		if (download_link == null)
			throw new NullPointerException("Update for package \"" + pt
					+ "\" is not found");

		File destination = Updater.getUpdateFileFor(pt);
		destination.deleteOnExit();

		Downloadable downloadable = new Downloadable(download_link.toString(), destination);
		downloadable.setInsertUA(true);
		downloadable.addHandler(new DownloadableHandler() {
			@Override
			public void onStart(Downloadable d) {
				onUpdateDownloading();
			}

			@Override
			public void onComplete(Downloadable d)
					throws RetryDownloadException {
				step = DOWNLOADED;
				onUpdateReady();
			}

			@Override
			public void onError(Downloadable d, Throwable e) {
				step = NONE;
				onUpdateDownloadError(e);
			}

			@Override
			public void onAbort(Downloadable d) {
				step = NONE;
				onUpdateDownloadError(d.getError());
			}
		});

		d.add(downloadable);

		if (async)
			d.startDownload();
		else
			d.startDownloadAndWait();
	}

	public void apply() {
		applyFor(PackageType.getCurrent());
	}

	void applyFor(PackageType pt) {
		try {
			applyFor_(pt);
		} catch (Exception e) {
			onUpdateApplyError(e);
		}
	}

	private void applyFor_(PackageType pt) throws Exception {
		if (step < DOWNLOADED)
			throw new IllegalStepException(step);

		log("Saving update... Launcher will be reopened.");

		File replace = Updater.getFileFor(pt), replacer = Updater.getUpdateFileFor(pt);
		replacer.deleteOnExit();

		String[] args = (TLauncher.getInstance() != null) ? TLauncher.getArgs()
				: new String[0];
		ProcessBuilder builder = Bootstrapper.createLauncher(args).createProcess();

		FileInputStream in = new FileInputStream(replacer);
		FileOutputStream out = new FileOutputStream(replace);

		onUpdateApplying();

		byte[] buffer = new byte[65536];

		int curread = in.read(buffer);
		while (curread > 0) {
			out.write(buffer, 0, curread);

			curread = in.read(buffer);
		}

		in.close();
		out.close();

		try {
			builder.start();
		} catch (Exception e) {
		}

		System.exit(0);
	}

	void setVersion(double v) {
		if (v <= 0.0)
			throw new IllegalArgumentException("Invalid version!");
		this.version = v;
	}

	void setCode(String cd) {
		this.code = cd;
	}

	void setDescription(String desc) {
		this.description = desc;
	}

	void setRequired(boolean required) {
		this.required = required;
	}

	void setLinkFor(PackageType pt, URI link) {
		if (pt == null)
			throw new NullPointerException("PackageType is NULL!");
		if (link == null)
			throw new NullPointerException("URI is NULL!");

		if (links.containsKey(pt))
			links.remove(pt);
		links.put(pt, link);
	}

	private void onUpdateError(Throwable e) {
		synchronized (listeners) {
			for (UpdateListener l : listeners)
				l.onUpdateError(this, e);
		}
	}

	private void onUpdateDownloading() {
		synchronized (listeners) {
			for (UpdateListener l : listeners)
				l.onUpdateDownloading(this);
		}
	}

	private void onUpdateDownloadError(Throwable e) {
		synchronized (listeners) {
			for (UpdateListener l : listeners)
				l.onUpdateDownloadError(this, e);
		}
	}

	private void onUpdateReady() {
		synchronized (listeners) {
			for (UpdateListener l : listeners)
				l.onUpdateReady(this);
		}
	}

	private void onUpdateApplying() {
		synchronized (listeners) {
			for (UpdateListener l : listeners)
				l.onUpdateApplying(this);
		}
	}

	private void onUpdateApplyError(Throwable e) {
		U.log("Apply error", e);

		synchronized (listeners) {
			for (UpdateListener l : listeners)
				l.onUpdateApplyError(this, e);
		}
	}

	private static void log(Object... obj) {
		U.log("[Updater]", obj);
	}

	private static String getMessageForStep(int step, String description) {
		String r = "Illegal action on step #" + step;

		if (description != null)
			r += " : " + description;

		return r;
	}

	public class IllegalStepException extends RuntimeException {
		private static final long serialVersionUID = -1988019882288031411L;

		IllegalStepException(int step, String description) {
			super(getMessageForStep(step, description));
		}

		IllegalStepException(int step) {
			super(getMessageForStep(step, null));
		}

	}
}
