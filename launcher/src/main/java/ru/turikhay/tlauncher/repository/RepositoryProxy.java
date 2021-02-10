package ru.turikhay.tlauncher.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

public class RepositoryProxy {
    public static final String[] PROXIFIED_HOSTS = new String[]{
            "launchermeta.mojang.com",
            "libraries.minecraft.net"
    };
    private static final String[] PROXIES = new String[] {
            "https://mcproxy.tlauncher.ru/proxy.php?url=",
            "https://mcproxy.tlaun.ch/proxy.php?url="
    };
    private static boolean PROXY_WORKED = false;

    private static ProxyRepoList proxyRepoList;
    public static ProxyRepoList getProxyRepoList() {
        if(proxyRepoList == null) {
            proxyRepoList = new ProxyRepoList();
        }
        return proxyRepoList;
    }

    public static class ProxyRepo implements IRepo {
        private static final Logger LOGGER = LogManager.getLogger(ProxyRepo.class);

        private final String proxyPrefix;

        public ProxyRepo(String proxy) {
            this.proxyPrefix = StringUtil.requireNotBlank(proxy);
        }

        @Override
        public URLConnection get(String path, int timeout, Proxy proxy) throws IOException {
            return get(path, timeout, proxy, 1);
        }

        public URLConnection get(String path, int timeout, Proxy proxy, int attempt) throws IOException {
            URL originalUrl = makeHttpUrl(path);

            IOException ioE = new IOException("not a first attempt; failed");
            if(attempt == 1) {
                LOGGER.debug("First attempt, no proxy: {}", originalUrl);
                try {
                    return openHttpConnection(originalUrl, proxy, timeout);
                } catch(IOException ioE1) {
                    ioE = ioE1;
                    LOGGER.warn("Failed to open connection to {}; error: {}", originalUrl, ioE.toString());
                    LOGGER.debug(ioE);
                }
            } else {
                LOGGER.debug("Using proxy: {}", path);
            }

            boolean accepted = false;
            for (String acceptedHost : PROXIFIED_HOSTS) {
                if(acceptedHost.equals(originalUrl.getHost())) {
                    accepted = true;
                    break;
                }
            }

            String hostIp;
            try {
                hostIp = InetAddress.getByName(originalUrl.getHost()).getHostAddress();
            } catch(Exception e) {
                hostIp = e.toString();
            }
            LOGGER.info("Resolved host {}: {}", originalUrl.getHost(), hostIp);

            if(!accepted) {
                LOGGER.warn("Host is not whitelisted to use proxy: {}", originalUrl.getHost());
                throw ioE;
            }

            String proxyRequestUrl = proxyPrefix + encodeUrl(originalUrl);
            LOGGER.debug("Proxying request to {}: {}", originalUrl, proxyRequestUrl);

            HttpURLConnection connection;
            try {
                connection = openHttpConnection(proxyRequestUrl, proxy, timeout);
            } catch(IOException oneMoreIOE) {
                LOGGER.error("Proxying request failed! URL: {}", proxyRequestUrl);
                throw oneMoreIOE;
            }

            LOGGER.warn("Using proxy ({}) to: {}", connection.getURL().getHost(), originalUrl);

            if(!PROXY_WORKED) {
                PROXY_WORKED = true;
            }

            return connection;
        }

        private HttpURLConnection openHttpConnection(String path, Proxy proxy, int timeout) throws IOException {
            HttpURLConnection httpURLConnection = (HttpURLConnection) makeHttpUrl(path).openConnection(proxy);
            setupTimeout(httpURLConnection, timeout);
            return httpURLConnection;
        }

        private HttpURLConnection openHttpConnection(URL url, Proxy proxy, int timeout) throws IOException {
            checkHttpUrl(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
            setupTimeout(httpURLConnection, timeout);
            return httpURLConnection;
        }

        private static void setupTimeout(HttpURLConnection connection, int timeout) {
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
        }

        private URL makeHttpUrl(String path) throws IOException {
            StringUtil.requireNotBlank(path, "path");
            URL url = new URL(path);
            checkHttpUrl(url);
            return url;
        }

        private void checkHttpUrl(URL url) {
            U.requireNotNull(url, "url");
            if(!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
                throw new IllegalArgumentException("not an http protocol: " + url);
            }
        }

        private String encodeUrl(URL url) {
            try {
                return URLEncoder.encode(url.toExternalForm(), FileUtil.getCharset().name());
            } catch (UnsupportedEncodingException e) {
                throw new Error("UTF-8 not supported?");
            }
        }
    }

    public static class ProxyRepoList extends RepoList {
        private ProxyRepoList() {
            super("ProxyRepo");

            for (String proxy : PROXIES){ //U.shuffle(PROXIES)) {
                add(new ProxyRepo(proxy));
            }
        }


        @Override
        protected URLConnection connect(IRepo repo, String path, int timeout, Proxy proxy, int attempt) throws IOException {
            if(repo instanceof ProxyRepo) {
                return ((ProxyRepo) repo).get(path, timeout, proxy, attempt);
            } else {
                return super.connect(repo, path, timeout, proxy, attempt);
            }
        }
    }
}
