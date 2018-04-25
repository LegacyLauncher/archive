package ru.turikhay.tlauncher.repository;

import ru.turikhay.tlauncher.sentry.Sentry;
import ru.turikhay.util.DataBuilder;
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
            "https://u.tlauncher.ru/proxy.php?url=",
            "https://turikhay.ru/proxy.php?url=",
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
        private final String proxyPrefix, logPrefix;

        public ProxyRepo(String proxy) {
            this.proxyPrefix = StringUtil.requireNotBlank(proxy);
            try {
                logPrefix = "[" + getClass().getSimpleName() + ":"+ new URL(proxy).getHost() +"]";
            } catch(Exception e) {
                throw new Error(e);
            }
        }

        @Override
        public URLConnection get(String path, int timeout, Proxy proxy) throws IOException {
            return get(path, timeout, proxy, 1);
        }

        public URLConnection get(String path, int timeout, Proxy proxy, int attempt) throws IOException {
            URL originalUrl = makeHttpUrl(path);

            IOException ioE = new IOException("not a first attempt; failed");
            if(attempt == 1) {
                log("First attempt: without proxyfying: " + path);
                try {
                    return openHttpConnection(originalUrl, proxy);
                } catch(IOException ioE1) {
                    ioE = ioE1;
                    log("Failed to open connection: " + path, ioE);
                }
            } else {
                log("Skipping attempt without proxyfying: " + path);
            }

            DataBuilder dataBuilder = DataBuilder.create("url", originalUrl).add("ioE", ioE);

            boolean accepted = false;
            for (String acceptedHost : PROXIFIED_HOSTS) {
                if(acceptedHost.equals(originalUrl.getHost())) {
                    accepted = true;
                    break;
                }
            }
            dataBuilder.add("accepted", accepted);

            String hostIp;
            try {
                hostIp = InetAddress.getByName(originalUrl.getHost()).getHostAddress();
            } catch(Exception e) {
                hostIp = e.toString();
            }
            log("Host: " + originalUrl.getHost() + ", IP: " + hostIp);
            dataBuilder.add("host", originalUrl.getHost()).add("hostIp", hostIp);

            if(!accepted) {
                log("Host is not whitelisted: " + originalUrl.getHost());
                Sentry.sendWarning(RepositoryProxy.class, "host is not whitelisted: " + originalUrl.getHost(), dataBuilder);
                throw ioE;
            }

            log("Proxyfying request: " + originalUrl);
            HttpURLConnection connection;
            try {
                connection = openHttpConnection(proxyPrefix + encodeUrl(originalUrl), proxy);
            } catch(IOException oneMoreIOE) {
                log("Proxy failed too!", oneMoreIOE);
                dataBuilder.add("proxy_ioE", oneMoreIOE);
                Sentry.sendError(RepositoryProxy.class, "proxyfying failed", oneMoreIOE, dataBuilder);
                throw oneMoreIOE;
            }

            if(!PROXY_WORKED) {
                Sentry.sendWarning(RepositoryProxy.class, "proxy is being used", dataBuilder);
                PROXY_WORKED = true;
            }

            return connection;
        }

        private HttpURLConnection openHttpConnection(String path, Proxy proxy) throws IOException {
            return (HttpURLConnection) makeHttpUrl(path).openConnection(proxy);
        }

        private HttpURLConnection openHttpConnection(URL url, Proxy proxy) throws IOException {
            checkHttpUrl(url);
            return (HttpURLConnection) url.openConnection(proxy);
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

        private void log(Object... obj) {
            U.log(logPrefix, obj);
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
