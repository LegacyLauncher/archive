package net.legacylauncher.user.minecraft.strategy.oareq.lcserv.nanohttpd;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import net.legacylauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import net.legacylauncher.user.minecraft.strategy.oareq.lcserv.*;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.AsyncThread;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NanoHttpdLocalServer implements ILocalServer {
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
            log.debug("Starting server on {}:{}", configuration.getHost(), port);

            selectedConfiguration = new LocalServerSelectedConfiguration(
                    configuration.getHost(),
                    port,
                    configuration.getPath(),
                    generateState()
            );

            URI redirectUrl;
            try {
                redirectUrl = new URI(urlProducer.buildRedirectUrl(selectedConfiguration));
            } catch (URISyntaxException e) {
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
                log.warn("Couldn't start local server on {}:{}",
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
