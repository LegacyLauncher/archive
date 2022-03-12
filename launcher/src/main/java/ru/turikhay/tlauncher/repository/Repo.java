package ru.turikhay.tlauncher.repository;

import ru.turikhay.util.U;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

public abstract class Repo implements IRepo {
    private final String name;

    public Repo(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String toString() {
        return name;
    }

    @Override
    public final URLConnection get(String path, int timeout, Proxy proxy) throws IOException {
        URL url = Objects.requireNonNull(makeUrl(path), "url");
        return makeConnection(url, timeout, proxy);
    }

    public final URLConnection get(String path, int timeout) throws IOException {
        return get(path, timeout, U.getProxy());
    }

    public final URLConnection get(String path) throws IOException {
        return get(path, U.getReadTimeout());
    }

    protected URLConnection makeConnection(URL url, int timeout, Proxy proxy) throws IOException {
        URLConnection connection = url.openConnection(proxy);

        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        connection.setRequestProperty("Pragma", "no-cache"); // HTTP 1.0.
        connection.setRequestProperty("Expires", "0"); // Proxies.

        return connection;
    }

    protected abstract URL makeUrl(String path) throws IOException;
}
