package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestStrategy;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LocalServerStrategy implements MicrosoftOAuthCodeRequestStrategy {
    private static final Logger LOGGER = LogManager.getLogger(LocalServerStrategy.class);

    private final ExternalBrowser externalBrowser;
    private final LocalServerUrlProducer localServerUrlProducer;
    private final ILocalServer localServer;
    private final LocalServerConfiguration serverConfiguration;

    public LocalServerStrategy(ExternalBrowser externalBrowser,
                               LocalServerUrlProducer localServerUrlProducer,
                               ILocalServer localServer,
                               LocalServerConfiguration serverConfiguration) {
        this.externalBrowser = externalBrowser;
        this.localServerUrlProducer = localServerUrlProducer;
        this.localServer = localServer;
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    public MicrosoftOAuthExchangeCode requestMicrosoftOAuthCode(long time, TimeUnit timeUnit)
            throws MicrosoftOAuthCodeRequestException, InterruptedException, TimeoutException {
        LOGGER.trace("Starting local server");
        LocalServerSelectedConfiguration selectedConfiguration = startServer();
        this.serverConfiguration.setSelectedConfiguration(selectedConfiguration);
        try {
            openExternalBrowser(selectedConfiguration);
            return waitForCode(time, timeUnit);
        } finally {
            localServer.stop();
        }
    }

    public LocalServerSelectedConfiguration startServer() throws MicrosoftOAuthCodeRequestException {
        try {
            return localServer.start(serverConfiguration);
        } catch (LocalServerException e) {
            throw new MicrosoftOAuthCodeRequestException("cannot start local server", e);
        }
    }

    private void openExternalBrowser(LocalServerSelectedConfiguration selectedConfiguration)
            throws MicrosoftOAuthCodeRequestException {
        String loginPageUrl;
        try {
            loginPageUrl = localServerUrlProducer.buildLoginUrl(selectedConfiguration);
        } catch (URISyntaxException | MalformedURLException e) {
            throw new MicrosoftOAuthCodeRequestException("invalid login page url", e);
        }
        LOGGER.debug("Opening login page: {}", loginPageUrl);
        externalBrowser.openUrl(loginPageUrl);
    }

    private MicrosoftOAuthExchangeCode waitForCode(long time, TimeUnit timeUnit)
            throws MicrosoftOAuthCodeRequestException, InterruptedException, TimeoutException {
        return localServer.waitForCode(time, timeUnit);
    }
}
