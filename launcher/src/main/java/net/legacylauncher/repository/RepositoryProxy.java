package net.legacylauncher.repository;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.util.EHttpClient;
import net.legacylauncher.util.StringUtil;
import net.legacylauncher.util.U;
import org.apache.hc.client5.http.fluent.Request;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


public class RepositoryProxy {
    public static final List<String> PROXIFIED_HOSTS = Collections.unmodifiableList(Arrays.asList(
            "launchermeta.mojang.com",
            "piston-meta.mojang.com",
            "libraries.minecraft.net",
            "launcher.mojang.com",
            "resources.download.minecraft.net",
            "files.minecraftforge.net",
            "maven.fabricmc.net",
            "piston-data.mojang.com"
    ));
    private static final List<String> PROXIES = Collections.unmodifiableList(
            RepoPrefixV1.prefixesCdnLast().stream().map(prefix -> String.format(Locale.ROOT,
                    "%s/proxy.php?url=", prefix
            )).collect(Collectors.toList())
    );
    private static boolean PROXY_WORKED = false;

    public static boolean canBeProxied(URL url) {
        return PROXIFIED_HOSTS.stream().anyMatch(host -> url.getHost().equals(host));
    }

    public static String requestMaybeProxy(String url) throws IOException {
        List<String> urls = new ArrayList<>();
        urls.add(url);
        {
            URL strictUrl = U.makeURL(url);
            if (RepositoryProxy.canBeProxied(strictUrl)) {
                RepositoryProxy.getProxyRepoList().getRelevant().getList().stream()
                        .filter(r -> r instanceof ProxyRepo)
                        .map(r -> ((ProxyRepo) r).prefixUrl(strictUrl))
                        .forEach(urls::add);
            }
        }
        IOException ex = null;
        for (String currentUrl : urls) {
            ProxyRepo.log.debug("Requesting: {}", currentUrl);
            try {
                return EHttpClient.toString(Request.get(currentUrl));
            } catch (IOException ioE) {
                ProxyRepo.log.warn("Couldn't fetch url {}", url, ioE);
                if (ex == null) {
                    ex = ioE;
                } else {
                    ex.addSuppressed(ioE);
                }
            }
        }

        if (ex != null) {
            throw ex;
        } else {
            throw new IOException("Unable to fetch data over network due to unknown reason");
        }
    }

    private static ProxyRepoList proxyRepoList;

    public static ProxyRepoList getProxyRepoList() {
        if (proxyRepoList == null) {
            proxyRepoList = new ProxyRepoList();
        }
        return proxyRepoList;
    }

    @Slf4j
    public static class ProxyRepo implements IRepo {
        private final String proxyPrefix;

        public ProxyRepo(String proxy) {
            this.proxyPrefix = StringUtil.requireNotBlank(proxy);
        }

        @Override
        public URLConnection get(String path, int timeout, Proxy proxy) throws IOException {
            return get(path, timeout, proxy, 1);
        }

        public String prefixUrl(URL url) {
            return proxyPrefix + encodeUrl(url);
        }

        @Override
        public List<String> getHosts() {
            return Collections.singletonList(U.parseHost(proxyPrefix));
        }

        public URLConnection get(String path, int timeout, Proxy proxy, int attempt) throws IOException {
            URL originalUrl = makeHttpUrl(path);

            IOException ioE = new IOException("not a first attempt; failed");
            if (attempt == 1) {
                log.debug("First attempt, no proxy: {}", originalUrl);
                try {
                    return openHttpConnection(originalUrl, proxy, timeout);
                } catch (IOException ioE1) {
                    ioE = ioE1;
                    log.warn("Failed to open connection to {}; error: {}", originalUrl, ioE.toString());
                    log.debug("Full error:", ioE);
                }
            } else {
                log.debug("Using proxy: {}", path);
            }

            boolean accepted = false;
            for (String acceptedHost : PROXIFIED_HOSTS) {
                if (acceptedHost.equals(originalUrl.getHost())) {
                    accepted = true;
                    break;
                }
            }

            String hostIp;
            try {
                hostIp = InetAddress.getByName(originalUrl.getHost()).getHostAddress();
            } catch (Exception e) {
                hostIp = e.toString();
            }
            log.info("Resolved host {}: {}", originalUrl.getHost(), hostIp);

            if (!accepted) {
                log.warn("Host is not whitelisted to use proxy: {}", originalUrl.getHost());
                throw ioE;
            }

            String proxyRequestUrl = prefixUrl(originalUrl);
            log.debug("Proxying request to {}: {}", originalUrl, proxyRequestUrl);

            HttpURLConnection connection;
            try {
                connection = openHttpConnection(proxyRequestUrl, proxy, timeout);
            } catch (IOException oneMoreIOE) {
                log.error("Proxying request failed! URL: {}", proxyRequestUrl);
                throw oneMoreIOE;
            }

            log.warn("Using proxy ({}) to: {}", connection.getURL().getHost(), originalUrl);

            if (!PROXY_WORKED) {
                PROXY_WORKED = true;
            }

            return connection;
        }

        private static HttpURLConnection openHttpConnection(String path, Proxy proxy, int timeout) throws IOException {
            HttpURLConnection httpURLConnection = (HttpURLConnection) makeHttpUrl(path).openConnection(proxy);
            setupTimeout(httpURLConnection, timeout);
            return httpURLConnection;
        }

        private static HttpURLConnection openHttpConnection(URL url, Proxy proxy, int timeout) throws IOException {
            checkHttpUrl(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
            setupTimeout(httpURLConnection, timeout);
            return httpURLConnection;
        }

        private static void setupTimeout(HttpURLConnection connection, int timeout) {
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
        }

        private static URL makeHttpUrl(String path) throws IOException {
            StringUtil.requireNotBlank(path, "path");
            URL url = new URL(path);
            checkHttpUrl(url);
            return url;
        }

        private static void checkHttpUrl(URL url) {
            Objects.requireNonNull(url, "url");
            if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
                throw new IllegalArgumentException("not an http protocol: " + url);
            }
        }

        private static String encodeUrl(URL url) {
            try {
                return URLEncoder.encode(url.toExternalForm(), StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new Error("UTF-8 not supported?");
            }
        }
    }

    public static class ProxyRepoList extends RepoList {
        private ProxyRepoList() {
            super("ProxyRepo");

            List<String> proxies = new ArrayList<>(PROXIES);
            Collections.shuffle(proxies);

            for (String proxy : proxies) {
                add(new ProxyRepo(proxy));
            }
        }

        @Override
        protected URLConnection connect(IRepo repo, String path, int timeout, Proxy proxy, int attempt) throws IOException {
            if (repo instanceof ProxyRepo) {
                return ((ProxyRepo) repo).get(path, timeout, proxy, attempt);
            } else {
                return super.connect(repo, path, timeout, proxy, attempt);
            }
        }
    }
}
