package net.legacylauncher;

import com.github.zafarkhaja.semver.Version;
import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import joptsimple.OptionSet;
import net.legacylauncher.configuration.*;
import net.legacylauncher.downloader.Downloader;
import net.legacylauncher.handlers.ExceptionHandler;
import net.legacylauncher.ipc.BootstrapIPC;
import net.legacylauncher.ipc.ResolverIPC;
import net.legacylauncher.logger.Log4j2ContextHelper;
import net.legacylauncher.logger.LoggerBuffer;
import net.legacylauncher.logger.LoggerInterface;
import net.legacylauncher.logger.SwingLoggerAppender;
import net.legacylauncher.managers.*;
import net.legacylauncher.minecraft.PromotedServer;
import net.legacylauncher.minecraft.Server;
import net.legacylauncher.minecraft.launcher.MinecraftLauncher;
import net.legacylauncher.minecraft.launcher.MinecraftListener;
import net.legacylauncher.portals.Portals;
import net.legacylauncher.repository.Repository;
import net.legacylauncher.sentry.SentryConfigurer;
import net.legacylauncher.stats.Stats;
import net.legacylauncher.ui.FlatLaf;
import net.legacylauncher.ui.LegacyLauncherFrame;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.frames.FirstRunNotice;
import net.legacylauncher.ui.frames.NewFolderFrame;
import net.legacylauncher.ui.frames.UpdateFrame;
import net.legacylauncher.ui.listener.UIListeners;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.logger.SwingLogger;
import net.legacylauncher.ui.login.LoginForm;
import net.legacylauncher.ui.notice.NoticeManager;
import net.legacylauncher.ui.notification.Notification;
import net.legacylauncher.user.*;
import net.legacylauncher.util.*;
import net.legacylauncher.util.async.ExtendedThread;
import net.legacylauncher.util.logging.DelegateServiceProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.slf4j.SLF4JServiceProvider;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.legacylauncher.managers.ConnectivityManager.*;

public final class LegacyLauncher {
    private static final Logger LOGGER = LogManager.getLogger(LegacyLauncher.class);

    private final boolean debug, ready;

    private final BootstrapIPC bootstrapIPC;
    private final ResolverIPC resolverIPC;
    private final BootConfiguration bootConfig;
    private final boolean bootConfigEmpty;

    private final Configuration config;
    private final LangConfiguration lang;

    private SwingLogger loggerUI;

    private final ComponentManager componentManager;
    private final LibraryReplaceProcessor libraryReplaceManager;
    private final VersionManager versionManager;
    private final ProfileManager profileManager;
    private final JavaManager javaManager;
    private final ConnectivityManager connectivityManager;
    private final MemoryAllocationService memoryAllocationService;
    private final GPUManager gpuManager;
    private final PromotedStoreManager promotedStoreManager;
    private final PersonalNoticeManager personalNoticeManager;

    private final Downloader downloader;

    private final UIListeners uiListeners;

    private LegacyLauncherFrame frame;

    private final long sessionStartTime;

    private final Object onReadySync = new Object();
    private Queue<Runnable> onReadyJobs = new ConcurrentLinkedQueue<>();

    private LegacyLauncher(BootstrapIPC bootstrapIPC, ResolverIPC resolver) throws Exception {
        Objects.requireNonNull(bootstrapIPC, "ipc");
        checkNotRunning();
        instance = this;

        Object timer = new Object();
        Time.start(timer);

        this.bootstrapIPC = bootstrapIPC;
        this.resolverIPC = resolver;

        getMetadata("slf4jDelegateServiceProvider", DelegateServiceProvider.class).ifPresent(delegateServiceProvider ->
                delegateServiceProvider.setProvider(new SLF4JServiceProvider())
        );
        String launcherConfiguration = bootstrapIPC.getLauncherConfiguration();
        LOGGER.debug("Launcher configuration: {}", launcherConfiguration == null ? null : launcherConfiguration.length() + " code units");

        OptionSet optionSet = ArgumentParser.parseArgs(bootstrapIPC.getLauncherArguments());
        if (optionSet.has("help")) {
            LOGGER.info("\n{}", ArgumentParser.printHelp());
            System.exit(0);
        }
        debug = optionSet.has("debug");

        bootstrapIPC.onBootProgress("Loading configuration", 0.1);
        this.config = Configuration.createConfiguration(optionSet);
        bootstrapIPC.setMetadata("client", config.getClient().toString());
        migrateLafConfigOrSetLaf();
        this.lang = new LangConfiguration();
        initConfig();

        // pass launcher classloader to awt main thread
        ClassLoaderFixUp.fixup();

        SwingUtil.wait(this::reloadLoggerUI);

        BootConfiguration bootConfig;
        boolean bootConfigEmpty = false;
        try {
            bootConfig = BootConfiguration.parse(launcherConfiguration);
        } catch (RuntimeException rE) {
            LOGGER.warn("Couldn't parse boot config: {}", launcherConfiguration, rE);
            bootConfig = new BootConfiguration();
            bootConfigEmpty = true;
        }
        this.bootConfig = bootConfig;
        this.bootConfigEmpty = bootConfigEmpty;

        Repository.updateList(bootConfig.getRepositories());
        Stats.setAllowed(bootConfig.isStatsAllowed());

        bootstrapIPC.onBootProgress("Handling run conditions", 0.17);
        handleWorkdir();
        if (!config.isFirstRun())
            handleUpdate();
        handleNoticeHiding();

        bootstrapIPC.onBootProgress("Preparing managers", 0.2);
        this.componentManager = new ComponentManager(this);

        bootstrapIPC.onBootProgress("Loading Library Replace manager", 0.22);
        libraryReplaceManager = componentManager.loadComponent(LibraryReplaceProcessor.class);
        libraryReplaceManager.setAllowElyEverywhere(bootConfig.isElyAllowed());

        bootstrapIPC.onBootProgress("Loading Version manager", 0.27);
        versionManager = componentManager.loadComponent(VersionManager.class);

        bootstrapIPC.onBootProgress("Loading Profile manager", 0.35);
        profileManager = componentManager.loadComponent(ProfileManager.class);

        migrateFromOldJreConfig();
        File jreRootDir = new File(config.get(JavaManagerConfig.class).getRootDirOrDefault());
        FileUtil.createFolder(jreRootDir);
        javaManager = new JavaManager(jreRootDir);

        connectivityManager = initConnectivityManager();
        connectivityManager.queueChecks();

        memoryAllocationService = new MemoryAllocationService();
        migrateMemoryValue();

        bootstrapIPC.onBootProgress("Loading manager listener", 0.36);
        componentManager.loadComponent(ComponentManagerListenerHelper.class);

        bootstrapIPC.onBootProgress("Loading Downloader", 0.4);
        downloader = new Downloader();

        bootstrapIPC.onBootProgress("Loading UI Listeners", 0.5);
        uiListeners = new UIListeners(this);

        SwingUtil.wait(() -> {
            bootstrapIPC.onBootProgress("Loading frame", 0.65);
            frame = new LegacyLauncherFrame(this);

            bootstrapIPC.onBootProgress("Post-init UI", 0.8);
            initAndRefreshUI();
        });

        ready = true;
        sessionStartTime = System.currentTimeMillis();

        bootstrapIPC.onBootSucceeded();

        if (config.getClient().toString().equals("23a9e755-046a-4250-9e03-1920baa98aeb")) {
            config.set("client", UUID.randomUUID());
        }

        if (OS.LINUX.isCurrent()) {
            gpuManager = SwitcherooControlGPUManagerLoader.tryToCreate().orElse(GPUManager.Empty.INSTANCE);
        } else if (OS.WINDOWS.isCurrent()) {
            gpuManager = WindowsGpuManager.tryToCreate().orElse(GPUManager.Empty.INSTANCE);
        } else {
            gpuManager = GPUManager.Empty.INSTANCE;
        }

        LOGGER.info("Loaded GPU manager: {}", gpuManager);

        promotedStoreManager = new PromotedStoreManager();
        personalNoticeManager = new PersonalNoticeManager();

        preloadUI();

        if (elyByCheckEntry != null && profileManager.getAccountManager().getUserSet().getSet().stream().anyMatch(u ->
                u.getType().equals(ElyUser.TYPE))) {
            // show notification if Ely accounts are not available
            elyByCheckEntry.withPriority(500);
            connectivityManager.queueCheck(elyByCheckEntry);
        }

        if (authServerCheckEntry != null) {
            if (profileManager.getAccountManager().getUserSet().getSet().stream()
                    .anyMatch(u -> u.getType().equals(MojangUser.TYPE) || u.getType().equals(MinecraftUser.TYPE))
            ) {
                bumpAuthServerCheckPriority();
            } else {
                authServerCheckEntry.getTask().thenRun(() -> {
                    AuthServerChecker checker = (AuthServerChecker) authServerCheckEntry.getChecker();
                    if (checker.getDetectedThirdPartyAuthenticator() != null) {
                        bumpAuthServerCheckPriority();
                    }
                });
            }
            connectivityManager.queueCheck(authServerCheckEntry);
        }

        Optional<String> packageModeOpt = getPackageMode();
        if (packageModeOpt.filter(m -> m.equals("dmg")).isPresent()) {
            Optional<String> dmgAppPathOpt = getMetadata("dmg-app-path", String.class);
            if (!dmgAppPathOpt.isPresent()) {
                LOGGER.warn("Package mode is dmg, but bootstrap hasn't announced dmg-app-path");
            } else {
                String dmgAppPath = dmgAppPathOpt.get();
                if (dmgAppPath.startsWith("/Volumes/")) {
                    LOGGER.info("Application seems to be running from a .dmg image");
                    SwingUtilities.invokeLater(() -> {
                        frame.mp.defaultScene.notificationPanel.addNotification(
                                "macos-copy-icon",
                                new Notification(
                                        "macos-copy-icon",
                                        () -> Alert.showMessage(
                                                "",
                                                Localizable.get("macos.please-install-notification")
                                        )
                                )
                        );
                    });
                }
            }
        }

        executeWhenReady(() -> {
            if (getBootstrapVersion() != null) {
                Version version;
                try {
                    version = Version.valueOf(getBootstrapVersion());
                } catch (RuntimeException e) {
                    LOGGER.warn("Couldn't parse bootstrap version: {}", getBootstrapVersion(), e);
                    Sentry.capture(new EventBuilder()
                            .withLevel(Event.Level.ERROR)
                            .withMessage("couldn't parse bootstrap version")
                            .withSentryInterface(new ExceptionInterface(e))
                    );
                    return;
                }
                if (version.compareTo(Version.forIntegers(1, 5, 13)) == 0) {
                    LOGGER.info("Detected deprecated bootstrap version: {}", version);
                    LOGGER.info("Collecting environment information for an upcoming upgrade");
                    String gameDir = config.get("minecraft.gamedir");
                    Sentry.capture(new EventBuilder()
                            .withLevel(Event.Level.INFO)
                            .withMessage("Deprecated bootstrap: gameDir")
                            .withTag("gameDirAbsolute", String.valueOf(Paths.get(gameDir).isAbsolute()))
                            .withExtra("gameDir", StringUtils.replace(gameDir, System.getProperty("user.name"), "***"))
                    );
                }
            }
        });

        executeWhenReady(() -> {
            if (optionSet.has("username")) {
                String forceSelectedUser = (String) optionSet.valueOf("username");
                Optional<String> forceSelectedUserType = Optional.ofNullable(optionSet.valueOf("usertype")).map(o -> (String) o);
                List<User> selectedUsers = profileManager.getAccountManager().getUserSet().getSet().stream()
                        .filter(u -> u.getDisplayName().equals(forceSelectedUser)).collect(Collectors.toList());
                User selectedUser;
                switch (selectedUsers.size()) {
                    case 0:
                        if (!forceSelectedUserType.isPresent() || forceSelectedUserType.get().equals("plain")) {
                            LOGGER.info("Force selected user {} doesn't exist, but we'll create one for you",
                                    forceSelectedUser);
                            selectedUser = new PlainUser(forceSelectedUser, UUID.randomUUID());
                            profileManager.getAccountManager().getUserSet().add(selectedUser);
                        } else {
                            LOGGER.warn("Force selected user {} (of type {}) doesn't exist",
                                    forceSelectedUser, forceSelectedUserType.get());
                            return;
                        }
                        break;
                    case 1:
                        LOGGER.info("Force selecting user {} for you", forceSelectedUser);
                        selectedUser = selectedUsers.get(0);
                        break;
                    default:
                        if (forceSelectedUserType.isPresent()) {
                            List<User> filteredUsers = selectedUsers.stream()
                                    .filter(u -> u.getType().equals(forceSelectedUserType.get()))
                                    .collect(Collectors.toList());
                            switch (filteredUsers.size()) {
                                case 0:
                                    LOGGER.warn("Found users with name {}, but none was of the type {}. A typo?",
                                            forceSelectedUser, forceSelectedUserType.get());
                                    return;
                                case 1:
                                    LOGGER.info("Selecting user {} of type {} for you",
                                            forceSelectedUser, forceSelectedUserType.get());
                                    selectedUser = filteredUsers.get(0);
                                    break;
                                default:
                                    LOGGER.warn("Something is very wrong with your account list. Take a look: {}",
                                            filteredUsers);
                                    return;
                            }
                        } else {
                            LOGGER.warn("More than one user with name {} was found. Consider specifying a type with --usertype",
                                    forceSelectedUser);
                            selectedUser = selectedUsers.get(0);
                        }
                        break;
                }
                LOGGER.debug("Selecting user: {}", selectedUser);
                profileManager.getAccountManager().getUserSet().select(selectedUser);
            }
        });

        executeWhenReady(() -> {
            boolean found;
            if (OS.LINUX.isCurrent()) {
                Path linuxFractureiser = FileUtils.getUserDirectory().toPath()
                        .resolve(".config")
                        .resolve(".data")
                        .resolve("lib.jar");
                found = Files.isRegularFile(linuxFractureiser);
                if (found) {
                    LOGGER.info("fractureiser detected in {}", linuxFractureiser);
                }
            } else if (OS.WINDOWS.isCurrent()) {
                String appData = System.getenv("APPDATA");
                if (appData == null) {
                    appData = System.getProperty("user.home") + "\\AppData";
                }
                String localAppData = System.getenv("LOCALAPPDATA");
                if (localAppData == null) {
                    localAppData = System.getProperty("user.home") + "\\AppData\\Local";
                }
                boolean edge, startup;
                Path edgePath = new File(localAppData + "\\Microsoft Edge").toPath();
                edge = Stream.of(
                        ".ref",
                        "client.jar",
                        "lib.dll",
                        "libWebGL64.jar"
                ).anyMatch(p ->
                        Files.isRegularFile(edgePath.resolve(p))
                );
                if (edge) {
                    LOGGER.warn("fractureiser trace detected in the \"Microsoft Edge\" dir");
                }
                startup = new File(appData + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\run.bat").isFile();
                if (startup) {
                    LOGGER.warn("Possible fractureiser trace detected in the Startup directory");
                }
                found = edge || startup;
            } else {
                return; // not affected
            }
            if (found) {
                Stats.fractureiserTraceDetected();
                Alert.showWarning("", Localizable.get("fractureiser.detected"));
            }
        });

        personalNoticeManager.queueRequest(
                config.getClient(),
                U.getFormattedVersion(getVersion()),
                bootstrapIPC.getBootstrapRelease().name,
                bootstrapIPC.getBootstrapRelease().version,
                config.getLocale()
        );
        executeWhenReady(() -> {
            personalNoticeManager.getRequestOnce().thenAcceptAsync(payload -> {
                if (payload.getNotices().isEmpty()) {
                    return;
                }
                frame.mp.defaultScene.noticePanel.load();
                NoticeManager notices = frame.getNotices();
                notices.addNoticeForCurrentLocale(payload.getNotices());
                notices.selectRandom();
            }, SwingUtil.executor());
        });

        executeOnReadyJobs();
    }

    private static final String PONG_RESPONSE = "Pong!\n";
    private static final String LAUNCHERMETA = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private Entry authServerCheckEntry, elyByCheckEntry;

    private ConnectivityManager initConnectivityManager() {
        List<ConnectivityManager.Entry> entries = new ArrayList<>();
        if (bootConfigEmpty) {
            entries.add(
                    forceFailed("boot").withPriority(Integer.MAX_VALUE)
            );
        }
        entries.add(checkByValidJson(
                "official_repo",
                LAUNCHERMETA
        ).withPriority(1000));
        entries.add(
                authServerCheckEntry = AuthServerChecker
                        .createEntry(resolverIPC)
                        .withPriority(-500) // will be set to 1000 if third party authenticator is detected
                // or Mojang/Microsoft accounts are presented
        );
        elyByCheckEntry = checkByValidJson(
                "account.ely.by",
                "https://account.ely.by/api/minecraft/session/profile/ffb3378cd561502fa78a08494be68811"
        ).withPriority(-500); // will be set to 500 if ely accounts are presented
        entries.add(elyByCheckEntry);
        entries.add(checkRepoByValidJson(
                "repo",
                Repository.EXTRA_VERSION_REPO,
                "versions/versions.json"
        ));
        // entries with negative priority are pretty much ignored, and only shown when there are other
        // entries that are not available
        entries.add(
                checkByContent("launcher.mojang.com", "https://launcher.mojang.com", "")
                        .withPriority(-500)
        );
        entries.add(
                checkRepoByValidJson("official_repo_proxy", Repository.PROXIFIED_REPO, LAUNCHERMETA)
                        .withPriority(-1000)
        );
        return new ConnectivityManager(this, entries);
    }

    private void bumpAuthServerCheckPriority() {
        authServerCheckEntry.withPriority(1000);
        authServerCheckEntry.getTask().thenRun(connectivityManager::showNotificationOnceIfNeeded);
    }

    private void migrateFromOldJreConfig() {
        String cmd = config.get("minecraft.cmd");
        if (cmd == null) {
            return;
        }

        LOGGER.info("Migrating from old JRE configuration");
        LOGGER.info("minecraft.cmd -> {} = {}", JavaManagerConfig.Custom.PATH_CUSTOM_PATH, cmd);
        LOGGER.info("{} = {}", JavaManagerConfig.PATH_JRE_TYPE, JavaManagerConfig.Custom.TYPE);

        config.set("minecraft.cmd", null);
        config.set(JavaManagerConfig.PATH_JRE_TYPE, JavaManagerConfig.Custom.TYPE);
        config.set(JavaManagerConfig.Custom.PATH_CUSTOM_PATH, cmd);
    }

    private void migrateLafConfigOrSetLaf() {
        boolean setSystemLaf;
        if (config.containsKey("gui.systemlookandfeel")) {
            // pre-FlatLaf configuration
            setSystemLaf = config.getBoolean("gui.systemlookandfeel");
            LOGGER.info("FlatLaf is not enabled because \"gui.systemlookandfeel\" is set to \"" + setSystemLaf + "\"");
            if (FlatLaf.isSupported()) {
                // already using system L&F
                config.set(FlatLafConfiguration.KEY_STATE, setSystemLaf ?
                        FlatLafConfiguration.State.SYSTEM.toString() : FlatLafConfiguration.State.OFF.toString(), false);
                config.set("gui.systemlookandfeel", null);
                setSystemLaf = false;
            }
        } else {
            // bootstrap didn't set L&F
            setSystemLaf = !isMetadataEnabled("set_laf");
            if (isMetadataEnabled("laf_launcher_aware") && FlatLaf.isSupported()) {
                Optional<FlatLafConfiguration> flatLafConfiguration = config.getFlatLafConfiguration();
                if (flatLafConfiguration.isPresent()) {
                    FlatLaf.initialize(flatLafConfiguration.get());
                    setSystemLaf = false;
                }
            }
        }
        if (setSystemLaf) {
            FlatLaf.setSystemLookAndFeel();
        }
    }

    private void migrateMemoryValue() {
        if (config.get("minecraft.memory") == null) {
            return;
        }
        int oldValue = config.getInteger("minecraft.memory");
        if (oldValue == memoryAllocationService.getFallbackHint().getActual()) {
            LOGGER.info("Migrating to minecraft.xmx = \"auto\" because minecraft.memory = PREFERRED_MEMORY ({})",
                    oldValue);
            config.set("minecraft.xmx", "auto", false);
        } else {
            LOGGER.info("Migrating to minecraft.xmx = minecraft.memory = {}", oldValue);
        }
        config.set("minecraft.memory", null, false);
    }

    private void executeOnReadyJobs() {
        synchronized (onReadySync) {
            Objects.requireNonNull(onReadyJobs, "onReadyJobs");
            Runnable job;
            while ((job = onReadyJobs.poll()) != null) {
                try {
                    job.run();
                } catch (Exception e) {
                    LOGGER.error("OnReadyJob failed: {}", job, e);
                }
            }
            onReadyJobs = null;
        }
    }

    public BootConfiguration getBootConfig() {
        return bootConfig;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isReady() {
        return ready;
    }

    public Configuration getSettings() {
        return config;
    }

    public LangConfiguration getLang() {
        return lang;
    }

    public ComponentManager getManager() {
        return componentManager;
    }

    public LibraryReplaceProcessor getLibraryManager() {
        return libraryReplaceManager;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public JavaManager getJavaManager() {
        return javaManager;
    }

    public Downloader getDownloader() {
        return downloader;
    }

    public UIListeners getUIListeners() {
        return uiListeners;
    }

    public LegacyLauncherFrame getFrame() {
        return frame;
    }

    private MinecraftLauncher launcher;

    public MinecraftLauncher getMinecraftLauncher() {
        return launcher;
    }

    public boolean isMinecraftLauncherWorking() {
        return launcher != null && launcher.isWorking();
    }

    public MinecraftLauncher newMinecraftLauncher(String versionName, Server server, int serverId, boolean forceUpdate) {
        if (isMinecraftLauncherWorking()) {
            throw new IllegalStateException("launcher is working");
        }

        launcher = new MinecraftLauncher(this, forceUpdate);

        for (MinecraftListener l : uiListeners.getMinecraftListeners()) {
            launcher.addListener(l);
        }

        launcher.setVersion(versionName);
        launcher.setServer(server, serverId);

        List<PromotedServer> promotedServerList = new ArrayList<>();
        if (bootConfig.getPromotedServers().containsKey(getSettings().getLocale().toString())) {
            promotedServerList.addAll(bootConfig.getPromotedServers().get(getSettings().getLocale().toString()));
        } else if (bootConfig.getPromotedServers().containsKey("global")) {
            promotedServerList.addAll(bootConfig.getPromotedServers().get("global"));
        }

        List<PromotedServer> outdatedServerList = new ArrayList<>();
        if (bootConfig.getOutdatedPromotedServers().containsKey(getSettings().getLocale().toString())) {
            outdatedServerList.addAll(bootConfig.getOutdatedPromotedServers().get(getSettings().getLocale().toString()));
        } else if (bootConfig.getOutdatedPromotedServers().containsKey("global")) {
            outdatedServerList.addAll(bootConfig.getOutdatedPromotedServers().get("global"));
        }
        launcher.setPromotedServers(promotedServerList, outdatedServerList);

        return launcher;
    }

    public static void kill() {
        LegacyLauncher legacyLauncher = LegacyLauncher.getInstance();
        if (legacyLauncher != null) {
            try {
                legacyLauncher.getSettings().save();
            } catch (Exception e) {
                Alert.showError("Configuration error", "Could not save settings – this is not good. Please contact support if you want to solve this.", e);
            }
            LOGGER.info("Goodbye!");
            // "close" main window
            legacyLauncher.frame.setVisible(false);

            // yes, that abomination of try-finally is required to properly handle everything
            try {
                // report and wait 5 seconds
                Stats.reportSessionDuration(legacyLauncher.sessionStartTime).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            } finally {
                try {
                    legacyLauncher.gpuManager.close();
                } catch (IOException ignored) {
                }
                try {
                    Portals.getPortal().close();
                } catch (IOException ignored) {
                }
                try {
                    legacyLauncher.bootstrapIPC.requestClose();
                } finally {
                    try {
                        legacyLauncher.bootstrapIPC.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        } else {
            System.exit(0);
        }
    }

    private void initConfig() {
        SentryConfigurer.setUser(config.getClient());
        LegacyLauncherFrame.setFontSize(config.getFontSize());
        if (!config.getBoolean("connection.ssl")) {
            LOGGER.warn("Disabling SSL certificate/hostname validation. IT IS NOT SECURE.");
            try {
                SSLContext context = SSLContext.getInstance("SSL");
                context.init(null, new X509TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                }, null);
                HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            } catch (Exception e) {
                LOGGER.error("Could not init SSLContext", e);
            }
            HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> true);
        }
        reloadLocale();
    }

    public void reloadLoggerUI() {
        SwingLoggerAppender swingLoggerAppender = Log4j2ContextHelper.getSwingLoggerAppender();
        LoggerInterface li = swingLoggerAppender.getLoggerInterface();
        boolean enableLogger = config.getLoggerType() == Configuration.LoggerType.GLOBAL;
        if (loggerUI == null) {
            if (!enableLogger) {
                return;
            }
            loggerUI = new SwingLogger(config);
            if (li instanceof LoggerBuffer) {
                loggerUI.drainFrom((LoggerBuffer) li);
            }
            swingLoggerAppender.setLoggerInterface(loggerUI);
            initLoggerUI();
            LOGGER.debug("Logger initialized");
        } else {
            if (enableLogger) {
                return;
            }
            swingLoggerAppender.setLoggerInterface(new LoggerBuffer());
            loggerUI.dispose();
            loggerUI = null;
            LOGGER.debug("Logger disposed");
        }
    }

    private void initLoggerUI() {
        loggerUI.setFolderAction(() -> {
            if (isMinecraftLauncherWorking()) {
                OS.openFolder(getMinecraftLauncher().getGameDir());
            } else {
                OS.openFolder(MinecraftUtil.getWorkingDirectory());
            }
        });
        loggerUI.setSaveAction(() -> OS.openFile(Log4j2ContextHelper.getCurrentLogFile().getFile()));
        updateLoggerUIActions();
        loggerUI.show();
    }

    public void updateLoggerUIActions() {
        if (loggerUI == null) {
            return;
        }
        if (isMinecraftLauncherWorking() && getMinecraftLauncher().isMinecraftRunning()) {
            loggerUI.setKillAction(() -> getMinecraftLauncher().killProcess());
        } else {
            loggerUI.setKillAction(null);
        }
    }

    private void handleNoticeHiding() {
        if (!isNoticeDisablingAllowed()) {
            config.set("notice.enabled", true);
        }
    }

    private void handleWorkdir() {
        if (config.isFirstRun()) {
            handleFirstRun();
        } else {
            config.set("minecraft.gamedir", MinecraftUtil.getWorkingDirectory(), false);
        }
    }

    public static void launch(BootstrapIPC ipc, ResolverIPC resolver) {
        checkNotRunning();

        setupErrorHandler();

        LOGGER.info("Starting Legacy Launcher {} {}", getBrand(), U.getFormattedVersion(getVersion()));
        BootstrapIPC.BootstrapRelease bootstrapRelease = ipc.getBootstrapRelease();
        LOGGER.info("... using {} {}", bootstrapRelease.name, bootstrapRelease.version);
        LOGGER.info("... with dns resolver {}", resolver.describe());
        EHttpClient.setGlobalResolver(resolver);

        LOGGER.info("Machine info: {}", OS.getSummary());
        LOGGER.info("Java version: {}", OS.JAVA_VERSION);
        LOGGER.info("Startup time: {}", Instant.now());
        LOGGER.info("Directory: {}", new File(""));
        LOGGER.info("Executable location: {}", FileUtil.getRunningJar());
        LOGGER.info("---");

        ipc.onBootStarted();

//        while (true) {
        try {
            new LegacyLauncher(ipc, resolver);
        } catch (Throwable t) {
            LOGGER.fatal("Error launching Legacy Launcher", t);
            ipc.onBootError(t);
        }
//        }
    }

    private void handleFirstRun() {
        new FirstRunNotice().showAndWait();

        File currentDir = MinecraftUtil.getWorkingDirectory(false);
        LOGGER.info("Current dir: {}", currentDir);
        if (NewFolderFrame.shouldWeMoveFrom(currentDir)) {
            currentDir = NewFolderFrame.selectDestination();
            if (currentDir != null) {
                new NewFolderFrame(this, currentDir).showAndWait();
            }
        }
    }

    public boolean isNoticeDisablingAllowed() {
        return bootConfig.isAllowNoticeDisable(config.getClient());
    }

    public boolean isNoticeDisabled() {
        return isNoticeDisablingAllowed() && !config.getBoolean("notice.enabled");
    }

    private void initAndRefreshUI() {
        LoginForm lf = frame.mp.defaultScene.loginForm;

        if (lf.autologin.isEnabled()) {
            versionManager.startRefresh(true);
            lf.autologin.setActive(true);
        } else {
            versionManager.asyncRefresh();
        }

        profileManager.refresh();

        new ExtendedThread(() -> {
            Stats.submitNoticeStatus(config.getBoolean("notice.enabled"));
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.MINUTES.sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Stats.beacon();
            }
        }, "Beacon").start();
    }

    private void preloadUI() {
        frame.mp.defaultScene.settingsForm.load();
    }

    public void reloadLocale() {
        Locale locale = config.getLocale();

        LOGGER.info("Selected locale: {}", locale);
        lang.setLocale(locale);

        SwingUtil.wait(() -> {
            Localizable.setLang(lang);
            Alert.prepareLocal();

            if (loggerUI != null) {
                loggerUI.updateLocale();
            }

            if (uiListeners != null) {
                uiListeners.updateLocale();
            }
        });
    }

    public void executeWhenReady(Runnable r) {
        synchronized (onReadySync) {
            if (onReadyJobs == null) {
                r.run();
            } else {
                onReadyJobs.offer(r);
            }
        }
    }

    public <V> Optional<V> getMetadata(String key, Class<V> metadataClass) {
        Object o = bootstrapIPC.getMetadata(key);
        if (o == null) {
            return Optional.empty();
        }
        if (!metadataClass.isInstance(o)) {
            LOGGER.warn("Metadata type mismatch! Key: {}, expected: {}, got: {}", key, metadataClass, o.getClass());
            return Optional.empty();
        }
        return Optional.of(metadataClass.cast(o));
    }

    public boolean isMetadataEnabled(String key) {
        return Objects.equals(Boolean.TRUE, bootstrapIPC.getMetadata(key));
    }

    private static LegacyLauncher instance;
    private static final Version SEMVER;

    public static LegacyLauncher getInstance() {
        return instance;
    }

    public static Version getVersion() {
        return SEMVER;
    }

    public String getBootstrapVersion() {
        return bootstrapIPC.getBootstrapRelease().version;
    }

    public MemoryAllocationService getMemoryAllocationService() {
        return memoryAllocationService;
    }

    static {
        SEMVER = Objects.requireNonNull(Version.valueOf(BuildConfig.VERSION), "semver");
    }

    public static String getBrand() {
        return Static.getBrand();
    }

    public static String getShortBrand() {
        return Static.getShortBrand();
    }

    public static String getFolder() {
        return Static.getFolder();
    }

    public static String getSettingsFile() {
        return Static.getSettings();
    }

    public static List<String> getOfficialRepo() {
        return Static.getOfficialRepo();
    }

    public static List<String> getExtraRepo() {
        return Static.getExtraRepo();
    }

    public static List<String> getLibraryRepo() {
        return Static.getLibraryRepo();
    }

    public static List<String> getAssetsRepo() {
        return Static.getAssetsRepo();
    }

    public static List<String> getServerList() {
        return Static.getServerList();
    }

    public static String getSupportEmail() {
        return "support@tln4.ru";
    }

    public GPUManager getGpuManager() {
        return gpuManager;
    }

    public PromotedStoreManager getPromotedStoreManager() {
        return promotedStoreManager;
    }

    public Optional<String> getPackageMode() {
        return getMetadata("package_mode", String.class);
    }

    private void handleUpdate() {
        String locale = getSettings().getLocale().toString();
        Map<String, BootstrapIPC.ReleaseNotes> releaseNotes = bootstrapIPC.getLauncherReleaseNotes(getVersion().toString());
        BootstrapIPC.ReleaseNotes actualNotes = releaseNotes.get(locale);
        if (actualNotes == null) {
            actualNotes = releaseNotes.get("en_US");
        }
        if (actualNotes != null) {
            new UpdateFrame(U.getMinorVersion(getVersion()), actualNotes.body).showAndWait();
        }
    }

    private static void checkNotRunning() {
        if (instance != null) {
            throw new IllegalStateException("already running");
        }
    }

    private static void setupErrorHandler() {
        ExceptionHandler handler = ExceptionHandler.getInstance();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Thread.currentThread().setUncaughtExceptionHandler(handler);
    }

    static {
        System.setProperty("java.net.useSystemProxies", "true");
        SentryConfigurer.configure(getVersion(), getShortBrand());
    }
}
