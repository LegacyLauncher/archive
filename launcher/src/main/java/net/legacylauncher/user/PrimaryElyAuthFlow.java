package net.legacylauncher.user;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.util.async.ExtendedThread;
import net.legacylauncher.util.git.MapTokenResolver;
import net.legacylauncher.util.git.TokenReplacingReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class PrimaryElyAuthFlow extends ElyAuthFlow<PrimaryElyAuthFlowListener> {
    private static final String SERVER_ADDRESS = "http://127.0.0.1:%d";
    private static final String SERVER_ENTRY_POINT = "/";
    static final String SERVER_FULL_URL = SERVER_ADDRESS + SERVER_ENTRY_POINT, QUERY_CODE_KEY = "code", QUERY_STATE_KEY = "state";
    private static final int PORT_CREATING_TRIES = 5, SERVER_BACKLOG = 1, SERVER_STOP_DELAY = 5;

    static final String TOKEN_EXCHANGE_SUCCESS = ElyAuth.ACCOUNT_BASE + "/oauth2/code/success?appName=Legacy%20Launcher";

    HttpServerAdapter server;

    @Override
    protected ElyAuthCode fetchCode() throws ElyAuthStrategyException, InterruptedException {
        int state = generateState();
        URIWatchdog watchdog = createWatchdog(state);
        server = createServer(watchdog);
        try {
            server.start();
            openBrowser(server.getPort(), state);
            return watchdog.waitForCode();
        } finally {
            server.stop();
        }
    }

    @Override
    protected void onCancelled() {
        server.stop();
    }

    private String getRedirectUri(int port) {
        return String.format(java.util.Locale.ROOT, SERVER_FULL_URL, port);
    }

    URIWatchdog createWatchdog(int state) {
        return new URIWatchdog(state);
    }

    HttpServerAdapter createServer(URIWatchdog watchdog) throws ElyAuthStrategyException, InterruptedException {
        log.debug("Creating server...");

        int portCreatingTries = 0, port;
        IOException ex = null;
        do {
            checkCancelled();

            ++portCreatingTries;
            port = ThreadLocalRandom.current().nextInt(49152, 65535);

            log.debug("attempt {}; selected port: {}", portCreatingTries, port);

            HttpServerAdapter adapter;
            try {
                adapter = new HttpServerAdapter(watchdog, port, watchdog.getCheckState());
            } catch (IOException ioE) {
                if (ex == null) {
                    ex = ioE;
                } else {
                    ex.addSuppressed(ioE);
                }
                continue;
            } catch (Exception e) {
                throw new ElyAuthStrategyException("Unknown error", e);
            }

            for (PrimaryElyAuthFlowListener listener : getListenerList()) {
                listener.primaryStrategyServerCreated(this, port);
            }

            return adapter;
        } while (portCreatingTries < PORT_CREATING_TRIES);

        throw new ElyAuthStrategyException("Max port occupy tries exceed", ex);
    }

    void openBrowser(int port, int state) throws InterruptedException {
        openBrowser(getRedirectUri(port), state);
    }

    class HttpServerAdapter {
        private final URIWatchdog watchdog;
        private final HttpServer server;
        private final int port, state;

        private HttpServerAdapter(URIWatchdog watchdog, int port, int state) throws IOException {
            log.debug("Creating HttpServerAdapter at port {}", port);

            this.watchdog = Objects.requireNonNull(watchdog, "watchdog");
            this.port = port;
            this.state = state;

            server = HttpServer.create(new InetSocketAddress(port), SERVER_BACKLOG);
            server.createContext(SERVER_ENTRY_POINT, httpExchange -> {
                try {
                    log.debug("handling uri: {}", httpExchange.getRequestURI());
                    int responseStatus;
                    String response;

                    if (HttpServerAdapter.this.watchdog.passURI(httpExchange.getRequestURI(), getRedirectUri(HttpServerAdapter.this.port), HttpServerAdapter.this.state)) {
                        responseStatus = HttpURLConnection.HTTP_MOVED_TEMP;
                        response = TokenReplacingReader.resolveVars(SERVER_RESPONSE, new MapTokenResolver(new HashMap<String, String>() {
                            {
                                put("text", Localizable.get("account.manager.multipane.process-account-ely.flow.complete.response"));
                            }
                        }));
                    } else {
                        responseStatus = HttpURLConnection.HTTP_BAD_REQUEST;
                        response = "\r\n";
                    }

                    httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=" + StandardCharsets.UTF_8.name());
                    if (responseStatus == HttpURLConnection.HTTP_MOVED_TEMP) {
                        httpExchange.getResponseHeaders().set("Location", TOKEN_EXCHANGE_SUCCESS);
                    }
                    httpExchange.sendResponseHeaders(responseStatus, response.getBytes(StandardCharsets.UTF_8).length);

                    OutputStream out = httpExchange.getResponseBody();
                    IOUtils.write(response, out, StandardCharsets.UTF_8);
                    out.close();
                } catch (Exception e) {
                    log.error("Interrupting watchdog because of Server (port {}) failure", port, e);
                    HttpServerAdapter.this.watchdog.interrupt();
                }
            });
            server.setExecutor(null);

            log.debug("HttpServer created successfully");
        }

        int getPort() {
            return port;
        }

        int getState() {
            return state;
        }

        void start() {
            log.debug("Http server started at port {}", port);
            server.start();
        }

        void stop() {
            log.debug("Http server stopped at port {}", port);
            server.stop(SERVER_STOP_DELAY);
        }
    }

    class URIWatchdog extends ExtendedThread {
        private final Object wait = new Object();
        private final int state;

        private volatile URI uri;
        private ElyAuthCode code;

        private URIWatchdog(int state) {
            this.state = state;

            log.debug("Starting watchdog...");
            startAndWait();
            unlockThread("start");
            log.debug("Started");
        }

        int getCheckState() {
            return state;
        }

        ElyAuthCode parseURI(URI uri, String redirect_uri, int state) {
            String query = uri.toString().substring(SERVER_ENTRY_POINT.length() + 1 /* including "?" */);
            Map<String, String> queryMap = splitQuery(query);

            if (Integer.parseInt(queryMap.get("state")) != state) {
                throw new IllegalArgumentException("state");
            }

            return new ElyAuthCode(queryMap.get(QUERY_CODE_KEY), redirect_uri, state);
        }

        boolean passURI(URI uri, String redirect_uri, int state) {
            log.debug("uri passed externally: {}", uri);

            try {
                parseURI(uri, redirect_uri, state);
            } catch (Exception e) {
                log.debug("invalid uri passed:", e);
                return false;
            }

            this.uri = uri;
            synchronized (wait) {
                wait.notifyAll();
            }

            return true;
        }

        ElyAuthCode waitForCode() throws InterruptedException {
            if (isAlive()) {
                join();
            }
            return code;
        }

        @Override
        public void run() {
            lockThread("start");
            while (true) {
                log.trace("iteration");
                URI currentURI = this.uri;
                while (currentURI == this.uri) {
                    log.trace("still same: {} vs {}", currentURI, this.uri);
                    synchronized (wait) {
                        log.trace("waiting");
                        try {
                            wait.wait();
                        } catch (InterruptedException interrupted) {
                            return;
                        }
                    }
                }

                log.debug("got new uri");
                currentURI = this.uri;

                try {
                    code = parseURI(currentURI, getRedirectUri(server.port), state);
                    log.debug("got the code {}", code);
                    return;
                } catch (Exception e) {
                    for (PrimaryElyAuthFlowListener listener : getListenerList()) {
                        Boolean result;
                        try {
                            result = PrimaryElyAuthFlow.this.join(listener.primaryStrategyCodeParseFailed(PrimaryElyAuthFlow.this, currentURI));
                        } catch (InterruptedException interrupted) {
                            return;
                        }
                        if (result != null && !result) {
                            // no more loops. interrupted.
                            Thread.interrupted();
                            return;
                        }
                    }
                    log.debug("one more loop", e);
                }
            }
        }
    }


    private static Map<String, String> splitQuery(String query) {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String[] pairs = StringUtils.split(query, '&');

        try {
            for (String pair : pairs) {
                int idx = StringUtils.indexOf(pair, '=');
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        } catch (UnsupportedEncodingException encoding) {
            throw new Error("UTF-8 is unsupported");
        }

        return query_pairs;
    }

    static final String SERVER_RESPONSE =
            "<!DOCTYPE html>" +
                    "<html><head><title>Legacy Launcher</title></head>" +
                    "<body>" +
                    "${text}" +
                    "</body>" +
                    "</html>";
}
