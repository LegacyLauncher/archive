package ru.turikhay.tlauncher.downloader;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import ru.turikhay.tlauncher.handlers.SimpleHostnameVerifier;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class Downloadable {
	private final static boolean DEFAULT_FORCE = false;
	private final static boolean DEFAULT_FAST = false;

	private String path;
	private Repository repo;

	private File destination;
	private final List<File> additionalDestinations;

	private boolean forceDownload, fastDownload, insertUseragent, locked;

	private DownloadableContainer container;
	private final List<DownloadableHandler> handlers;

	private Throwable error;

	private Downloadable() {
		this.additionalDestinations = Collections
				.synchronizedList(new ArrayList<File>());
		this.handlers = Collections
				.synchronizedList(new ArrayList<DownloadableHandler>());
	}

	public Downloadable(Repository repo, String path, File destination,
			boolean forceDownload, boolean fastDownload) {
		this();

		this.setURL(repo, path);
		this.setDestination(destination);

		this.forceDownload = forceDownload;
		this.fastDownload = fastDownload;
	}

	public Downloadable(Repository repo, String path, File destination,
			boolean forceDownload) {
		this(repo, path, destination, forceDownload, DEFAULT_FAST);
	}

	public Downloadable(Repository repo, String path, File destination) {
		this(repo, path, destination, DEFAULT_FORCE, DEFAULT_FAST);
	}

	private Downloadable(String url, File destination, boolean forceDownload,
			boolean fastDownload) {
		this();

		this.setURL(url);
		this.setDestination(destination);

		this.forceDownload = forceDownload;
		this.fastDownload = fastDownload;
	}

	public Downloadable(String url, File destination) {
		this(url, destination, DEFAULT_FORCE, DEFAULT_FAST);
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(this == o) return true;

		if(!(o instanceof Downloadable))
			return false;

		Downloadable c = (Downloadable) o;
		return
				U.equal(path, c.path)
				&& U.equal(repo, c.repo)
				&& U.equal(destination, c.destination)
				&& U.equal(additionalDestinations, c.additionalDestinations);
	}

	public boolean getInsertUA() {
		return insertUseragent;
	}

	public void setInsertUA(boolean ua) {
		checkLocked();
		this.insertUseragent = ua;
	}

	public boolean isForce() {
		return forceDownload;
	}

	public void setForce(boolean force) {
		checkLocked();
		this.forceDownload = force;
	}

	public boolean isFast() {
		return fastDownload;
	}

	public void setFast(boolean fast) {
		checkLocked();
		this.fastDownload = fast;
	}

	public String getURL() {
		return path;
	}

	public Repository getRepository() {
		return repo;
	}

	public boolean hasRepository() {
		return repo != null;
	}

	void setURL(Repository repo, String path) {
		if (repo == null)
			throw new NullPointerException("Repository is NULL!");

		if (path == null)
			throw new NullPointerException("Path is NULL!");

		checkLocked();

		this.repo = repo;
		this.path = path;
	}

	void setURL(String url) {
		if (url == null)
			throw new NullPointerException();

		if (url.isEmpty())
			throw new IllegalArgumentException("URL cannot be empty!");

		checkLocked();

		this.repo = null;
		this.path = url;
	}

	public File getDestination() {
		return destination;
	}

	public String getFilename() {
		return FileUtil.getFilename(path);
	}

	void setDestination(File file) {
		if (file == null)
			throw new NullPointerException();

		checkLocked();
		this.destination = file;
	}

	public List<File> getAdditionalDestinations() {
		return Collections.unmodifiableList(additionalDestinations);
	}

	public void addAdditionalDestination(File file) {
		if (file == null)
			throw new NullPointerException();

		checkLocked();
		this.additionalDestinations.add(file);
	}

	public DownloadableContainer getContainer() {
		return container;
	}

	public boolean hasContainer() {
		return container != null;
	}

	public boolean hasConsole() {
		return container != null && container.hasConsole();
	}

	public void addHandler(DownloadableHandler handler) {
		if (handler == null)
			throw new NullPointerException();

		checkLocked();
		handlers.add(handler);
	}

	void setContainer(DownloadableContainer container) {
		checkLocked();
		this.container = container;
	}

	public Throwable getError() {
		return error;
	}

	private void setLocked(boolean locked) {
		this.locked = locked;
	}

	void checkLocked() {
		if (locked)
			throw new IllegalStateException("Downloadable is locked!");
	}

	void onStart() {
		setLocked(true);

		for (DownloadableHandler handler : handlers)
			handler.onStart(this);
	}

	void onAbort(AbortedDownloadException ae) {
		setLocked(false);

		this.error = ae;

		for (DownloadableHandler handler : handlers)
			handler.onAbort(this);

		if (container != null)
			container.onAbort(this);
	}

	void onComplete() throws RetryDownloadException {
		setLocked(false);

		for (DownloadableHandler handler : handlers)
			handler.onComplete(this);

		if (container != null)
			container.onComplete(this);
	}

	void onError(Throwable e) {
		this.error = e;

		if (e == null)
			return;

		setLocked(false);

		for (DownloadableHandler handler : handlers)
			handler.onError(this, e);

		if(container != null)
			container.onError(this, e);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{path='" + path + "'; " + "repo="
				+ repo + "; " + "destinations=" + destination + ","
				+ additionalDestinations + "; " + "force=" + forceDownload
				+ "; " + "fast=" + fastDownload + "; " + "locked=" + locked
				+ "; " + "container=" + container + "; " + "handlers="
				+ handlers + "; " + "error=" + error + ";" + "}";
	}

	public static HttpURLConnection setUp(URLConnection connection0,
			int timeout, boolean fake) {
		if (connection0 == null)
			throw new NullPointerException();

		if (!(connection0 instanceof HttpURLConnection))
			throw new IllegalArgumentException("Unknown connection protocol: "
					+ connection0);

		HttpURLConnection connection = (HttpURLConnection) connection0;

		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);

		connection.setUseCaches(false);
		connection.setDefaultUseCaches(false);
		connection.setRequestProperty("Cache-Control",
				"no-store,max-age=0,no-cache");
		connection.setRequestProperty("Expires", "0");
		connection.setRequestProperty("Pragma", "no-cache");

		HttpsURLConnection securedConnection = Reflect.cast(connection, HttpsURLConnection.class);

		if(securedConnection != null)
			securedConnection.setHostnameVerifier(SimpleHostnameVerifier.getInstance());

		if (!fake)
			return connection;

		String userAgent;

		switch(OS.CURRENT) {
		case OSX:
			userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8) AppleWebKit/535.18.5 (KHTML, like Gecko) Version/5.2 Safari/535.18.5";
			break;
		case WINDOWS:
			userAgent = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0; .NET4.0C)";
			break;
		default:
			userAgent = "Mozilla/5.0 (Linux; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0";
			break;
		}

		connection.setRequestProperty("User-Agent", userAgent);

		return connection;
	}

	public static HttpURLConnection setUp(URLConnection connection, int timeout) {
		return setUp(connection, timeout, false);
	}

	public static HttpURLConnection setUp(URLConnection connection, boolean fake) {
		return setUp(connection, U.getConnectionTimeout(), fake);
	}

	public static HttpURLConnection setUp(URLConnection connection) {
		return setUp(connection, false);
	}

	public static String getEtag(String etag) {
		if (etag == null)
			return "-";

		if ((etag.startsWith("\"")) && (etag.endsWith("\"")))
			return etag.substring(1, etag.length() - 1);

		return etag;
	}

}
