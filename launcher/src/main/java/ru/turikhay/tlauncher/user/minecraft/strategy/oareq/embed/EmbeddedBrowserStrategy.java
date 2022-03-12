package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.embed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.exceptions.ParseException;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EmbeddedBrowserStrategy implements MicrosoftOAuthCodeRequestStrategy {
    private static final Logger LOGGER = LogManager.getLogger(EmbeddedBrowserStrategy.class);

    private final ExecutorService urlThread = Executors.newSingleThreadExecutor();
    private final EmbeddedBrowser browser;
    private final EmbeddedBrowserUrlProducer urlProducer;
    private final BrowserConfiguration configuration;
    private final OAuthUrlParser parser;

    public EmbeddedBrowserStrategy(EmbeddedBrowser browser,
                                   EmbeddedBrowserUrlProducer urlProducer,
                                   BrowserConfiguration configuration,
                                   OAuthUrlParser parser) {
        this.browser = browser;
        this.urlProducer = urlProducer;
        this.configuration = configuration;
        this.parser = parser;
    }

    private MicrosoftOAuthExchangeCode code;
    private MicrosoftOAuthCodeRequestException exception;

    @Override
    public MicrosoftOAuthExchangeCode requestMicrosoftOAuthCode(long time, TimeUnit timeUnit)
            throws MicrosoftOAuthCodeRequestException, InterruptedException, TimeoutException {
        URL url;
        try {
            url = urlProducer.buildLoginUrl();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new MicrosoftOAuthCodeRequestException("bad initial uri syntax", e);
        }
        browser.initAndShow(this.configuration, url, this::deferUrlNavigated);
        try {
            browser.waitForClose(time, timeUnit);
        } finally {
            browser.close();
        }
        if (exception != null) {
            throw new MicrosoftOAuthCodeRequestException("url parsed an error", exception);
        }
        if (code == null) {
            throw new CodeRequestCancelledException("browser closed and no code received");
        }
        return code;
    }

    private void deferUrlNavigated(String url) {
        urlThread.submit(() -> urlNavigated(url));
    }

    private void urlNavigated(String url) {
        LOGGER.trace("Navigated: {}", url);
        String code = null;
        try {
            code = parser.parseAndValidate(url);
        } catch (ParseException e) {
            LOGGER.trace("Couldn't parse: {}", e.toString());
            return;
        } catch (MicrosoftOAuthCodeRequestException e) {
            LOGGER.warn("Parser returned: {}", e.toString());
            this.exception = e;
        }
        this.code = code == null ? null :
                new MicrosoftOAuthExchangeCode(code, urlProducer.getRedirectUrl());
        browser.close();
    }

    public static boolean isJavaFXWebViewSupported() {
        try {
            JavaFXBrowser.checkAvailable();
        } catch (Error e) {
            LOGGER.info("JavaFX Browser not available: {}", e.toString());
            return false;
        }
        return true;
    }
}
