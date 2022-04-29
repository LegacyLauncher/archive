package ru.turikhay.tlauncher.downloader;

import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Downloadable {
    private static final boolean DEFAULT_FORCE = false;
    private static final boolean DEFAULT_FAST = false;
    private String path;
    private Repository repo;
    private File destination;
    private final List<File> additionalDestinations;
    private boolean forceDownload;
    private boolean fastDownload;
    private boolean insertUseragent;
    private boolean locked;
    private DownloadableContainer container;
    private final List<DownloadableHandler> handlers;
    private Throwable error;

    protected Downloadable() {
        additionalDestinations = Collections.synchronizedList(new ArrayList<>());
        handlers = Collections.synchronizedList(new ArrayList<>());
    }

    public Downloadable(Repository repo, String path, File destination, boolean forceDownload, boolean fastDownload) {
        this();
        setURL(repo, path);
        setDestination(destination);
        this.forceDownload = forceDownload;
        this.fastDownload = fastDownload;
    }

    public Downloadable(Repository repo, String path, File destination, boolean forceDownload) {
        this(repo, path, destination, forceDownload, false);
    }

    public Downloadable(Repository repo, String path, File destination) {
        this(repo, path, destination, false, false);
    }

    private Downloadable(String url, File destination, boolean forceDownload, boolean fastDownload) {
        this();
        setURL(url);
        setDestination(destination);
        this.forceDownload = forceDownload;
        this.fastDownload = fastDownload;
    }

    public Downloadable(String url, File destination) {
        this(url, destination, false, false);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (this == o) {
            return true;
        } else if (!(o instanceof Downloadable)) {
            return false;
        } else {
            Downloadable c = (Downloadable) o;
            return Objects.equals(path, c.path) && Objects.equals(repo, c.repo) && Objects.equals(destination, c.destination) && Objects.equals(additionalDestinations, c.additionalDestinations);
        }
    }

    public boolean getInsertUA() {
        return insertUseragent;
    }

    public void setInsertUA(boolean ua) {
        checkLocked();
        insertUseragent = ua;
    }

    public boolean isForce() {
        return forceDownload;
    }

    public void setForce(boolean force) {
        checkLocked();
        forceDownload = force;
    }

    public boolean isFast() {
        return fastDownload;
    }

    public void setFast(boolean fast) {
        checkLocked();
        fastDownload = fast;
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

    protected void setURL(Repository repo, String path) {
        if (repo == null) {
            throw new NullPointerException("repo");
        }

        if (path == null) {
            throw new NullPointerException("path");
        }

        checkLocked();
        this.repo = repo;
        this.path = path;
    }

    protected void setURL(String url) {
        if (url == null) {
            throw new NullPointerException();
        } else if (url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty!");
        } else {
            checkLocked();
            repo = null;
            path = url;
        }
    }

    public File getDestination() {
        return destination;
    }

    public String getFilename() {
        return FileUtil.getFilename(path);
    }

    protected void setDestination(File file) {
        if (file == null) {
            throw new NullPointerException();
        } else {
            checkLocked();
            destination = file;
        }
    }

    public List<File> getAdditionalDestinations() {
        return Collections.unmodifiableList(additionalDestinations);
    }

    public void addAdditionalDestination(File file) {
        if (file == null) {
            throw new NullPointerException();
        } else {
            checkLocked();
            additionalDestinations.add(file);
        }
    }

    public DownloadableContainer getContainer() {
        return container;
    }

    public boolean hasContainer() {
        return container != null;
    }

    public void addHandler(DownloadableHandler handler) {
        if (handler == null) {
            throw new NullPointerException();
        } else {
            checkLocked();
            handlers.add(handler);
        }
    }

    protected void setContainer(DownloadableContainer container) {
        checkLocked();
        this.container = container;
    }

    public Throwable getError() {
        return error;
    }

    private void setLocked(boolean locked) {
        this.locked = locked;
    }

    protected void checkLocked() {
        if (locked) {
            throw new IllegalStateException("Downloadable is locked!");
        }
    }

    protected void onStart() {
        setLocked(true);

        for (DownloadableHandler handler : handlers) {
            handler.onStart(this);
        }

    }

    protected void onAbort(AbortedDownloadException ae) {
        setLocked(false);
        error = ae;

        for (DownloadableHandler handler : handlers) {
            handler.onAbort(this);
        }

        if (container != null) {
            container.onAbort(this);
        }

    }

    protected void onComplete() throws RetryDownloadException {
        setLocked(false);

        for (DownloadableHandler handler : handlers) {
            handler.onComplete(this);
        }

        if (container != null) {
            container.onComplete(this);
        }

    }

    protected void onError(Throwable e) {
        error = e;
        if (e != null) {
            setLocked(false);

            for (DownloadableHandler handler : handlers) {
                handler.onError(this, e);
            }

            if (container != null) {
                container.onError(this, e);
            }

        }
    }

    public String toString() {
        return getClass().getSimpleName() + "{path='" + path + "'; " + "repo=" + repo + "; " + "destinations=" + destination + "," + additionalDestinations + "; " + "force=" + forceDownload + "; " + "fast=" + fastDownload + "; " + "locked=" + locked + "; " + "container=" + container + "; " + "handlers=" + handlers + "; " + "error=" + error + ";" + "}";
    }

    public static HttpURLConnection setUp(URLConnection connection0, int timeout, boolean fake) {
        if (connection0 == null) {
            throw new NullPointerException();
        } else if (!(connection0 instanceof HttpURLConnection)) {
            throw new IllegalArgumentException("Unknown connection protocol: " + connection0);
        } else {
            HttpURLConnection connection = (HttpURLConnection) connection0;
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            connection.setRequestProperty("Pragma", "no-cache"); // HTTP 1.0.
            connection.setRequestProperty("Expires", "0"); // Proxies.
//            connection.setRequestProperty("User-Agent", "");

            return connection;
            /*if (!fake) {
                return connection;
            } else {
                String userAgent;
                switch (OS.CURRENT) {
                    case WINDOWS:
                        userAgent = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0; .NET4.0C)";
                        break;
                    case OSX:
                        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8) AppleWebKit/535.18.5 (KHTML, like Gecko) Version/5.2 Safari/535.18.5";
                        break;
                    default:
                        userAgent = "Mozilla/5.0 (Linux; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0";
                }

                connection.setRequestProperty("User-Agent", userAgent);
                return connection;
            }*/
        }
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
        return etag == null ? "-" : (etag.startsWith("\"") && etag.endsWith("\"") ? etag.substring(1, etag.length() - 1) : etag);
    }
}
