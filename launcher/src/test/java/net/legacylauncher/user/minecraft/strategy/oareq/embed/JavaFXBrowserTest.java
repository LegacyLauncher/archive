package net.legacylauncher.user.minecraft.strategy.oareq.embed;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class JavaFXBrowserTest {
    @Test
    @Disabled
    void test() throws InterruptedException, MalformedURLException, TimeoutException {
        JavaFXBrowser browser = new JavaFXBrowser(true);
        browser.initAndShow(new BrowserConfiguration("Пожалуйста, аутентифицируйтесь", Collections.emptyList()), new URL("https://google.com"), url -> log.debug("New URL: {}", url));
        browser.waitForClose(60, TimeUnit.MINUTES);
    }

}