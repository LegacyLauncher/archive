package net.legacylauncher.repository;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.connection.ConnectionQueue;
import net.legacylauncher.connection.ConnectionSelector;
import net.legacylauncher.util.EConnection;
import net.legacylauncher.util.EConnector;
import net.legacylauncher.util.U;
import net.legacylauncher.util.ua.LauncherUserAgent;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RepositoryProxy {
    public static final List<String> PROXIFIED_HOSTS = Collections.unmodifiableList(Arrays.asList(
            "launchermeta.mojang.com",
            "piston-meta.mojang.com",
            "libraries.minecraft.net",
            "launcher.mojang.com",
            "resources.download.minecraft.net",
            "files.minecraftforge.net",
            "maven.minecraftforge.net",
            "maven.fabricmc.net",
            "piston-data.mojang.com",
            "maven.quiltmc.org",
            "maven.neoforged.net",
            "modloaders.forgecdn.net"
    ));
    private static boolean PROXY_WORKED = false;

    public static boolean canBeProxied(URL url) {
        return PROXIFIED_HOSTS.stream().anyMatch(host -> url.getHost().equals(host));
    }

    public static String requestMaybeProxy(String _url) throws IOException {
        List<URL> urls = new ArrayList<>();
        URL url = U.makeURL(_url, true);
        urls.add(url);
        if (RepositoryProxy.canBeProxied(url)) {
            RepositoryProxy.getProxyRepoList().getRelevant().getList().stream()
                    .filter(r -> r instanceof ProxyRepo)
                    .map(r -> ((ProxyRepo) r).toProxyUrl(url))
                    .forEach(urls::add);
        }
        ConnectionQueue<EConnection> queue;
        try {
            queue = ConnectionSelector.create(
                    new EConnector(),
                    5,
                    TimeUnit.SECONDS
            ).select(urls).get();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
        while (true) {
            EConnection connection;
            try {
                connection = queue.takeOrThrow();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted", e);
            }
            String content;
            try {
                content = connection.getResponse().returnContent().asString();
            } catch (IOException e) {
                log.warn("Failed to read response from {}", connection.getUrl(), e);
                continue;
            }
            queue.close();
            return content;
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
    @ToString(of = "proxyHost")
    public static class ProxyRepo implements IRepo {
        private final String proxyHost;

        public ProxyRepo(String host) {
            this.proxyHost = host;
        }

        @Override
        public URLConnection get(String path, int timeout, Proxy proxy) throws IOException {
            return get(path, timeout, proxy, 1);
        }

        @Override
        public List<String> getHosts() {
            return Collections.singletonList(proxyHost);
        }

        public URLConnection get(String path, int timeout, Proxy proxy, int attempt) throws IOException {
            URL originalUrl = new URL(path);

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

            URL proxyRequestUrl = toProxyUrl(originalUrl);
            log.debug("Proxying {} to {}", originalUrl, proxyRequestUrl);

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

        public URL toProxyUrl(URL originalUrl) {
            String url = String.format(Locale.ROOT, "https://%s/%s%s",
                    proxyHost, originalUrl.getHost(), originalUrl.getPath());
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                throw new Error(e);
            }
        }

        private static HttpURLConnection openHttpConnection(URL url, Proxy proxy, int timeout) throws IOException {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
            setup(httpURLConnection, timeout);
            return httpURLConnection;
        }

        private static void setup(HttpURLConnection connection, int timeout) {
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            LauncherUserAgent.set(connection);
        }
    }

    public static class ProxyRepoList extends RepoList {
        private ProxyRepoList() {
            super("ProxyRepo");

            for (String proxy : HostsV1.PROXY) {
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
