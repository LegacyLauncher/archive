package ru.turikhay.tlauncher.bootstrap;

import com.getsentry.raven.DefaultRavenFactory;
import com.getsentry.raven.Raven;
import com.getsentry.raven.dsn.Dsn;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.User;
import com.getsentry.raven.event.helper.EventBuilderHelper;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.bridge.BootListenerAdapter;
import ru.turikhay.tlauncher.bootstrap.exception.*;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.launcher.*;
import ru.turikhay.tlauncher.bootstrap.meta.*;
import ru.turikhay.tlauncher.bootstrap.ssl.FixSSL;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.task.TaskInterruptedException;
import ru.turikhay.tlauncher.bootstrap.task.TaskList;
import ru.turikhay.tlauncher.bootstrap.transport.SignedStream;
import ru.turikhay.tlauncher.bootstrap.ui.HeadlessInterface;
import ru.turikhay.tlauncher.bootstrap.ui.IInterface;
import ru.turikhay.tlauncher.bootstrap.ui.UserInterface;
import ru.turikhay.tlauncher.bootstrap.util.*;
import ru.turikhay.tlauncher.bootstrap.util.stream.OutputRedirectBuffer;
import ru.turikhay.tlauncher.bootstrap.util.stream.RedirectPrintStream;
import ru.turikhay.util.windows.wmi.WMI;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public final class Bootstrap {
    public static final Raven SENTRY = new DefaultRavenFactory().createRavenInstance(
            new Dsn("https://3ece46580a3c4d4e900f41d20397d229:8fbceaeb066e4fcab40f9740d04eebab@sentry.ely.by/45")
    );

    static {
        SENTRY.addBuilderHelper(new EventBuilderHelper() {
            @Override
            public void helpBuildingEvent(EventBuilder eventBuilder) {
                eventBuilder
                        .withServerName(OS.CURRENT.name())
                        .withTag(
                                "java",
                                JavaVersion.getCurrent() == JavaVersion.UNKNOWN ?
                                        "unknown" : String.valueOf(JavaVersion.getCurrent().getMajor())
                        )
                        .withTag("java_version", System.getProperty("java.version"))
                        .withTag("os", System.getProperty("os.name") + " " + System.getProperty("os.version"))
                        .withTag("os_arch", System.getProperty("os.arch"));
            }
        });
        FixSSL.addLetsEncryptCertSupportIfNeeded();
    }

    static Bootstrap createBootstrap() {
        log("Starting bootstrap...");

        Bootstrap bootstrap = new Bootstrap();
        LocalBootstrapMeta localBootstrapMeta = bootstrap.getMeta();

        log("Version: " + localBootstrapMeta.getVersion());

        /*log("Starting bootstrap...");



        Bootstrap bootstrap = new Bootstrap(!parsed.has(forceHeadlessMode));
        LocalBootstrapMeta localBootstrapMeta = bootstrap.getMeta();
        log("Version: " + localBootstrapMeta.getVersion());*/

        File
            defaultFile = U.requireNotNull(LocalLauncher.getDefaultFileLocation(localBootstrapMeta.getShortBrand()), "defaultFileLocation"),
            defaultLibFolder = defaultFile.getParentFile() == null? new File("lib") : new File(defaultFile.getParentFile(), "lib");

        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<File> targetFileParser =
                parser.accepts("targetJar", "points to the targetJar").withRequiredArg().withValuesConvertedBy(new FileValueConverter());
        ArgumentAcceptingOptionSpec<File> targetLibFolderParser =
                parser.accepts("targetLibFolder", "points to the library folder").withRequiredArg().withValuesConvertedBy(new FileValueConverter());
        ArgumentAcceptingOptionSpec<String> brandParser =
                parser.accepts("brand", "defines brand name").withRequiredArg().ofType(String.class).defaultsTo(U.requireNotNull(localBootstrapMeta.getShortBrand(), "default shortBrand"));
        OptionSpecBuilder forceUpdateParser =
                parser.accepts("ignoreUpdate", "defines if bootstrap should ignore launcher update processes");
        OptionSpecBuilder ignoreSelfUpdateParser =
                parser.accepts("ignoreSelfUpdate", "defines if bootstrap should ignore self update processes");
        OptionSpecBuilder forceHeadlessMode =
                parser.accepts("headlessMode", "defines if bootstrap should run without UI");
        OptionSpecBuilder packageMode =
                parser.accepts("packageMode", "defines if bootstrap runs inside a package");
        ArgumentAcceptingOptionSpec<File> targetUpdateFile =
                parser.accepts("updateMetaFile", "points to update meta file").withRequiredArg().withValuesConvertedBy(new FileValueConverter());

        OptionSet parsed = parseJvmArgs(parser);

        bootstrap.setupUserInterface(parsed.has(forceHeadlessMode));

        localBootstrapMeta.setShortBrand(U.requireNotNull(brandParser.value(parsed), "shortBrand"));
        log("Short brand: ", localBootstrapMeta.getShortBrand());

        File targetJar = targetFileParser.value(parsed);  // can be null
        if (targetJar == null)
            targetJar = U.requireNotNull(LocalLauncher.getDefaultFileLocation(localBootstrapMeta.getShortBrand()), "defaultFileLocation");
        bootstrap.setTargetJar(targetJar);
        log("Target jar:", bootstrap.getTargetJar());

        File targetLibFolder = targetLibFolderParser.value(parsed); // can be null
        if (targetLibFolder == null)
            targetLibFolder = (bootstrap.getTargetJar().getParentFile() == null) ? new File("lib") : new File(bootstrap.getTargetJar().getParentFile().getParentFile(), "lib");
        bootstrap.setTargetLibFolder(targetLibFolder);
        log("Target lib folder:", bootstrap.getTargetLibFolder());

        bootstrap.setUpdateMetaFile(targetUpdateFile.value(parsed));
        log("Update meta file:", bootstrap.getUpdateMetaFile());

        bootstrap.setIgnoreUpdate(parsed.has(forceUpdateParser));
        log("Ignore launcher update:", bootstrap.getIgnoreUpdate());

        bootstrap.setIgnoreSelfUpdate(parsed.has(ignoreSelfUpdateParser));
        log("Ignore self update:", bootstrap.getIgnoreSelfUpdate());

        bootstrap.setPackageMode(parsed.has(packageMode));
        log("Package mode:", bootstrap.getPackageMode());

        if (bootstrap.getPackageMode()) {
            bootstrap.setIgnoreSelfUpdate(true);
            bootstrap.setIgnoreUpdate(true);
            log("Package mode: Ignore self update set to", bootstrap.getIgnoreSelfUpdate());
            log("Package mode: Ignore update set to", bootstrap.getIgnoreUpdate());
        }

        try {
            checkAccessible(bootstrap.getTargetJar(), false, !bootstrap.getPackageMode());
        } catch (IOException e) {
            throw new RuntimeException("error checking target jar: " + bootstrap.getTargetJar().getAbsolutePath(), e);
        }

        try {
            checkAccessible(bootstrap.getTargetLibFolder(), false, !bootstrap.getPackageMode());
        } catch (IOException e) {
            throw new RuntimeException("error checking target lib folder: " + bootstrap.getTargetLibFolder().getAbsolutePath(), e);
        }

        return bootstrap;
    }

    private static OptionSet parseJvmArgs(OptionParser parser) {
        List<String> jvmArgs = new ArrayList<String>();

        for(String key : parser.recognizedOptions().keySet()) {
            String value = System.getProperty("tlauncher.bootstrap." + key);
            if(value != null) {
                jvmArgs.add("--" + key);
                jvmArgs.add(value);
                log("Found JVM arg: ", key, " ", value);
            }
        }

        return parser.parse(U.toArray(jvmArgs, String.class));
    }

    public static void main(String[] args) {
        checkRunningConditions();

        System.setOut(out = OutputRedirectBuffer.createRedirect(System.out));
        System.setErr(err = OutputRedirectBuffer.createRedirect(System.err));

        Bootstrap bootstrap = null;
        Ref<BootBridge> bootBridgeRef = new Ref<BootBridge>();

        try {
            bootstrap = createBootstrap();
            bootstrap.defTask(args, bootBridgeRef).call();
        } catch(TaskInterruptedException interrupted) {
            log("Default task was interrupted");
        } catch(InterruptedException interrupted) {
            log("Interrupted");
        } catch (Exception e) {
            e.printStackTrace();
            handleFatalError(bootstrap, bootBridgeRef.getObject(), e, true);
            System.exit(-1);
        }
        System.exit(0);
    }

    static void handleFatalError(Bootstrap bootstrap, BootBridge bridge, Throwable e, boolean sendSentry) {
        FatalExceptionType exceptionType = FatalExceptionType.getType(e);

        if(sendSentry) {
            EventBuilder b = new EventBuilder()
                    .withMessage("fatal error")
                    .withLevel(Event.Level.FATAL)
                    .withSentryInterface(new ExceptionInterface(e))
                    .withTag("type", exceptionType.name());
            avList:
            {
                if(!OS.WINDOWS.isCurrent()) {
                    break avList;
                }
                List<String> avList;
                try {
                    avList = WMI.getAVSoftwareList();
                } catch (Exception e0) {
                    log("Could not get AV list", e0);
                    break avList;
                }
                b.withExtra("avList", avList);
            }
            if(bridge != null && bridge.getClient() != null) {
                SENTRY.getContext().setUser(new User(
                        bridge.getClient().toString(),
                        bridge.getClient().toString(),
                        null,
                        null
                ));
            }
            SENTRY.sendEvent(b);
        }

        if(bootstrap != null) {
            bootstrap.getUserInterface().dispose();
        }

        UserInterface.showFatalError(exceptionType,
                bridge == null || bridge.getClient() == null? null : bridge.getClient().toString());
    }

    private final InternalLauncher internal;
    private final LocalBootstrapMeta meta;

    private IInterface ui;
    private File targetJar, targetLibFolder, targetUpdateFile, updateMetaFile;
    private boolean ignoreUpdate, ignoreSelfUpdate, packageMode;

    Bootstrap(File targetJar, File targetLibFolder) {
        final String resourceName = "meta.json";
        try {
            meta = Json.parse(U.requireNotNull(getClass().getResourceAsStream(resourceName), resourceName), LocalBootstrapMeta.class);
        } catch (Exception e) {
            throw new Error("could not load meta", e);
        }

        SENTRY.addBuilderHelper(new EventBuilderHelper() {
            @Override
            public void helpBuildingEvent(EventBuilder eventBuilder) {
                eventBuilder
                        .withRelease(meta.getVersion().getNormalVersion())
                        .withEnvironment(meta.getShortBrand());
            }
        });

        InternalLauncher internal;
        try {
            internal = new InternalLauncher();
        } catch (LauncherNotFoundException e) {
            log("Internal launcher is not located in the classpath");
            internal = null;
        }
        this.internal = internal;

        setTargetJar(targetJar);
        setTargetLibFolder(targetLibFolder);
    }

    public Bootstrap() {
        this(null, null);
    }

    public void setupUserInterface(boolean forceHeadlessMode) {
        if(ui != null) {
            return;
        }
        log("Setting up user interface");
        loadSwing:
        {
            if(forceHeadlessMode) {
                log("Forcing headless mode");
                break loadSwing;
            }

            log("Trying to load user interface");
            try {
                ui = new UserInterface();
            } catch (RuntimeException rE) {
                log("User interface is not loaded:", rE);
                break loadSwing;
            }
            log("UI loaded");
            return;
        }
        ui = new HeadlessInterface();
        log("Headless mode loaded");
    }

    IInterface getUserInterface() {
        return ui;
    }

    public File getTargetJar() {
        return targetJar;
    }

    private void setTargetJar(File file) {
        this.targetJar = file;
    }

    public File getTargetLibFolder() {
        return targetLibFolder;
    }

    private void setTargetLibFolder(File targetLibFolder) {
        this.targetLibFolder = targetLibFolder;
    }

    public boolean getIgnoreUpdate() {
        return ignoreUpdate;
    }

    private void setIgnoreUpdate(boolean ignore) {
        this.ignoreUpdate = ignore;
    }

    public boolean getIgnoreSelfUpdate() {
        return ignoreSelfUpdate;
    }

    public void setIgnoreSelfUpdate(boolean ignoreSelfUpdate) {
        this.ignoreSelfUpdate = ignoreSelfUpdate;
    }

    public boolean getPackageMode() { return packageMode; }

    public void setPackageMode(boolean packageMode) {
        this.packageMode = packageMode;
    }

    public File getTargetUpdateFile() {
        return targetUpdateFile;
    }

    private void setTargetUpdateFile(File targetUpdateFile) {
        this.targetUpdateFile = targetUpdateFile;
    }

    public File getUpdateMetaFile() {
        return updateMetaFile;
    }

    private void setUpdateMetaFile(File updateMetaFile) {
        this.updateMetaFile = updateMetaFile;
    }

    public LocalBootstrapMeta getMeta() {
        return meta;
    }

    DownloadEntry getBootstrapUpdate(UpdateMeta updateMeta) {
        RemoteBootstrapMeta remoteMeta = U.requireNotNull(updateMeta, "updateMeta").getBootstrap();

        U.requireNotNull(remoteMeta, "RemoteBootstrap meta");
        U.requireNotNull(remoteMeta.getDownload(), "RemoteBootstrap download URL");

        log("Local bootstrap version: " + meta.getVersion());
        log("Remote bootstrap version: " + remoteMeta.getVersion());

        if(meta.getVersion().greaterThan(remoteMeta.getVersion())) {
            log("Local bootstrap version is newer than remote one");
            return null;
        }

        String localBootstrapChecksum;
        try {
            localBootstrapChecksum = U.getSHA256(U.getJar(Bootstrap.class));
        } catch (Exception e) {
            log("Could not get local bootstrap checksum", e);
            return null;
        }

        log("Current package: " + PackageType.CURRENT);
        log("Remote bootstrap checksum of selected package: " + remoteMeta.getDownload(PackageType.CURRENT));

        log("Local bootstrap checksum: " + localBootstrapChecksum);
        log("Remote bootstrap checksum: " + remoteMeta.getDownload(PackageType.CURRENT).getChecksum());

        if (localBootstrapChecksum.equalsIgnoreCase(remoteMeta.getDownload().getChecksum())) {
            return null;
        }

        return remoteMeta.getDownload();
    }

    TaskList downloadLibraries(LocalLauncherMeta localLauncherMeta) {
        TaskList taskList = new TaskList("downloadLibraries", 4);
        File libDir = getTargetLibFolder();

        for (Library library : localLauncherMeta.getLibraries()) {
            taskList.submit(library.download(libDir));
        }

        return taskList;
    }

    private BootBridge bootBridge;

    private BootBridge createBridge(String[] args, String options) {
        BootBridge bridge = BootBridge.create(meta.getVersion().toString(), args, options);
        bridge.addListener(new BootListenerAdapter() {
            @Override
            public void onBootSucceeded() {
                disableRedirectRecording();
            }
        });
        List<String> argsList = new ArrayList<String>();
        Collections.addAll(argsList, args);
        return bridge;
    }

    Task<LocalLauncherTask> prepareLauncher(final UpdateMeta updateMeta) {
        return new Task<LocalLauncherTask>("prepareLauncher") {
            @Override
            protected LocalLauncherTask execute() throws Exception {
                RemoteLauncher remoteLauncher = updateMeta == null? null : new RemoteLauncher(updateMeta.getLauncher());
                log("Remote launcher: " + remoteLauncher);

                LocalLauncherTask localLauncherTask = bindTo(getLocalLauncher(remoteLauncher), .0, .25);
                LocalLauncher localLauncher = localLauncherTask.getLauncher();
                LocalLauncherMeta localLauncherMeta = localLauncher.getMeta();
                log("Local launcher: " + localLauncher);
                printVersion(localLauncherMeta);

                log("Downloading libraries...");
                bindTo(downloadLibraries(localLauncherMeta), .25, 1.);

                return localLauncherTask;
            }
        };
    }

    Task<Void> startLauncher(final LocalLauncher localLauncher, final BootBridge bridge) {
        return new Task<Void>("startLauncher") {
            @Override
            protected Void execute() throws Exception {
                log("Starting launcher...");

                bridge.addListener(new BootListenerAdapter() {
                    @Override
                    public void onBootStateChanged(String stepName, double percentage) {
                        updateProgress(percentage);
                    }
                });

                return bindTo(ClassLoaderStarter.start(localLauncher, bridge), 0., 1.);
            }
        };
    }

    private Task<Void> defTask(final String[] args, final Ref<BootBridge> bootBridgeRef) {
        return new Task<Void>("defTask") {
            {
                if(ui != null) {
                    ui.bindToTask(this);
                }
            }

            @Override
            protected Void execute() throws Exception {
                printVersion(null);
                UpdateMeta updateMeta;

                try {
                    if(updateMetaFile != null) {
                        Compressor.init();
                        SignedStream signedStream = null;
                        try {
                            signedStream = new SignedStream(new FileInputStream(updateMetaFile));
                            updateMeta = UpdateMeta.fetchFrom(Compressor.uncompressMarked(signedStream, false));
                            signedStream.validateSignature();
                        } finally {
                            U.close(signedStream);
                        }
                    } else {
                        updateMeta = bindTo(UpdateMeta.fetchFor(meta.getShortBrand()), .0, .25);
                    }
                } catch(ExceptionList list) {
                    log("Could not retrieve update meta:", list);
                    SENTRY.sendEvent(new EventBuilder()
                            .withLevel(Event.Level.ERROR)
                            .withMessage("update meta not available")
                            .withSentryInterface(new ExceptionInterface(list))
                    );
                    updateMeta = null;
                }

                if(updateMeta != null) {
                    DownloadEntry downloadEntry = getBootstrapUpdate(updateMeta);
                    if(downloadEntry != null) {
                        if(getIgnoreSelfUpdate()) {
                            log("Bootstrap self update ignored:", updateMeta.getBootstrap().getVersion());
                        } else {
                            Updater updater = new Updater("bootstrapUpdate",
                                    U.getJar(Bootstrap.class), downloadEntry, true);
                            bindTo(updater, .25, 1.);
                            return null;
                        }
                    }
                }


                LocalLauncherTask localLauncherTask = bindTo(prepareLauncher(updateMeta), .25, .75);
                LocalLauncher localLauncher = localLauncherTask.getLauncher();

                BootBridge bridge = createBridge(args, updateMeta == null? null : updateMeta.getOptions());
                bootBridge = bridge;
                if(localLauncherTask.isUpdated() && updateMeta != null) {
                    addUpdateMessage(bridge, updateMeta.getLauncher());
                }
                bootBridgeRef.setObject(bridge);

                bindTo(startLauncher(localLauncher, bridge), 0.75, 1.);

                checkInterrupted();

                log("Idle state: Waiting for launcher the close");
                bridge.waitUntilClose();

                return null;
            }
        };
    }

    private void addUpdateMessage(BootBridge bridge, RemoteLauncherMeta remoteLauncherMeta) {
        Map<String, String> description = remoteLauncherMeta.getDescription();
        if(description == null) {
            return;
        }
        String updateTitle = UserInterface.getLString("update.launcher.title", "Launcher was updated");
        for(Map.Entry<String, String> entry : description.entrySet()) {
            bridge.addMessage(entry.getKey(), updateTitle, entry.getValue());
        }
    }

    private void printVersion(LocalLauncherMeta localLauncherMeta) {
        HeadlessInterface.printVersion(getMeta().getVersion().toString(), localLauncherMeta == null? null : localLauncherMeta.getVersion().toString());
    }

    private Task<LocalLauncherTask> getLocalLauncher(final RemoteLauncher remote) {
        return new Task<LocalLauncherTask>("getLocalLauncher") {
            @Override
            protected LocalLauncherTask execute() throws Exception {
                updateProgress(0.);
                log("Getting local launcher...");

                RemoteLauncherMeta remoteLauncherMeta = remote == null? null : U.requireNotNull(remote.getMeta(), "RemoteLauncherMeta");

                LocalLauncher local;
                try {
                    local = new LocalLauncher(getTargetJar(), getTargetLibFolder());
                } catch (LauncherNotFoundException lnfE) {
                    log("Could not find local launcher:", lnfE);

                    if (internal == null) {
                        local = null;
                    } else {
                        log("... replacing it with internal one:", internal);
                        local = bindTo(internal.toLocalLauncher(getTargetJar(), getTargetLibFolder()), .0, .1);
                    }
                }

                File file = local != null? local.getFile() : getTargetJar();

                if(local != null) {
                    if(remote == null) {
                        log("We have local launcher, but have no remote.");
                        return new LocalLauncherTask(local);
                    }

                    LocalLauncherMeta localLauncherMeta;

                    try {
                        localLauncherMeta = U.requireNotNull(local.getMeta(), "LocalLauncherMeta");
                    } catch (IOException ioE) {
                        log("Could not get local launcher meta:", ioE);
                        localLauncherMeta = null;
                    }

                    updateProgress(.2);

                    replaceSelect:
                    {
                        if (localLauncherMeta == null) {
                            break replaceSelect;
                        }

                        U.requireNotNull(localLauncherMeta.getShortBrand(), "LocalLauncher shortBrand");
                        U.requireNotNull(localLauncherMeta.getBrand(), "LocalLauncher brand");
                        U.requireNotNull(localLauncherMeta.getMainClass(), "LocalLauncher mainClass");

                        if(!localLauncherMeta.getVersion().equals(remote.getMeta().getVersion())) {
                            log("Local version doesn't match remote");
                            if(getIgnoreUpdate()) {
                                log("... nevermind");
                            } else {
                                break replaceSelect;
                            }
                        } else if(!getIgnoreUpdate()) {
                            String localLauncherHash = U.getSHA256(local.getFile());
                            log("Local SHA256: " + localLauncherHash);
                            log("Remote SHA256: " + remoteLauncherMeta.getChecksum());

                            if (!localLauncherHash.equalsIgnoreCase(remoteLauncherMeta.getChecksum())) {
                                log("... local SHA256 checksum is not the same as remote");
                                break replaceSelect;
                            }

                            log("All done, local launcher is up to date.");
                        }

                        return new LocalLauncherTask(local);
                    }

                    updateProgress(.5);
                }

                if(remote == null) {
                    throw new LauncherNotFoundException("could not retrieve any launcher");
                }

                LocalLauncher fromRemote;
                try {
                    fromRemote = bindTo(remote.toLocalLauncher(file, getTargetLibFolder()), .5, 1.);
                } catch(IOException ioE) {
                    if(local == null) {
                        throw ioE;
                    }
                    SENTRY.sendEvent(new EventBuilder()
                            .withLevel(Event.Level.ERROR)
                            .withMessage("couldn't download remote launcher")
                            .withSentryInterface(new ExceptionInterface(ioE))
                    );
                    return new LocalLauncherTask(local);
                }

                return new LocalLauncherTask(fromRemote, true);
            }
        };
    }

    private static RedirectPrintStream out, err;

    private static void disableRedirectRecording() {
        if (out != null) {
            out.disableRecording();
        }
        if (err != null) {
            err.disableRecording();
        }
        OutputRedirectBuffer.clearBuffer();
    }

    private static void checkRunningConditions() {
        JavaVersion supported = JavaVersion.create(1, 8, 0, 45);
        JavaVersion current = JavaVersion.getCurrent();

        if(current == JavaVersion.UNKNOWN) {
            SENTRY.sendEvent(new EventBuilder()
                    .withLevel(Event.Level.WARNING)
                    .withMessage("unknown java version: " + System.getProperty("java.version"))
            );
        } else if(JavaVersion.getCurrent().compareTo(supported) < 0) {
            SENTRY.sendEvent(new EventBuilder()
                    .withLevel(Event.Level.ERROR)
                    .withMessage("old java version")
            );
            String message =
                    "Your Java version is not supported. Please install at least " + supported.getVersion() + " from Java.com" +
                    "\n" +
                    "Ваша версия Java не поддерживается. Пожалуйста установите как минимум " + supported.getVersion() + " с сайта Java.com";
            UserInterface.showError(message, null);
            throw new Error(message);
        }

        String message = null;
        IOException ioE = null;
        File file = null;

        findProblem:
        {
            File jar = U.getJar(Bootstrap.class);
            String path = jar.getAbsolutePath();

            if (path.contains("!" + File.separatorChar)) {
                message =
                        "Please do not run (any) Java application which path contains folder name that ends with «!»" +
                                "\n" +
                                "Не запускайте Java-приложения в директориях, чей путь содержит «!». Переместите TLauncher в другую папку." +
                                "\n\n" + path;
                break findProblem;
            }

            try {
                checkAccessible(jar, false, false);
            } catch (IOException jarException) {
                file = jar;

                if (jarException instanceof UnknownFreeSpaceException) {
                    message =
                            "Could not determine free space on partition storing Bootstrap. Please check your hard drive.\n" +
                                    "\n" +
                                    "Не удалось определить свободное место. Проверьте диск на наличие ошибок.";
                } else if (jarException instanceof InsufficientFreeSpace) {
                    message =
                            "Insufficient disk space on partition storing JAR file.\n" +
                                    "\n" +
                                    "Недостаточно места на диске. с которого запускается JAR-файл. Пожалуйста, освбодите место и попробуйте снова.";
                } else {
                    message =
                            "Could not access JAR file.\n" +
                                    "\n" +
                                    "Не удалось получить доступ к JAR-файлу.";
                }
                ioE = jarException;
                break findProblem;
            }

            File tempFile;
            try {
                tempFile = File.createTempFile("bootstrap", null);
                checkAccessible(tempFile, true, true);
            } catch (IOException tempFileException) {
                if (tempFileException instanceof UnknownFreeSpaceException) {
                    message =
                            "Could not determine free space on partition storing temporary folder. Please check your hard drive.\n" +
                                    "\n" +
                                    "Не удалось определить свободное место на системном диске. Либо он полон, либо содержит ошибки.";
                } else if (tempFileException instanceof InsufficientFreeSpace) {
                    message =
                            "Insufficient disk space on partition storing temporary folder.\n" +
                                    "\n" +
                                    "Недостаточно места на системном диске. Пожалуйста, освободите место и попробуйте снова.";
                } else {
                    message =
                            "Could not access temporary folder. Please check your hard drive.\n" +
                                    "\n" +
                                    "Не удалось создать временный файл. Проверьте диск на наличие ошибок.";
                }
                ioE = tempFileException;
                break findProblem;
            }

            if(tempFile.getParentFile() != null && tempFile.isDirectory()) {
                ImageIO.setCacheDirectory(tempFile.getParentFile());
            }
        }

        if(message != null) {
            UserInterface.showError(message, file == null? null : file.getAbsolutePath());
            throw new Error(message, ioE);
        }
    }

    private static void checkAccessible(File file, boolean requireExistance, boolean requireWritePermission) throws IOException {
        if(!file.exists()) {
            if(requireExistance) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
        } else {
            NoFileAccessException.throwIfNoAccess(file, requireWritePermission);
        }

        long freeSpace = file.getFreeSpace();
        if(freeSpace != 0L && freeSpace < 1024L * 64L) {
            throw new InsufficientFreeSpace();
        }
    }

    private static void log(Object... o) {
        U.log("[Bootstrap]", o);
    }
}
