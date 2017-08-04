package ru.turikhay.tlauncher.user;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.exceptions.IOExceptionList;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;
import ru.turikhay.util.git.MapTokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

public class PrimaryElyAuthFlow extends ElyAuthFlow<PrimaryElyAuthFlowListener> {
    private static final String SERVER_ADDRESS = "http://127.0.0.1:%d";
    private static final String SERVER_ENTRY_POINT = "/";
    static final String SERVER_FULL_URL = SERVER_ADDRESS + SERVER_ENTRY_POINT, QUERY_CODE_KEY = "code", QUERY_STATE_KEY = "state";
    private static final int PORT_CREATING_TRIES = 5, SERVER_BACKLOG = 1, SERVER_STOP_DELAY = 5;

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
        return String.format(SERVER_FULL_URL, port);
    }

    URIWatchdog createWatchdog(int state) {
        return new URIWatchdog(state);
    }

    HttpServerAdapter createServer(URIWatchdog watchdog) throws ElyAuthStrategyException, InterruptedException {
        log("Creating server...");

        int portCreatingTries = 0, port;
        ArrayList<IOException> ioEList = new ArrayList<IOException>();
        do {
            checkCancelled();

            ++portCreatingTries;
            port = U.random(49152, 65535);

            log("try:", portCreatingTries, "; selected port:", port);

            HttpServerAdapter adapter;
            try {
                adapter = new HttpServerAdapter(watchdog, port, watchdog.getCheckState());
            } catch (IOException ioE) {
                ioEList.add(ioE);
                continue;
            } catch (ClassNotFoundException classNotFound) {
                throw new ElyAuthStrategyException("Incompatible JRE/JDK - Oracle JRE required", classNotFound, "incompatible");
            } catch (Exception e) {
                throw new ElyAuthStrategyException("Unknown error", e, "unknown");
            }

            for(PrimaryElyAuthFlowListener listener : getListenerList()) {
                listener.primaryStrategyServerCreated(this, port);
            }

            return adapter;
        } while (portCreatingTries < PORT_CREATING_TRIES);

        throw new ElyAuthStrategyException("Max port occupy tries exceed", new IOExceptionList(ioEList), "max-tries");
    }

    void openBrowser(int port, int state) throws ElyAuthStrategyException, InterruptedException {
        openBrowser(getRedirectUri(port), state);
    }

    class HttpServerAdapter {
        private final URIWatchdog watchdog;
        private final HttpServer server;
        private final int port, state;

        private HttpServerAdapter(URIWatchdog watchdog, int port, int state) throws ClassNotFoundException, IOException {
            log("Creating HttpServerAdapter at port", port);

            this.watchdog = U.requireNotNull(watchdog, "watchdog");
            this.port = port;
            this.state = state;

            server = HttpServer.create(new InetSocketAddress(port), SERVER_BACKLOG);
            server.createContext(SERVER_ENTRY_POINT, new HttpHandler() {
                @Override
                public void handle(HttpExchange httpExchange) throws IOException {
                    try {
                        log("handling uri:", httpExchange.getRequestURI());
                        int responseStatus;
                        String response;

                        if (HttpServerAdapter.this.watchdog.passURI(httpExchange.getRequestURI(), getRedirectUri(HttpServerAdapter.this.port), HttpServerAdapter.this.state)) {
                            responseStatus = HttpURLConnection.HTTP_OK;
                            response = TokenReplacingReader.resolveVars(SERVER_RESPONSE, new MapTokenResolver(new HashMap<String, String>() {
                                { put("text", Localizable.get("account.manager.multipane.process-account-ely.flow.complete.response")); }
                            }));
                        } else {
                            responseStatus = HttpURLConnection.HTTP_BAD_REQUEST;
                            response = "\r\n";
                        }

                        httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=" + FileUtil.getCharset().name());
                        if(responseStatus == HttpURLConnection.HTTP_MOVED_TEMP) {


                            // TODO redirect to the special page when it's ready
                            httpExchange.getResponseHeaders().set("Location", ElyAuth.ACCOUNT_BASE);
                        }
                        httpExchange.sendResponseHeaders(responseStatus, response.getBytes(FileUtil.getCharset()).length);

                        OutputStream out = httpExchange.getResponseBody();
                        IOUtils.write(response, out, FileUtil.getCharset());
                        out.close();
                    } catch (Exception e) {
                        U.log("Interrupting watchdog because of Server failure:", e);
                        HttpServerAdapter.this.watchdog.interrupt();
                    }
                }
            });
            server.setExecutor(null);

            log("HttpServer created successfully");
        }

        int getPort() {
            return port;
        }

        int getState() {
            return state;
        }

        void start() {
            log("Http server started at port", port);
            server.start();
        }

        void stop() {
            log("Http server stopped at port", port);
            server.stop(SERVER_STOP_DELAY);
        }

        private void log(Object... o) {
            PrimaryElyAuthFlow.this.log("[HttpServer]", o);
        }
    }

    class URIWatchdog extends ExtendedThread {
        private final Object wait = new Object();
        private final int state;

        private volatile URI uri;
        private ElyAuthCode code;

        private URIWatchdog( int state) {
            this.state = state;

            log("Starting watchdog...");
            startAndWait();
            unlockThread("start");
            log("Started");
        }

        int getCheckState() {
            return state;
        }

        ElyAuthCode parseURI(URI uri, String redirect_uri, int state) {
            String query = uri.toString().substring(SERVER_ENTRY_POINT.length() + 1 /* including "?" */);
            Map<String, String> queryMap = splitQuery(query);

            if(Integer.parseInt(queryMap.get("state")) != state) {
                throw new IllegalArgumentException("state");
            }

            return new ElyAuthCode(queryMap.get(QUERY_CODE_KEY), redirect_uri, state);
        }

        boolean passURI(URI uri, String redirect_uri, int state) {
            log("uri passed externally:", uri);

            try {
                parseURI(uri, redirect_uri, state);
            } catch (Exception e) {
                log("invalid uri passed:", e);
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
                log("iteration");
                URI currentURI = this.uri;
                while (currentURI == this.uri) {
                    log("still same:", currentURI, this.uri);
                    synchronized (wait) {
                        log("waiting");
                        try {
                            wait.wait();
                        } catch (InterruptedException interrupted) {
                            return;
                        }
                    }
                }

                log("got new uri");
                currentURI = this.uri;

                try {
                    code = parseURI(currentURI, getRedirectUri(server.port), state);
                    log("got the code", code);
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
                    log("one more loop", e);
                }
            }
        }

        private void log(Object... o) {
            PrimaryElyAuthFlow.this.log("[URIWatchdog]", o);
        }
    }


    private static Map<String, String> splitQuery(String query) {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
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
                    "<html><head><title>TLauncher</title></head>" +
                    "<body>" +
                    "${text}" +
                    "</body>" +
                    "</html>";
}
