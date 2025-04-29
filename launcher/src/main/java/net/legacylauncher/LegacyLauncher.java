package net.legacylauncher;

import com.github.zafarkhaja.semver.Version;
import joptsimple.OptionSet;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.common.exceptions.LocalIOException;
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
import net.legacylauncher.stats.Stats;
import net.legacylauncher.ui.FlatLaf;
import net.legacylauncher.ui.LegacyLauncherFrame;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.frames.FirstRunNotice;
import net.legacylauncher.ui.frames.InstallOpenGLCompatPackNotice;
import net.legacylauncher.ui.frames.NewFolderFrame;
import net.legacylauncher.ui.frames.UpdateFrame;
import net.legacylauncher.ui.listener.UIListeners;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.logger.SwingLogger;
import net.legacylauncher.ui.login.LoginForm;
import net.legacylauncher.ui.notice.NoticeManager;
import net.legacylauncher.ui.notification.Notification;
import net.legacylauncher.user.ElyUser;
import net.legacylauncher.user.PlainUser;
import net.legacylauncher.user.User;
import net.legacylauncher.util.*;
import net.legacylauncher.util.async.AsyncThread;
import net.legacylauncher.util.logging.DelegateServiceProvider;
import net.legacylauncher.util.shared.FlatLafConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.logging.slf4j.SLF4JServiceProvider;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.legacylauncher.managers.ConnectivityManager.*;

@Slf4j
public final class LegacyLauncher {
    private static final String LAUNCHER_META = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final Version SEMVER = Version.parse(BuildConfig.VERSION);
    @Getter
    private static LegacyLauncher instance;

    static {
        System.setProperty("java.net.useSystemProxies", "true");
    }

    @Getter
    private final boolean debug;
    @Getter
    private final boolean ready;
    private final BootstrapIPC bootstrapIPC;
    @Getter
    private final BootConfiguration bootConfig;
    private final boolean bootConfigEmpty;
    @Getter
    private final Configuration settings;
    @Getter
    private final LangConfiguration lang;
    @Getter
    private final ComponentManager manager;
    @Getter
    private final LibraryReplaceProcessor libraryManager;
    @Getter
    private final VersionManager versionManager;
    @Getter
    private final ProfileManager profileManager;
    @Getter
    private final JavaManager javaManager;
    @Getter
    private final MemoryAllocationService memoryAllocationService;
    @Getter
    private final GPUManager gpuManager;
    @Getter
    private final PromotedStoreManager promotedStoreManager;
    private final PersonalNoticeManager personalNoticeManager;
    @Getter
    private final Downloader downloader;
    @Getter
    private final UIListeners uiListeners;
    private final long sessionStartTime;
    private final Object onReadySync = new Object();
    private SwingLogger loggerUI;
    @Getter
    private LegacyLauncherFrame frame;
    private Queue<Runnable> onReadyJobs = new ConcurrentLinkedQueue<>();
    private Entry elyByCheckEntry;
    @Getter
    private MinecraftLauncher minecraftLauncher;

    private LegacyLauncher(BootstrapIPC bootstrapIPC) throws Exception {
        Objects.requireNonNull(bootstrapIPC, "ipc");
        checkNotRunning();
        instance = this;

        Object timer = new Object();
        Time.start(timer);

        this.bootstrapIPC = bootstrapIPC;

        getMetadata("slf4jDelegateServiceProvider", DelegateServiceProvider.class).ifPresent(delegateServiceProvider ->
                delegateServiceProvider.setProvider(new SLF4JServiceProvider())
        );
        String launcherConfiguration = bootstrapIPC.getLauncherConfiguration();
        log.debug("Launcher configuration: {}", launcherConfiguration == null ? null : launcherConfiguration.length() + " code units");

        OptionSet optionSet = ArgumentParser.parseArgs(bootstrapIPC.getLauncherArguments());
        if (optionSet.has("help")) {
            log.info("\n{}", ArgumentParser.printHelp());
            System.exit(0);
        }
        debug = optionSet.has("debug");

        bootstrapIPC.onBootProgress("Loading configuration", 0.1);
        this.settings = Configuration.createConfiguration(optionSet);
        bootstrapIPC.setMetadata("client", settings.getClient().toString());
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
            log.warn("Couldn't parse boot config: {}", launcherConfiguration, rE);
            bootConfig = new BootConfiguration();
            bootConfigEmpty = true;
        }
        this.bootConfig = bootConfig;
        this.bootConfigEmpty = bootConfigEmpty;

        Repository.updateList(bootConfig.getRepositories());
        Stats.setAllowed(bootConfig.isStatsAllowed());

        bootstrapIPC.onBootProgress("Handling run conditions", 0.17);
        handleWorkdir();
        if (!settings.isFirstRun())
            handleUpdate();
        handleNoticeHiding();

        bootstrapIPC.onBootProgress("Preparing managers", 0.2);
        this.manager = new ComponentManager(this);

        bootstrapIPC.onBootProgress("Loading Library Replace manager", 0.22);
        libraryManager = manager.loadComponent(LibraryReplaceProcessor.class);
        libraryManager.setAllowElyEverywhere(bootConfig.isElyAllowed());

        bootstrapIPC.onBootProgress("Loading Version manager", 0.27);
        versionManager = manager.loadComponent(VersionManager.class);

        bootstrapIPC.onBootProgress("Loading Profile manager", 0.35);
        profileManager = manager.loadComponent(ProfileManager.class);

        migrateFromOldJreConfig();
        javaManager = new JavaManager(createJreRootDir());

        ConnectivityManager connectivityManager = initConnectivityManager();
        connectivityManager.queueChecks();

        memoryAllocationService = new MemoryAllocationService();
        migrateMemoryValue();

        bootstrapIPC.onBootProgress("Loading manager listener", 0.36);
        manager.loadComponent(ComponentManagerListenerHelper.class);

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

        if (settings.getClient().toString().equals("23a9e755-046a-4250-9e03-1920baa98aeb")) {
            settings.set("client", UUID.randomUUID());
        }

        if (OS.LINUX.isCurrent()) {
            gpuManager = SwitcherooControlGPUManagerLoader.tryToCreate().orElse(GPUManager.Empty.INSTANCE);
        } else if (OS.WINDOWS.isCurrent()) {
            gpuManager = WindowsGpuManager.tryToCreate().orElse(GPUManager.Empty.INSTANCE);
        } else {
            gpuManager = GPUManager.Empty.INSTANCE;
        }

        log.info("Loaded GPU manager: {}", gpuManager);

        promotedStoreManager = new PromotedStoreManager();
        personalNoticeManager = new PersonalNoticeManager();

        preloadUI();

        if (elyByCheckEntry != null && profileManager.getAccountManager().getUserSet().getSet().stream().anyMatch(u ->
                u.getType().equals(ElyUser.TYPE))) {
            // show notification if Ely accounts are not available
            elyByCheckEntry.withPriority(500);
            connectivityManager.queueCheck(elyByCheckEntry);
        }

        Optional<String> packageModeOpt = getPackageMode();
        if (packageModeOpt.filter(m -> m.equals("dmg")).isPresent()) {
            Optional<String> dmgAppPathOpt = getMetadata("dmg-app-path", String.class);
            if (!dmgAppPathOpt.isPresent()) {
                log.warn("Package mode is dmg, but bootstrap hasn't announced dmg-app-path");
            } else {
                String dmgAppPath = dmgAppPathOpt.get();
                if (dmgAppPath.startsWith("/Volumes/")) {
                    log.info("Application seems to be running from a .dmg image");
                    SwingUtilities.invokeLater(() -> frame.mp.defaultScene.notificationPanel.addNotification(
                            "macos-copy-icon",
                            new Notification(
                                    "macos-copy-icon",
                                    () -> Alert.showMessage(
                                            "",
                                            Localizable.get("macos.please-install-notification")
                                    )
                            )
                    ));
                }
            }
        }

        executeWhenReady(() -> {
            if (getBootstrapVersion() != null) {
                Version version;
                try {
                    version = Version.parse(getBootstrapVersion());
                } catch (RuntimeException e) {
                    log.warn("Couldn't parse bootstrap version: {}", getBootstrapVersion(), e);
                    return;
                }
                if (version.compareTo(Version.of(1, 5, 13)) == 0) {
                    log.info("Detected deprecated bootstrap version: {}", version);
                    log.info("Collecting environment information for an upcoming upgrade");
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
                            log.info("Force selected user {} doesn't exist, but we'll create one for you",
                                    forceSelectedUser);
                            selectedUser = new PlainUser(forceSelectedUser, UUID.randomUUID(), true);
                            profileManager.getAccountManager().getUserSet().add(selectedUser);
                        } else {
                            log.warn("Force selected user {} (of type {}) doesn't exist",
                                    forceSelectedUser, forceSelectedUserType.get());
                            return;
                        }
                        break;
                    case 1:
                        log.info("Force selecting user {} for you", forceSelectedUser);
                        selectedUser = selectedUsers.get(0);
                        break;
                    default:
                        if (forceSelectedUserType.isPresent()) {
                            List<User> filteredUsers = selectedUsers.stream()
                                    .filter(u -> u.getType().equals(forceSelectedUserType.get()))
                                    .collect(Collectors.toList());
                            switch (filteredUsers.size()) {
                                case 0:
                                    log.warn("Found users with name {}, but none was of the type {}. A typo?",
                                            forceSelectedUser, forceSelectedUserType.get());
                                    return;
                                case 1:
                                    log.info("Selecting user {} of type {} for you",
                                            forceSelectedUser, forceSelectedUserType.get());
                                    selectedUser = filteredUsers.get(0);
                                    break;
                                default:
                                    log.warn("Something is very wrong with your account list. Take a look: {}",
                                            filteredUsers);
                                    return;
                            }
                        } else {
                            log.warn("More than one user with name {} was found. Consider specifying a type with --usertype",
                                    forceSelectedUser);
                            selectedUser = selectedUsers.get(0);
                        }
                        break;
                }
                log.debug("Selecting user: {}", selectedUser);
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
                    log.info("fractureiser detected in {}", linuxFractureiser);
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
                    log.warn("fractureiser trace detected in the \"Microsoft Edge\" dir");
                }
                startup = new File(appData + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\run.bat").isFile();
                if (startup) {
                    log.warn("Possible fractureiser trace detected in the Startup directory");
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
                settings.getClient(),
                getVersion().toString(),
                bootstrapIPC.getBootstrapRelease().name,
                bootstrapIPC.getBootstrapRelease().version,
                settings.getLocale()
        );
        executeWhenReady(() -> personalNoticeManager.getRequestOnce().thenAcceptAsync(payload -> {
            if (payload.getNotices().isEmpty()) {
                return;
            }
            frame.mp.defaultScene.noticePanel.load();
            NoticeManager notices = frame.getNotices();
            notices.addNoticeForCurrentLocale(payload.getNotices());
            notices.selectRandom();
        }, SwingUtil.executor()));
        executeWhenReady(() -> {
            if (settings.isFirstRun() && OS.WINDOWS.isCurrent() && OS.Arch.CURRENT.isARM()) {
                SwingUtil.later(() -> {
                    InstallOpenGLCompatPackNotice f = new InstallOpenGLCompatPackNotice();
                    f.setLocationRelativeTo(frame);
                    f.setVisible(true);
                });
            }
        });

        executeOnReadyJobs();
    }

    private File createJreRootDir() throws LocalIOException {
        File jreRootDir = settings.get(JavaManagerConfig.class).getRootDir().map(File::new).orElse(null);
        if (jreRootDir != null) {
            try {
                FileUtil.createFolder(jreRootDir);
                return jreRootDir;
            } catch (LocalIOException e) {
                log.warn("Failed to create JRE root directory: {}", jreRootDir, e);
                log.warn("Falling back to default");
            }
        }
        jreRootDir = new File(JavaManagerConfig.getDefaultRootDir());
        log.info("Creating default JRE root directory: {}", jreRootDir);
        try {
            FileUtil.createFolder(jreRootDir);
        } catch (LocalIOException e) {
            log.error("Failed to create JRE root directory in the default location", e);
            throw e;
        }
        return jreRootDir;
    }

    public static void kill() {
        LegacyLauncher legacyLauncher = LegacyLauncher.getInstance();
        if (legacyLauncher == null) {
            System.exit(0);
        }
        try {
            legacyLauncher.getSettings().save();
        } catch (Exception e) {
            Alert.showError("Configuration error", "Could not save settings – this is not good. Please contact support if you want to solve this.", e);
        }
        log.info("Goodbye!");
        // "close" main window
        legacyLauncher.frame.setVisible(false);

        Collection<AutoCloseable> closeables = new ArrayList<>();
        closeables.add(() -> {
            // report and wait 5 seconds
            Stats.reportSessionDuration(legacyLauncher.sessionStartTime).get(5, TimeUnit.SECONDS);
        });
        closeables.add(legacyLauncher.gpuManager);
        closeables.add(Portals.getPortal());
        closeables.add(legacyLauncher.bootstrapIPC);

        Exception cause = null;

        for (AutoCloseable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            } catch (Exception e) {
                if (cause == null) {
                    cause = e;
                } else {
                    cause.addSuppressed(e);
                }
            }
        }

        if (cause != null) {
            sneakyThrows(cause);
        }
    }

    @SneakyThrows
    private static void sneakyThrows(Exception cause) {
        throw cause;
    }

    public static void launch(BootstrapIPC ipc, ResolverIPC resolver) {
        checkNotRunning();

        setupErrorHandler();

        log.info("Starting Legacy Launcher {} {}", BuildConfig.FULL_BRAND, getVersion().toString());
        BootstrapIPC.BootstrapRelease bootstrapRelease = ipc.getBootstrapRelease();
        log.info("... using {} {}", bootstrapRelease.name, bootstrapRelease.version);
        log.info("... with dns resolver {}", resolver.describe());
        EHttpClient.setGlobalResolver(resolver);

        log.info("Machine info: {}", OS.getSummary());
        log.info("Java version: {}", OS.JAVA_VERSION);
        log.info("Startup time: {}", Instant.now());
        log.info("Directory: {}", new File(""));
        log.info("Executable location: {}", FileUtil.getRunningJar());
        log.info("---");

        ipc.onBootStarted();

        try {
            new LegacyLauncher(ipc);
        } catch (Throwable t) {
            log.error("Error launching Legacy Launcher", t);
            ipc.onBootError(t);
        }
    }

    public static Version getVersion() {
        return SEMVER;
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

    private ConnectivityManager initConnectivityManager() {
        List<ConnectivityManager.Entry> entries = new ArrayList<>();
        if (bootConfigEmpty) {
            entries.add(
                    forceFailed("boot").withPriority(Integer.MAX_VALUE)
            );
        }
        entries.add(checkByValidJson(
                "official_repo",
                LAUNCHER_META
        ).withPriority(1000));
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
                checkRepoByValidJson("official_repo_proxy", Repository.PROXIFIED_REPO, LAUNCHER_META)
                        .withPriority(-1000)
        );
        return new ConnectivityManager(this, entries);
    }

    private void migrateFromOldJreConfig() {
        String cmd = settings.get("minecraft.cmd");
        if (cmd == null) {
            return;
        }

        log.info("Migrating from old JRE configuration");
        log.info("minecraft.cmd -> {} = {}", JavaManagerConfig.Custom.PATH_CUSTOM_PATH, cmd);
        log.info("{} = {}", JavaManagerConfig.PATH_JRE_TYPE, JavaManagerConfig.Custom.TYPE);

        settings.set("minecraft.cmd", null);
        settings.set(JavaManagerConfig.PATH_JRE_TYPE, JavaManagerConfig.Custom.TYPE);
        settings.set(JavaManagerConfig.Custom.PATH_CUSTOM_PATH, cmd);
    }

    private void migrateLafConfigOrSetLaf() {
        boolean setSystemLaf;
        if (settings.containsKey("gui.systemlookandfeel")) {
            // pre-FlatLaf configuration
            setSystemLaf = settings.getBoolean("gui.systemlookandfeel");
            log.info("FlatLaf is not enabled because \"gui.systemlookandfeel\" is set to \"" + setSystemLaf + "\"");
            if (FlatLaf.isSupported()) {
                // already using system L&F
                settings.set(FlatLafConfiguration.KEY_STATE, setSystemLaf ?
                        FlatLafConfiguration.State.SYSTEM.toString() : FlatLafConfiguration.State.OFF.toString(), false);
                settings.set("gui.systemlookandfeel", null);
                setSystemLaf = false;
            }
        } else {
            // bootstrap didn't set L&F
            setSystemLaf = !isMetadataEnabled("set_laf");
            if (isMetadataEnabled("laf_launcher_aware") && FlatLaf.isSupported()) {
                Optional<FlatLafConfiguration> flatLafConfiguration = settings.getFlatLafConfiguration();
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
        if (settings.get("minecraft.memory") == null) {
            return;
        }
        int oldValue = settings.getInteger("minecraft.memory");
        if (oldValue == memoryAllocationService.getFallbackHint().getActual()) {
            log.info("Migrating to minecraft.xmx = \"auto\" because minecraft.memory = PREFERRED_MEMORY ({})",
                    oldValue);
            settings.set("minecraft.xmx", "auto", false);
        } else {
            log.info("Migrating to minecraft.xmx = minecraft.memory = {}", oldValue);
        }
        settings.set("minecraft.memory", null, false);
    }

    private void executeOnReadyJobs() {
        synchronized (onReadySync) {
            Objects.requireNonNull(onReadyJobs, "onReadyJobs");
            Runnable job;
            while ((job = onReadyJobs.poll()) != null) {
                try {
                    job.run();
                } catch (Exception e) {
                    log.error("OnReadyJob failed: {}", job, e);
                }
            }
            onReadyJobs = null;
        }
    }

    public boolean isMinecraftLauncherWorking() {
        return minecraftLauncher != null && minecraftLauncher.isWorking();
    }

    public MinecraftLauncher newMinecraftLauncher(String versionName, Server server, int serverId, boolean forceUpdate) {
        if (isMinecraftLauncherWorking()) {
            throw new IllegalStateException("launcher is working");
        }

        minecraftLauncher = new MinecraftLauncher(this, forceUpdate);

        for (MinecraftListener l : uiListeners.getMinecraftListeners()) {
            minecraftLauncher.addListener(l);
        }

        minecraftLauncher.setVersion(versionName);
        minecraftLauncher.setServer(server, serverId);

        List<PromotedServer> promotedServerList = new ArrayList<>();
        if (bootConfig.getPromotedServers().containsKey(getSettings().getLocale().toString())) {
            promotedServerList.addAll(bootConfig.getPromotedServers().get(getSettings().getLocale().toString()));
        } else if (bootConfig.getPromotedServers().containsKey("global")) {
            promotedServerList.addAll(bootConfig.getPromotedServers().get("global"));
        }

        minecraftLauncher.setPromotedServers(promotedServerList);

        return minecraftLauncher;
    }

    private void initConfig() {
        LegacyLauncherFrame.setFontSize(settings.getFontSize());
        if (!settings.getBoolean("connection.ssl")) {
            log.warn("Disabling SSL certificate/hostname validation. IT IS NOT SECURE.");
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
                log.error("Could not init SSLContext", e);
            }
            HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> true);
        }
        reloadLocale();
    }

    public void reloadLoggerUI() {
        SwingLoggerAppender swingLoggerAppender = Log4j2ContextHelper.getSwingLoggerAppender();
        LoggerInterface li = swingLoggerAppender.getLoggerInterface();
        boolean enableLogger = settings.getLoggerType() == Configuration.LoggerType.GLOBAL;
        if (loggerUI == null) {
            if (!enableLogger) {
                return;
            }
            loggerUI = new SwingLogger(settings);
            if (li instanceof LoggerBuffer) {
                loggerUI.drainFrom((LoggerBuffer) li);
            }
            swingLoggerAppender.setLoggerInterface(loggerUI);
            initLoggerUI();
            log.debug("Logger initialized");
        } else {
            if (enableLogger) {
                return;
            }
            swingLoggerAppender.setLoggerInterface(new LoggerBuffer());
            loggerUI.dispose();
            loggerUI = null;
            log.debug("Logger disposed");
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
            settings.set("notice.enabled", true);
        }
    }

    private void handleWorkdir() {
        if (settings.isFirstRun()) {
            handleFirstRun();
        } else {
            settings.set("minecraft.gamedir", MinecraftUtil.getWorkingDirectory(), false);
        }
    }

    private void handleFirstRun() {
        new FirstRunNotice().showAndWait();

        File currentDir = MinecraftUtil.getWorkingDirectory(false);
        log.info("Current dir: {}", currentDir);
        if (NewFolderFrame.shouldWeMoveFrom(currentDir)) {
            currentDir = NewFolderFrame.selectDestination();
            if (currentDir != null) {
                new NewFolderFrame(this, currentDir).showAndWait();
            }
        }
    }

    public boolean isNoticeDisablingAllowed() {
        return bootConfig.isAllowNoticeDisable(settings.getClient());
    }

    public boolean isNoticeDisabled() {
        return isNoticeDisablingAllowed() && !settings.getBoolean("notice.enabled");
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

        AsyncThread.DELAYER.scheduleWithFixedDelay(
                () -> AsyncThread.execute(Stats::beacon),
                30, 30, TimeUnit.MINUTES
        );
    }

    private void preloadUI() {
        frame.mp.defaultScene.settingsForm.load();
    }

    public void reloadLocale() {
        Locale locale = settings.getLocale();

        log.info("Selected locale: {}", locale);
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

    public <V> Optional<V> getMetadata(String key, Class<? extends V> metadataClass) {
        Object o = bootstrapIPC.getMetadata(key);
        if (o == null) {
            return Optional.empty();
        }
        if (!metadataClass.isInstance(o)) {
            log.warn("Metadata type mismatch! Key: {}, expected: {}, got: {}", key, metadataClass, o.getClass());
            return Optional.empty();
        }
        return Optional.of(metadataClass.cast(o));
    }

    public boolean isMetadataEnabled(String key) {
        return Objects.equals(Boolean.TRUE, bootstrapIPC.getMetadata(key));
    }

    public String getBootstrapVersion() {
        return bootstrapIPC.getBootstrapRelease().version;
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
}
