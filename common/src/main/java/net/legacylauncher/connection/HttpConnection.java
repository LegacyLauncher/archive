package net.legacylauncher.connection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

@Value
public class HttpConnection implements Connection {
    HttpURLConnection connection;

    public URL getUrl() {
        return connection.getURL();
    }

    @Override
    public void disconnect() {
        connection.disconnect();
    }

    public static HttpConnection of(URLConnection connection) {
        return new HttpConnection((HttpURLConnection) connection);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static
    class Connector implements UrlConnector<HttpConnection> {
        public static final int CONNECT_TIMEOUT = 15_000, READ_TIMEOUT = 10_000;
        protected final int connect, read;
        protected final String userAgent;
        protected final Proxy proxy;

        public Connector(long connect, long read, String userAgent, Proxy proxy) {
            this.connect = checkInt(connect);
            this.read = checkInt(read);
            this.userAgent = userAgent;
            this.proxy = proxy;
        }

        @Override
        public HttpConnection connect(ConnectionInfo info) throws IOException {
            HttpURLConnection delegate = openConnection(info.getUrl(), proxy);
            delegate.setConnectTimeout(connect);
            delegate.setReadTimeout(read);
            if (userAgent != null) {
                delegate.setRequestProperty("User-Agent", userAgent);
            }
            delegate.connect();
            return new HttpConnection(delegate);
        }

        public static HttpURLConnection openConnection(URL url, Proxy proxy) throws IOException {
            return (HttpURLConnection) url.openConnection(proxy);
        }

        private static int checkInt(long value) {
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw new IllegalArgumentException();
            }
            return (int) value;
        }
    }
}
