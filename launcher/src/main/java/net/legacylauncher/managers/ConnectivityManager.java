package net.legacylauncher.managers;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.repository.Repository;
import net.legacylauncher.ui.ConnectivityWarning;
import net.legacylauncher.ui.notification.Notification;
import net.legacylauncher.util.Lazy;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.AsyncThread;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ConnectivityManager {
    private static final String NOTIFICATION_ID = "connectivity";

    private final LegacyLauncher launcher;
    private final List<Entry> entries;
    private final AtomicBoolean showFailedNotification = new AtomicBoolean();
    private ConnectivityWarning warningWindow;

    public ConnectivityManager(LegacyLauncher launcher, List<Entry> entries) {
        this.launcher = launcher;
        this.entries = entries;
    }

    public static Entry checkByContent(String name, String url, String expectedContent) {
        return new Entry(name, U.parseHost(url), new HttpContentChecker(url, expectedContent));
    }

    public static Entry checkByValidJson(String name, String url) {
        return new Entry(name, U.parseHost(url), new JsonContentChecker(url));
    }

    public static Entry checkRepoByValidJson(String name, Repository repository, String path) {
        return new Entry(
                name,
                repository.getList().getRelevant().getList().stream().flatMap(l -> l.getHosts().stream()),
                new RepoEntryJsonChecker(repository, path)
        );
    }

    public static Entry forceFailed(String name) {
        return new Entry(name, Collections.emptyList(), () -> Boolean.FALSE);
    }

    public void queueChecks() {
        entries.stream()
                .filter(Entry::isNormalPriority)
                .forEach(this::queueCheck);
    }

    public void queueCheck(Entry entry) {
        CompletableFuture<?> future = entry.checkConnection();
        if (entry.isNormalPriority()) {
            // normal priority -> show or update notification
            future.whenCompleteAsync((r, e) -> notifyIfFailed(r != Boolean.TRUE || e != null));
        } else {
            // low priority -> only notify if there are failed entries with normal priority
            future.whenCompleteAsync((r, e) -> scheduleUpdateWarningWindow());
        }
    }

    private void queueLowPriorityChecks() {
        entries.stream()
                .filter(Entry::isLowPriority)
                .forEach(this::queueCheck);
    }

    public void showNotificationOnceIfNeeded() {
        if (entries.stream().filter(Entry::isDone).allMatch(e -> e.isReachable() || e.isLowPriority())) {
            return;
        }
        if (!showFailedNotification.compareAndSet(false, true)) {
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
        if (failed) {
            queueLowPriorityChecks();
            showNotificationOnceIfNeeded();
        }
        scheduleUpdateWarningWindow();
    }

    private void scheduleUpdateWarningWindow() {
        SwingUtil.later(this::updateWarningWindow);
    }

    private void showWarningWindow() {
        if (warningWindow == null) {
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
        if (warningWindow != null) {
            warningWindow.updateEntries(Collections.unmodifiableList(entries));
        }
    }

    public interface EntryChecker {
        Boolean checkConnection() throws Exception;
    }

    private static class RepoEntryJsonChecker implements EntryChecker {
        private final Repository repo;
        private final String path;

        public RepoEntryJsonChecker(Repository repo, String path) {
            this.repo = repo;
            this.path = path;
        }

        @Override
        public Boolean checkConnection() throws IOException {
            try {
                String content = IOUtils.toString(repo.read(path));
                try {
                    JsonParser.parseString(content);
                } catch (JsonSyntaxException e) {
                    throw new InvalidJsonException(content, e);
                }
            } catch (IOException e) {
                log.warn("Connectivity check to {} (using {}) failed", path, repo.name(), e);
                throw e;
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
                checkResponse(Request.get(url).execute());
            } catch (IOException e) {
                log.warn("Connectivity check to {} failed", url, e);
                throw new RuntimeException(e);
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
            if (!expectedContent.equals(actualContent)) {
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
            } catch (JsonSyntaxException e) {
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
        private final Set<String> hosts;
        private final EntryChecker checker;
        private final Lazy<CompletableFuture<Boolean>> future;
        private final Lazy<CompletableFuture<Boolean>> completion;
        private int priority;

        private Entry(String name, Collection<String> hosts, EntryChecker checker) {
            this.name = name;
            this.hosts = Collections.unmodifiableSet(new LinkedHashSet<>(hosts));
            this.checker = checker;
            this.completion = Lazy.of(CompletableFuture::new);
            this.future = Lazy.of(() -> {
                CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return checker.checkConnection();
                            } catch (RuntimeException e) {
                                throw e;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        },
                        AsyncThread.SHARED_SERVICE
                );
                f.whenCompleteAsync((r, e) -> {
                    CompletableFuture<Boolean> cf = completion.get();
                    if (e != null) {
                        cf.complete(Boolean.FALSE);
                    } else {
                        cf.complete(r);
                    }
                }, AsyncThread.SHARED_SERVICE);
                return f;
            });
        }

        private Entry(String name, Stream<String> hostStream, EntryChecker checker) {
            this(name, hostStream.collect(Collectors.toList()), checker);
        }

        Entry(String name, String host, EntryChecker checker) {
            this(name, Collections.singletonList(host), checker);
        }

        public String getName() {
            return name;
        }

        public Set<String> getHosts() {
            return hosts;
        }

        public EntryChecker getChecker() {
            return checker;
        }

        public boolean isQueued() {
            return future.isInitialized();
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

        public int getPriority() {
            return priority;
        }

        boolean isNormalPriority() {
            return !isLowPriority();
        }

        boolean isLowPriority() {
            return priority < 0;
        }

        public Entry withPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public CompletableFuture<Boolean> getTask() {
            return completion.get();
        }

        protected CompletableFuture<Boolean> checkConnection() {
            return future.get();
        }
    }

}
