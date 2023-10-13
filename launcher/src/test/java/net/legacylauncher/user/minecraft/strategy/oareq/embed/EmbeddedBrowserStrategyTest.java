package net.legacylauncher.user.minecraft.strategy.oareq.embed;

import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import net.legacylauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.TimeoutException;

public class EmbeddedBrowserStrategyTest {
    private static final Logger LOGGER = LogManager.getLogger(EmbeddedBrowserStrategyTest.class);

    @Test
    @Disabled
    void test() throws InterruptedException, MicrosoftOAuthCodeRequestException, TimeoutException {
        EmbeddedBrowserStrategy strategy = new EmbeddedBrowserStrategy(
                new JavaFXBrowser(true),
                new EmbeddedBrowserUrlProducer(),
                new BrowserConfiguration("Auth", Collections.emptyList()),
                new OAuthUrlParser()
        );
        MicrosoftOAuthExchangeCode microsoftOAuthExchangeCode = strategy.requestMicrosoftOAuthCode();
        LOGGER.info("Code: {}", microsoftOAuthExchangeCode);
    }

}