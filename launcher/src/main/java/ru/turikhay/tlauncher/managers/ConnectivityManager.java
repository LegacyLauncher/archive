package ru.turikhay.tlauncher.managers;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.ui.ConnectivityWarning;
import ru.turikhay.tlauncher.ui.notification.Notification;
import ru.turikhay.util.Lazy;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectivityManager {
    private static final Logger LOGGER = LogManager.getLogger(ConnectivityManager.class);
    private static final String NOTIFICATION_ID = "connectivity";

    private final TLauncher launcher;
    private final List<Entry> entries;

    public ConnectivityManager(TLauncher launcher, List<Entry> entries) {
        this.launcher = launcher;
        this.entries = entries;
    }

    public void queueChecks() {
        entries.stream()
            .map(Entry::checkConnection)
            .forEach(f -> f.whenCompleteAsync((r, e) ->
                    notifyIfFailed(r != Boolean.TRUE || e != null)
            ));
    }

    private final AtomicBoolean showFailedNotification = new AtomicBoolean();

    public void showNotificationOnceIfNeeded() {
        if(entries.stream().filter(Entry::isDone).allMatch(e -> e.isReachable() || e.getPriority() < 0)) {
            return;
        }
        if(!showFailedNotification.compareAndSet(false, true)) {
            return;
        }
        launcher.executeWhenReady(() -> SwingUtil.later(() ->
            launcher.getFrame().mp.defaultScene.notificationPanel.addNotification(
                    NOTIFICATION_ID,
                    new Notification("warning", this::showWarningWindow)
            )
        ));
    }

    private void notifyIfFailed(boolean failed) {
        if(failed) {
            showNotificationOnceIfNeeded();
        }
        SwingUtil.later(this::updateWarningWindow);
    }

    private ConnectivityWarning warningWindow;

    private void showWarningWindow() {
        if(warningWindow == null) {
            warningWindow = new ConnectivityWarning();
            warningWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    warningWindow = null;
                }
            });
            updateWarningWindow();
            warningWindow.pack();
            warningWindow.showAtCenter();
        } else {
            warningWindow.requestFocus();
        }
        launcher.getFrame().mp.defaultScene.notificationPanel.removeNotification(NOTIFICATION_ID);
    }

    private void updateWarningWindow() {
        if(warningWindow != null) {
            warningWindow.updateEntries(Collections.unmodifiableList(entries));
        }
    }

    private interface EntryChecker {
        Boolean checkConnection();
    }

    private static class RepoEntryJsonChecker implements EntryChecker {
        private final Repository repo;
        private final String path;

        public RepoEntryJsonChecker(Repository repo, String path) {
            this.repo = repo;
            this.path = path;
        }

        @Override
        public Boolean checkConnection() {
            try {
                String content = IOUtils.toString(repo.read(path));
                try {
                    JsonParser.parseString(content);
                } catch (JsonSyntaxException e) {
                    throw new InvalidJsonException(content, e);
                }
            } catch (IOException e) {
                LOGGER.warn("Connectivity check to {} (using {}) failed", path, repo.name(), e);
                U.throwChecked(e);
            }
            return Boolean.TRUE;
        }
    }

    private static abstract class AbstractHttpGetEntryChecker implements EntryChecker {
        private final String url;

        protected AbstractHttpGetEntryChecker(String url) {
            this.url = url;
        }

        @Override
        public final Boolean checkConnection() {
            try {
                checkResponse(Request.Get(url).execute());
            } catch(IOException e) {
                LOGGER.warn("Connectivity check to {} failed", url, e);
                U.throwChecked(e);
            }
            return Boolean.TRUE;
        }

        protected abstract void checkResponse(Response response) throws IOException;
    }

    private static class HttpContentChecker extends AbstractHttpGetEntryChecker {
        private final String expectedContent;

        protected HttpContentChecker(String url, String expectedContent) {
            super(url);
            this.expectedContent = Objects.requireNonNull(expectedContent, "expectedContent");
        }

        @Override
        protected void checkResponse(Response response) throws IOException {
            String actualContent = response.returnContent().asString();
            if(!expectedContent.equals(actualContent)) {
                throw new ContentMismatchException(expectedContent, actualContent);
            }
        }

        private static class ContentMismatchException extends IOException {
            public ContentMismatchException(String expected, String actual) {
                super(
                        String.format(Locale.ROOT, "expected: \"%s\", actual: \"%s\"",
                                expected, actual)
                );
            }
        }
    }

    private static class JsonContentChecker extends AbstractHttpGetEntryChecker {
        private JsonContentChecker(String url) {
            super(url);
        }

        @Override
        protected void checkResponse(Response response) throws IOException {
            String content = response.returnContent().asString();
            try {
                JsonParser.parseString(content);
            } catch(JsonSyntaxException e) {
                throw new InvalidJsonException(content, e);
            }
        }
    }

    private static class InvalidJsonException extends IOException {
        public InvalidJsonException(String content, Throwable cause) {
            super("invalid json: " + content, cause);
        }
    }

    public static class Entry {
        private final String name;
        private final Lazy<CompletableFuture<Boolean>> future;

        private Entry(String name, EntryChecker checker) {
            this.name = name;
            this.future = Lazy.of(() ->
                    CompletableFuture.supplyAsync(
                            checker::checkConnection,
                            AsyncThread.SHARED_SERVICE
                    )
            );
        }

        public String getName() {
            return name;
        }

        public boolean isDone() {
            return future.valueIfInitialized().map(CompletableFuture::isDone).orElse(false);
        }

        public boolean isReachable() {
            return future.valueIfInitialized()
                    .filter(f -> !f.isCompletedExceptionally())
                    .map(f -> f.getNow(Boolean.FALSE))
                    .orElse(Boolean.FALSE);
        }

        private int priority;

        public int getPriority() {
            return priority;
        }

        public Entry withPriority(int priority) {
            this.priority = priority;
            return this;
        }

        protected CompletableFuture<?> checkConnection() {
            return future.get();
        }
    }

    public static Entry checkByContent(String name, String url, String expectedContent) {
        return new Entry(name, new HttpContentChecker(url, expectedContent));
    }

    public static Entry checkByValidJson(String name, String url) {
        return new Entry(name, new JsonContentChecker(url));
    }

    public static Entry checkRepoByValidJson(String name, Repository repository, String path) {
        return new Entry(name, new RepoEntryJsonChecker(repository, path));
    }

    public static Entry forceFailed(String name) {
        return new Entry(name, () -> Boolean.FALSE);
    }

}
