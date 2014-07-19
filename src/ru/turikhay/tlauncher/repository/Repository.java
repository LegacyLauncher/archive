package ru.turikhay.tlauncher.repository;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import net.minecraft.launcher.Http;

public enum Repository {
	LOCAL_VERSION_REPO,
	OFFICIAL_VERSION_REPO(TLauncher.getOfficialRepo()),
	EXTRA_VERSION_REPO(TLauncher.getExtraRepo()),

	ASSETS_REPO(TLauncher.getAssetsRepo()),
	LIBRARY_REPO(TLauncher.getLibraryRepo()),

	SERVERLIST_REPO(TLauncher.getServerList());

	private static final int DEFAULT_TIMEOUT = 5000;
	public static final Repository[] VERSION_REPOS = getVersionRepos();

	private final String lowerName;
	private final List<String> repos;

	private int primaryTimeout, selected;
	private boolean isSelected;

	Repository(int timeout, String[] urls) {
		if (urls == null)
			throw new NullPointerException("URL array is NULL!");

		this.lowerName = super.name().toLowerCase();
		this.repos = new ArrayList<String>();

		this.setTimeout(timeout);
		Collections.addAll(repos, urls);
	}

	Repository(String[] urls) {
		this(DEFAULT_TIMEOUT, urls);
	}

	Repository(int timeout) {
		this(timeout, new String[0]);
	}

	Repository() {
		this(DEFAULT_TIMEOUT, new String[0]);
	}

	public int getTimeout() {
		return primaryTimeout;
	}

	int getSelected() {
		return selected;
	}

	public synchronized void selectNext() {
		if (++selected >= getCount())
			selected = 0;
	}

	void setSelected(int pos) {
		if (!isSelectable())
			throw new IllegalStateException();

		this.isSelected = true;
		this.selected = pos;
	}

	public String getSelectedRepo() {
		return repos.get(selected);
	}

	String getRepo(int pos) {
		return repos.get(pos);
	}

	public List<String> getList() {
		return repos;
	}

	public int getCount() {
		return repos.size();
	}

	boolean isSelected() {
		return isSelected;
	}

	public boolean isSelectable() {
		return !repos.isEmpty();
	}

	@SuppressWarnings("null")
	String getUrl(String uri, boolean selectPath) throws IOException {
		boolean canSelect = isSelectable();

		if (!canSelect)
			return getRawUrl(uri);

		boolean gotError = false;

		if (!selectPath && isSelected())
			try {
				return this.getRawUrl(uri);
			} catch (IOException e) {
				gotError = true;
				log("Cannot get required URL, reselecting path.");
			}

		log("Selecting relevant path...");

		Object lock = new Object();

		IOException e = null;
		int i = 0, attempt = 0, exclude = (gotError) ? getSelected() : -1;

		while (i < 3) {
			++i;
			int timeout = primaryTimeout * i;

			for (int x = 0; x < getCount(); x++) {
				if (i == 1 && x == exclude)
					continue; // Exclude bad path at the first try

				++attempt;
				log("Attempt #" + attempt + "; timeout: " + timeout
						+ " ms; url: " + getRepo(x));

				Time.start(lock);

				try {
					String result = Http.performGet(new URL(getRepo(x) + uri),
							timeout, timeout);
					setSelected(x);

					log("Success: Reached the repo in", Time.stop(lock), "ms.");
					return result;

				} catch (IOException e0) {
					log("Failed: Repo is not reachable!");
					e = e0;
				}

				Time.stop(lock);
			}
		}

		log("Failed: All repos are unreachable.");
		throw e;
	}

	public String getUrl(String uri) throws IOException {
		return this.getUrl(uri, false);
	}

	public String getUrl() throws IOException {
		return this.getUrl("", false);
	}

	String getRawUrl(String uri) throws IOException {
		String url = getSelectedRepo() + Http.encode(uri);

		try {
			return Http.performGet(new URL(url));
		} catch (IOException e) {
			log("Cannot get raw:", url);
			throw e;
		}
	}

	@Override
	public String toString() {
		return lowerName;
	}

	void setTimeout(int ms) {
		if (ms < 0)
			throw new IllegalArgumentException("Negative timeout: " + ms);

		this.primaryTimeout = ms;
	}

	void log(Object... obj) {
		U.log("[REPO][" + name() + "]", obj);
	}

	public static Repository[] getVersionRepos() {
		return new Repository[]{ Repository.LOCAL_VERSION_REPO, Repository.OFFICIAL_VERSION_REPO, Repository.EXTRA_VERSION_REPO };
	}
}
