package ru.turikhay.tlauncher.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.async.AsyncThread;

import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public abstract class ElyAuthFlow<L extends ElyAuthFlowListener> implements Callable<ElyAuthCode> {
    private static final Logger LOGGER = LogManager.getLogger(ElyAuthFlow.class);

    // client_id=tlauncher&response_type=code&scope=account_info+minecraft_server_session&redirect_uri=http://localhost:80
    static final String OAUTH2_BASE = ElyAuth.ACCOUNT_BASE + "/oauth2/v1";
    static final String OAUTH2_AUTH_REQUEST = OAUTH2_BASE +
            "?client_id=" + ElyAuth.CLIENT_ID + "&response_type=code&scope=account_info+minecraft_server_session&redirect_uri=%s&state=%d&prompt=select_account";


    private final List<L> listenerList = new ArrayList<>(), listenerList_ = Collections.unmodifiableList(listenerList);
    private final Object sync = new Object();

    private PrimaryElyAuthFlowBrowser browser = new PrimaryElyAuthFlowDefaultBrowser();
    private boolean started;

    ElyAuthFlow() {
    }

    @Override
    public ElyAuthCode call() throws Exception {
        ElyAuthCode code;

        synchronized (sync) {
            started = true;

            for (L listener : listenerList) {
                listener.strategyStarted(this);
            }
        }
        try {
            code = Objects.requireNonNull(fetchCode(), "code");
            checkCancelled();
        } catch (InterruptedException interrupted) {
            onCancelled();
            for (L listener : listenerList) {
                listener.strategyCancelled(this);
            }
            throw interrupted;
        } catch (Exception e) {
            LOGGER.error("Error fetching code", e);
            for (L listener : listenerList) {
                listener.strategyErrored(this, e);
            }
            throw e;
        }

        for (L listener : listenerList) {
            listener.strategyComplete(this, code);
        }

        return code;
    }

    protected final void openBrowser(String redirect_uri, int state) throws InterruptedException {
        checkCancelled();

        URL url;
        try {
            url = new URL(String.format(java.util.Locale.ROOT, OAUTH2_AUTH_REQUEST, URLEncoder.encode(redirect_uri, FileUtil.getCharset().name()), state));
        } catch (Exception e) {
            throw new Error(e);
        }

        if (browser.openLink(url)) {
            for (ElyAuthFlowListener listener : getListenerList()) {
                listener.strategyUrlOpened(this, url);
            }
        } else {
            for (ElyAuthFlowListener listener : getListenerList()) {
                listener.strategyUrlOpeningFailed(this, url);
            }
        }
    }

    protected abstract ElyAuthCode fetchCode() throws ElyAuthStrategyException, InterruptedException;

    protected abstract void onCancelled();

    public void registerBrowser(PrimaryElyAuthFlowBrowser browser) {
        checkStarted();
        this.browser = Objects.requireNonNull(browser, "browser");
    }

    public void registerListener(L listener) {
        Objects.requireNonNull(listener, "listener");
        synchronized (sync) {
            checkStarted();
            listenerList.add(listener);
        }
    }

    private void checkStarted() {
        synchronized (sync) {
            if (started) {
                throw new IllegalStateException("started");
            }
        }
    }

    protected void checkCancelled() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    protected final <V> V join(ElyFlowWaitTask<V> task) throws InterruptedException {
        if (task == null) {
            return null;
        }
        try {
            return AsyncThread.future(task).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected final int generateState() {
        return new SecureRandom().nextInt();
    }

    protected final List<L> getListenerList() {
        return listenerList_;
    }
}
