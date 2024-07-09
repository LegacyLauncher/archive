package net.legacylauncher.user.minecraft.strategy.oareq.embed;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import net.legacylauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.TimeoutException;

@Slf4j
public class EmbeddedBrowserStrategyTest {
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
        log.info("Code: {}", microsoftOAuthExchangeCode);
    }

}