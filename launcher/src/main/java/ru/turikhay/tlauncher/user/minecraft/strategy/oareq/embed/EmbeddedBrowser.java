package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.embed;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface EmbeddedBrowser {
    void initAndShow(BrowserConfiguration configuration, URL url, URLCallback callback);

    void close();

    void waitForClose(long time, TimeUnit timeUnit) throws InterruptedException, TimeoutException;
}
