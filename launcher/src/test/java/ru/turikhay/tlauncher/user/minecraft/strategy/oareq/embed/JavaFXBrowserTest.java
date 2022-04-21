package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.embed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JavaFXBrowserTest {
    private static final Logger LOGGER = LogManager.getLogger(JavaFXBrowserTest.class);

    @Test
    @Disabled
    void test() throws InterruptedException, MalformedURLException, TimeoutException {
        JavaFXBrowser browser = new JavaFXBrowser(true);
        browser.initAndShow(new BrowserConfiguration("Пожалуйста, аутентифицируйтесь", Collections.emptyList()), new URL("https://google.com"), url -> LOGGER.debug("New URL: {}", url));
        browser.waitForClose(60, TimeUnit.MINUTES);
    }

}