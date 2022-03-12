package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv.nanohttpd.NanoHttpdLocalServer;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LocalServerStrategyTest {

    @Test
    @Disabled
    void testPortAcquisition() {
        LocalServerUrlProducer urlProducer = new LocalServerUrlProducer();
        LocalServerStrategy strategy = new LocalServerStrategy(
                new ExternalBrowser() {
                    @Override
                    public void openUrl(String url) {
                        // no-op
                    }
                },
                urlProducer,
                new NanoHttpdLocalServer(
                        new OAuthUrlParser(),
                        urlProducer
                ),
                new LocalServerConfiguration(
                        "localhost",
                        // these ports must be in use
                        Arrays.asList(49672, 65001),
                        "",
                        null
                )
        );
        MicrosoftOAuthCodeRequestException e = assertThrows(
                MicrosoftOAuthCodeRequestException.class,
                () -> strategy.requestMicrosoftOAuthCode(5, TimeUnit.SECONDS)
        );
        assertEquals("cannot start local server", e.getMessage());
    }

}