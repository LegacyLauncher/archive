package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.embed;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JavaFXBrowser implements EmbeddedBrowser {
    private static final int WIDTH = 600, HEIGHT = 600;

    private final CountDownLatch closeLatch = new CountDownLatch(1);
    private final boolean explicitExit;

    private BrowserConfiguration configuration;
    private URLCallback urlCallback;

    private JFrame window;
    private JFXPanel panel;
    private WebView webView;

    public JavaFXBrowser(boolean explicitExit) {
        this.explicitExit = explicitExit;
    }

    public JavaFXBrowser() {
        this(false);
    }

    private void init(URL url) {
        initFrame();
        initJFX(url);
        packAndShow();
    }

    private void initFrame() {
        this.window = new JFrame();
        window.setTitle(configuration.getTitle());
        window.setIconImages(configuration.getFavicons());
        window.setResizable(false);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                destroy();
            }
        });
    }

    private void initJFX(URL url) {
        if (explicitExit) {
            // Prevent JavaFX from stopping when browser is closed
            // This will ensure another browser can be opened after
            Platform.setImplicitExit(false);
        }
        this.panel = new JFXPanel();
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        Platform.runLater(() -> initWebView(url));
        window.setContentPane(panel);
    }

    private void packAndShow() {
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private void initWebView(URL url) {
        if (configuration.isClearCookies()) {
            CookieHandler.setDefault(new CookieManager());
        }
        this.webView = new WebView();
        webView.setPrefSize(WIDTH, HEIGHT);
        webView.getEngine().load(url.toString());
        webView.getEngine().getHistory().getEntries().addListener((ListChangeListener<WebHistory.Entry>) c -> {
            if (c.next() && c.wasAdded()) {
                for (WebHistory.Entry entry : c.getAddedSubList()) {
                    urlCallback.navigatedUrl(entry.getUrl());
                }
            }
        });
        Group group = new Group(webView);
        panel.setScene(new Scene(group));
    }

    private void destroy() {
        Platform.runLater(() -> {
            if (webView == null || panel == null) {
                return;
            }
            webView.getEngine().getLoadWorker().cancel();
            webView = null;
            panel.setScene(null);
            panel = null;
            SwingUtilities.invokeLater(() -> {
                if (window == null) {
                    return;
                }
                window.dispose();
                window = null;
            });
        });
        closeLatch.countDown();
    }

    @Override
    public void initAndShow(BrowserConfiguration configuration, URL url, URLCallback callback) {
        this.configuration = configuration;
        this.urlCallback = callback;
        SwingUtilities.invokeLater(() -> init(url));
    }

    @Override
    public void close() {
        this.destroy();
    }

    @Override
    public void waitForClose(long time, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        if (!closeLatch.await(time, timeUnit)) {
            throw new TimeoutException();
        }
    }

    public static void checkAvailable() {
        WebView.class.toString();
    }
}
