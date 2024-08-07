package net.legacylauncher.user.minecraft.strategy.oareq.embed;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.exceptions.ParseException;
import net.legacylauncher.user.minecraft.strategy.oareq.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class EmbeddedBrowserStrategy implements MicrosoftOAuthCodeRequestStrategy {
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
        log.trace("Navigated: {}", url);
        String code = null;
        try {
            code = parser.parseAndValidate(url);
        } catch (ParseException e) {
            log.trace("Couldn't parse: {}", e.toString());
            return;
        } catch (MicrosoftOAuthCodeRequestException e) {
            log.warn("Parser returned: {}", e.toString());
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
            log.info("JavaFX Browser not available: {}", e.toString());
            return false;
        }
        return true;
    }
}
