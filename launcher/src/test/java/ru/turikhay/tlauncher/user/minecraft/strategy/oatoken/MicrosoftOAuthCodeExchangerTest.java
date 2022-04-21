package ru.turikhay.tlauncher.user.minecraft.strategy.oatoken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.turikhay.tlauncher.user.minecraft.oauth.OAuthApplication;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.embed.BrowserConfiguration;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.embed.EmbeddedBrowserStrategy;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.embed.EmbeddedBrowserUrlProducer;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.embed.JavaFXBrowser;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.exchange.MicrosoftOAuthCodeExchangeException;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.exchange.MicrosoftOAuthCodeExchanger;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

public class MicrosoftOAuthCodeExchangerTest {
    private static final Logger LOGGER = LogManager.getLogger(MicrosoftOAuthCodeExchangerTest.class);

    @Test
    @Disabled
    void test() throws InterruptedException, MicrosoftOAuthCodeRequestException, MicrosoftOAuthCodeExchangeException, IOException, TimeoutException {
        EmbeddedBrowserStrategy embeddedBrowserStrategy = new EmbeddedBrowserStrategy(
                new JavaFXBrowser(true),
                new EmbeddedBrowserUrlProducer(),
                new BrowserConfiguration("", Collections.emptyList()),
                new OAuthUrlParser()
        );
        MicrosoftOAuthExchangeCode microsoftOAuthExchangeCode =
                embeddedBrowserStrategy.requestMicrosoftOAuthCode();
        MicrosoftOAuthCodeExchanger exchangeStrategy =
                new MicrosoftOAuthCodeExchanger(OAuthApplication.OFFICIAL_LAUNCHER);
        MicrosoftOAuthToken microsoftOAuthToken =
                exchangeStrategy.exchangeMicrosoftOAuthCode(microsoftOAuthExchangeCode);
        LOGGER.info(microsoftOAuthToken);
    }

}