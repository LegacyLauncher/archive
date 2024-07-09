package net.legacylauncher.user.minecraft.strategy.oatoken;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import net.legacylauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import net.legacylauncher.user.minecraft.strategy.oareq.embed.BrowserConfiguration;
import net.legacylauncher.user.minecraft.strategy.oareq.embed.EmbeddedBrowserStrategy;
import net.legacylauncher.user.minecraft.strategy.oareq.embed.EmbeddedBrowserUrlProducer;
import net.legacylauncher.user.minecraft.strategy.oareq.embed.JavaFXBrowser;
import net.legacylauncher.user.minecraft.strategy.oatoken.exchange.MicrosoftOAuthCodeExchangeException;
import net.legacylauncher.user.minecraft.strategy.oatoken.exchange.MicrosoftOAuthCodeExchanger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

@Slf4j
public class MicrosoftOAuthCodeExchangerTest {

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
        log.info("{}", microsoftOAuthToken);
    }

}