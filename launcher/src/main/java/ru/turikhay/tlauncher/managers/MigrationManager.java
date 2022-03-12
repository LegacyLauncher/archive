package ru.turikhay.tlauncher.managers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.apache.commons.lang3.Validate;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;
import ru.turikhay.tlauncher.ui.MigrationFrame;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.notification.Notification;
import ru.turikhay.tlauncher.user.AuthlibUser;
import ru.turikhay.tlauncher.user.MojangUser;
import ru.turikhay.tlauncher.user.MojangUserMigrationStatus;
import ru.turikhay.util.Lazy;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MigrationManager {
    private static final Logger LOGGER = LogManager.getLogger(MigrationManager.class);

    // thread pool with for only one thread and one concurrent task
    private final ExecutorService service = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1),
            new ThreadFactoryBuilder()
                    .setNameFormat("MigrationManager-%d")
                    .setUncaughtExceptionHandler(ExceptionHandler.getInstance())
                    .build(),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    private final TLauncher l;
    public final Lazy<MigrationManifest> manifest = Lazy.of(this::fetchMigrationManifest);

    // file with UUID = migrationStatus
    private final File migrationStatusFile = new File(
            MinecraftUtil.getSystemRelatedDirectory("tlauncher", true),
            "mojang-migration-status.properties"
    );

    private Set<MojangUser> possiblyEligibleForMigration = Collections.emptySet();
    private MigrationFrame frame;
    private boolean firstTime;

    public MigrationManager(TLauncher l) {
        this.l = l;
        l.getProfileManager().getAccountManager().addListener(set -> queueMigrationCheck());
    }

    public void queueMigrationCheck() {
        service.submit(this::performMigrationCheck);
    }

    private void performMigrationCheck() {
        this.firstTime = !migrationStatusFile.isFile();

        LOGGER.debug("Performing migration check");
        MigrationManifest manifest;
        try {
            manifest = this.manifest.call();
        } catch (Exception e) {
            LOGGER.debug("No manifest -> no migration check");
            return;
        }

        Set<MojangUser> users =
                l.getProfileManager().getAccountManager().getUserSet().getSet().stream()
                        .filter(u -> u instanceof MojangUser)
                        .map(u -> (MojangUser) u)
                        .sorted(Comparator.comparing(AuthlibUser::getUsername))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        if (users.isEmpty()) {
            LOGGER.debug("We don't have any Mojang users -> skipping notification");
            return;
        }

        this.possiblyEligibleForMigration = Collections.unmodifiableSet(users);
        this.updateMigrationFrameIfOpened();

        // we have no forced migration date -> decide whether to show notification or not
        if (!manifest.data.hasForcedMigrationDate()) {
            Properties statuses = readMigrationStatuses();
            Stream<MojangUser> eligibleOrKnown = users.stream().filter(u ->
                    // filter out those that are known to be eligible
                    !MojangUserMigrationStatus.Status.ELIGIBLE.name().equals(statuses.getProperty(u.getUUID().toString()))
            ).filter(u ->
                    // filter out those that return the same status as last time we checked
                    u.isReadyToMigrate().value().map(f -> {
                        try {
                            return f.get(10, TimeUnit.SECONDS).asStatus();
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            if (e instanceof InterruptedException) {
                                Thread.currentThread().interrupt();
                            }
                            return MojangUserMigrationStatus.Status.ERROR;
                        }
                    }).map(s ->
                            !s.name().equals(statuses.getProperty(u.getUUID().toString()))
                    ).orElse(true)
            );
            if (!eligibleOrKnown.findAny().isPresent()) {
                LOGGER.debug("There's no migration date yet, and user is aware of their accounts " +
                        "eligible for the migration");
                return;
            }
        }
        this.showMigrationNotificationIfFrameClosed();
    }

    private void showMigrationNotificationIfFrameClosed() {
        if (this.frame != null) {
            return;
        }
        Notification notification = new Notification(
                manifest.value()
                        .filter(m -> m.data.hasForcedMigrationDate())
                        .map(m -> "migration-icon-clock")
                        .orElse("migration-icon-warn"),
                this::showMigrationFrame
        );
        SwingUtil.later(() ->
                l.getFrame().mp.defaultScene.notificationPanel.addNotification("mojang-migration", notification)
        );
    }

    private void removeMigrationNotification() {
        l.getFrame().mp.defaultScene.notificationPanel.removeNotification("mojang-migration");
    }

    public MigrationFrame getFrame() {
        return this.frame;
    }

    public void showMigrationFrame() {
        if (this.frame == null) {
            this.frame = new MigrationFrame();
            this.frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (e.getWindow() == MigrationManager.this.frame) {
                        MigrationManager.this.frame = null;
                        writeMigrationStatus();
                        if (firstTime) {
                            firstTime = false;
                            Alert.showLocMessage("mojang-migration.button-hint");
                        }
                    }
                }
            });

            this.updateMigrationFrameIfOpened();
            this.frame.pack();
            this.frame.showAtCenter();
            this.removeMigrationNotification();
        } else {
            this.frame.requestFocus();
        }
    }

    private void updateMigrationFrameIfOpened() {
        if (this.frame != null) {
            AsyncThread.execute(() -> {
                Optional<MigrationManifest.Data> data = this.manifest.value().map(m -> m.data);
                SwingUtil.later(() ->
                        this.frame.updateUsers(
                                this.possiblyEligibleForMigration,
                                data.flatMap(MigrationManifest.Data::getForcedMigrationStartDate).orElse(null),
                                data.flatMap(MigrationManifest.Data::getForcedMigrationEndDate).orElse(null)
                        )
                );
            });
        }
    }

    private MigrationManifest fetchMigrationManifest() {
        MigrationManifest manifest;
        try {
            manifest = U.getGson().fromJson(
                    Request.Get("https://launchercontent.mojang.com/accountMigration.json")
                            .execute().returnContent().asString(),
                    MigrationManifest.class
            );
            manifest.validate();
            return manifest;
        } catch (RuntimeException e) {
            LOGGER.warn("Couldn't load or parse migration manifest", e);
            Sentry.capture(new EventBuilder()
                    .withLevel(Event.Level.WARNING)
                    .withMessage("migration manifest parse error")
                    .withSentryInterface(new ExceptionInterface(e))
            );
        } catch (IOException e) {
            LOGGER.warn("Couldn't fetch migration manifest", e);
        }
        return null;
    }

    private Properties readMigrationStatuses() {
        Properties p = new Properties();
        if (migrationStatusFile.isFile()) {
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(migrationStatusFile),
                    StandardCharsets.UTF_8
            )) {
                p.load(reader);
            } catch (IOException e) {
                LOGGER.warn("Couldn't read migration status file", e);
                p.clear();
            }
        }
        return p;
    }

    public void writeMigrationStatus() {
        // save UUIDs
        Properties p = new Properties();
        possiblyEligibleForMigration.forEach(u -> p.setProperty(
                u.getUUID().toString(),
                u.isReadyToMigrate().valueIfInitialized()
                        .map(f -> f.getNow(null))
                        .map(MojangUserMigrationStatus::asStatus)
                        .orElse(MojangUserMigrationStatus.Status.NONE)
                        .name()
        ));
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(migrationStatusFile), StandardCharsets.UTF_8)) {
            p.store(writer, null);
        } catch (IOException e) {
            LOGGER.warn("Couldn't write ignored list", e);
        }
    }

    private static class MigrationManifest {
        int version;
        Data data;

        private static class Data {
            private static final Instant NO_DATE = Instant.parse("2099-01-01T00:00:00.000Z");
            // String migrationStarts;
            String forcedMigrationStarts;
            String forcedMigrationEnds;
            // String id;

            public Optional<Instant> getForcedMigrationStartDate() {
                return parseDate(forcedMigrationStarts);
            }

            public Optional<Instant> getForcedMigrationEndDate() {
                return parseDate(forcedMigrationEnds);
            }

            public boolean hasForcedMigrationDate() {
                return getForcedMigrationStartDate().isPresent();
            }

            private static Optional<Instant> parseDate(String date) {
                Instant parsedDate = Instant.parse(date);
                return parsedDate.isBefore(NO_DATE) ? Optional.of(parsedDate) : Optional.empty();
            }
        }

        void validate() {
            Validate.isTrue(version == 1, " incompatible version: " + version);
            Validate.isTrue(data != null, "no data");
            Validate.isTrue(data.forcedMigrationStarts != null, "no forcedMigrationStarts");
            Validate.isTrue(data.forcedMigrationEnds != null, "no forcedMigrationEnds");
            data.getForcedMigrationStartDate();
            data.getForcedMigrationEndDate();
        }
    }
}
