package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv.nanohttpd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.RedirectUrl;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv.*;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class NanoHttpdLocalServer implements ILocalServer {
    private static final Logger LOGGER = LogManager.getLogger(NanoHttpdLocalServer.class);

    private final AtomicBoolean invoked = new AtomicBoolean(false);

    private final OAuthUrlParser urlParser;
    private final LocalServerUrlProducer urlProducer;

    private LockExchange lockExchange;
    private NanoHttpdAdapter adapter;

    public NanoHttpdLocalServer(OAuthUrlParser urlParser, LocalServerUrlProducer urlProducer) {
        this.urlParser = urlParser;
        this.urlProducer = urlProducer;
    }

    LocalServerSelectedConfiguration selectedConfiguration;

    @Override
    public LocalServerSelectedConfiguration start(LocalServerConfiguration configuration)
            throws LocalServerException {
        if (!invoked.compareAndSet(false, true)) {
            return selectedConfiguration;
        }

        LockExchange lockExchange = null;
        NanoHttpdAdapter adapter = null;
        List<IOException> serverStartExceptions = new ArrayList<>();

        for (int port : configuration.getAllowedPorts()) {
            LOGGER.debug("Starting server on {}:{}", configuration.getHost(), port);

            selectedConfiguration = new LocalServerSelectedConfiguration(
                    configuration.getHost(),
                    port,
                    configuration.getPath(),
                    generateState()
            );

            RedirectUrl redirectUrl;
            try {
                redirectUrl = new RedirectUrl(urlProducer.buildRedirectUrl(selectedConfiguration));
            } catch (URISyntaxException | MalformedURLException e) {
                throw new LocalServerException("cannot build redirect uri", e);
            }

            lockExchange = new LockExchange();
            adapter = new NanoHttpdAdapter(
                    selectedConfiguration,
                    lockExchange,
                    urlParser,
                    redirectUrl,
                    configuration.getRedirectOnSuccess()
            );

            try {
                adapter.start();
            } catch (IOException e) {
                LOGGER.warn("Couldn't start local server on {}:{}",
                        configuration.getHost(), port, e);
                serverStartExceptions.add(e);
                adapter = null;
                continue; // not ok
            }

            break; // ok
        }
        if (adapter == null) {
            LocalServerException e = new LocalServerException("every allowed port cannot be bound to");
            serverStartExceptions.forEach(e::addSuppressed);
            throw e;
        } else {
            this.adapter = adapter;
            this.lockExchange = lockExchange;
            return selectedConfiguration;
        }
    }

    private static String generateState() {
        return String.valueOf(new Random().nextLong());
    }

    @Override
    public MicrosoftOAuthExchangeCode waitForCode(long time, TimeUnit timeUnit)
            throws MicrosoftOAuthCodeRequestException, TimeoutException, InterruptedException {
        if (!invoked.get()) {
            throw new IllegalStateException("server has not been started");
        }
        return lockExchange.waitForCode(time, timeUnit);
    }

    @Override
    public void stop() {
        if (!invoked.compareAndSet(true, false)) {
            throw new IllegalStateException("server has not been started");
        }
        AsyncThread.execute(() -> {
            U.sleepFor(2000);
            adapter.stop();
        });
    }

}
