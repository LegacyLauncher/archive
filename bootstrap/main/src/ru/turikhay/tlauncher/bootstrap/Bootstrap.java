package ru.turikhay.tlauncher.bootstrap;

import ru.turikhay.tlauncher.bootstrap.bridge.BootListenerAdapter;
import ru.turikhay.tlauncher.bootstrap.exception.*;
import ru.turikhay.tlauncher.bootstrap.task.TaskInterruptedException;
import ru.turikhay.tlauncher.bootstrap.ui.UserInterface;
import ru.turikhay.tlauncher.bootstrap.util.*;
import shaded.com.getsentry.raven.DefaultRavenFactory;
import shaded.com.getsentry.raven.Raven;
import shaded.com.getsentry.raven.dsn.Dsn;
import shaded.com.getsentry.raven.event.Breadcrumb;
import shaded.com.getsentry.raven.event.BreadcrumbBuilder;
import shaded.com.getsentry.raven.event.Event;
import shaded.com.getsentry.raven.event.EventBuilder;
import shaded.com.getsentry.raven.event.interfaces.ExceptionInterface;
import ru.turikhay.tlauncher.bootstrap.launcher.*;
import ru.turikhay.tlauncher.bootstrap.util.DataBuilder;
import shaded.joptsimple.ArgumentAcceptingOptionSpec;
import shaded.joptsimple.OptionParser;
import shaded.joptsimple.OptionSet;
import shaded.joptsimple.OptionSpecBuilder;
import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.meta.*;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.task.TaskList;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.tlauncher.bootstrap.util.stream.RedirectPrintStream;
import ru.turikhay.tlauncher.bootstrap.util.FileValueConverter;
import shaded.ru.turikhay.util.windows.wmi.WMI;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public final class Bootstrap {
    private static final Raven raven = new DefaultRavenFactory().createRavenInstance(new Dsn("https://fe7b0410e04848019449cb8de9c9bc22:5c3a7bd40c9348dea0bc6858715570eb@sentry.ely.by/4?"));

    static Bootstrap createBootstrap() {
        log("Starting bootstrap...");

        Bootstrap bootstrap = new Bootstrap();
        LocalBootstrapMeta localBootstrapMeta = bootstrap.getMeta();

        log("Version: " + localBootstrapMeta.getVersion());

        File
            defaultFile = U.requireNotNull(LocalLauncher.getDefaultFileLocation(localBootstrapMeta.getShortBrand()), "defaultFileLocation"),
            defaultLibFolder = defaultFile.getParentFile() == null? new File("lib") : new File(defaultFile.getParentFile(), "lib");

        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<LaunchType> launchTypeParser =
                parser.accepts("launchType", "defines launch type").withRequiredArg().ofType(LaunchType.class).defaultsTo(U.requireNotNull(localBootstrapMeta.getLaunchType(), "default LaunchType"));
        ArgumentAcceptingOptionSpec<File> targetFileParser =
                parser.accepts("targetJar", "points to the targetJar").withRequiredArg().withValuesConvertedBy(new FileValueConverter()).defaultsTo(defaultFile);
        ArgumentAcceptingOptionSpec<File> targetLibFolderParser =
                parser.accepts("targetLibFolder", "points to the library folder").withRequiredArg().withValuesConvertedBy(new FileValueConverter()).defaultsTo(defaultLibFolder);
        ArgumentAcceptingOptionSpec<String> brandParser =
                parser.accepts("brand", "defines brand name").withRequiredArg().ofType(String.class).defaultsTo(U.requireNotNull(localBootstrapMeta.getShortBrand(), "default shortBrand"));
        OptionSpecBuilder forceUpdateParser =
                parser.accepts("forceUpdate", "defines if bootstrap should update launcher on update found");

        OptionSet parsed = parseJvmArgs(parser);

        localBootstrapMeta.setLaunchType(U.requireNotNull(launchTypeParser.value(parsed), "LaunchType"));
        log("Launch type:", localBootstrapMeta.getLaunchType());

        localBootstrapMeta.setShortBrand(U.requireNotNull(brandParser.value(parsed), "shortBrand"));
        log("Short brand: ", localBootstrapMeta.getShortBrand());

        localBootstrapMeta.setForceUpdate(parsed.has(forceUpdateParser) || localBootstrapMeta.isForceUpdate());
        log("Force update?", localBootstrapMeta.isForceUpdate());

        bootstrap.setTargetJar(targetFileParser.value(parsed));
        log("Target jar:", bootstrap.getTargetJar());

        bootstrap.setTargetLibFolder(targetLibFolderParser.value(parsed));
        log("Target lib folder:", bootstrap.getTargetLibFolder());

        try {
            checkAccessible(bootstrap.getTargetJar(), false);
        } catch (IOException e) {
            throw new RuntimeException("error checking target jar: " + bootstrap.getTargetJar().getAbsolutePath(), e);
        }

        try {
            checkAccessible(bootstrap.getTargetLibFolder(), false);
        } catch (IOException e) {
            throw new RuntimeException("error checking target lib folder: " + bootstrap.getTargetLibFolder().getAbsolutePath(), e);
        }

        recordBreadcrumb("createBootstrap", DataBuilder.create("localBootstrapMeta", localBootstrapMeta).add("targetJar", bootstrap.getTargetJar()).add("targetLibFolder", bootstrap.getTargetLibFolder()));
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

        recordBreadcrumb("parseJvmArgs", DataBuilder.create("list", jvmArgs));

        return parser.parse(U.toArray(jvmArgs, String.class));
    }

    public static void main(String[] args) {
        checkRunningConditions();

        System.setOut(out = RedirectPrintStream.newRedirectorFor(System.out));
        System.setErr(err = RedirectPrintStream.newRedirectorFor(System.err));

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
            LocalBootstrapMeta localBootstrapMeta = bootstrap == null? null : bootstrap.getMeta();
            EventBuilder b = new EventBuilder()
                    .withEnvironment(System.getProperty("os.name"))
                    .withLevel(Event.Level.FATAL)
                    .withSentryInterface(new ExceptionInterface(e))
                    .withRelease(localBootstrapMeta == null ? null : String.valueOf(localBootstrapMeta.getVersion()));

            if (exceptionType != null) {
                b.withTag("type", exceptionType.name());
            }

            if(bridge != null && bridge.getClient() != null) {
                b.withSentryInterface(new shaded.com.getsentry.raven.event.interfaces.UserInterface(
                        bridge.getClient().toString(), null, null, null
                ));
            }

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

                int count = 0;
                for(String av : avList) {
                    if("Windows Defender".equals(av)) {
                        continue;
                    }

                    if(count > 1) {
                        b.withTag("av" + String.valueOf(count), av);
                    } else {
                        b.withTag("av", av);
                    }

                    count++;
                }
            }

            raven.sendEvent(b);
        }

        if(bootstrap != null && bootstrap.getUserInterface() != null) {
            bootstrap.getUserInterface().getFrame().dispose();
        }

        if(UserInterface.getResourceBundle() != null) {
            ResourceBundle resourceBundle = UserInterface.getResourceBundle();

            final String supportLink = resourceBundle.getString("support");

            if (exceptionType == null) {
                UserInterface.showError(resourceBundle.getString("error.fatal") + "\n" + supportLink, RedirectPrintStream.getBuffer().toString());
            } else {
                StringBuilder message = new StringBuilder();
                message.append(resourceBundle.getString("error." + exceptionType.nameLowerCase()));

                if (resourceBundle.containsKey("error." + exceptionType.nameLowerCase() + "." + OS.CURRENT.nameLowerCase())) {
                    message.append(' ').append(resourceBundle.getString("error." + exceptionType.nameLowerCase() + "." + OS.CURRENT.nameLowerCase()));
                }

                message.append("\n\n");
                message.append(supportLink);

                UserInterface.showError(message.toString(), U.toString(e));
            }
        }
    }

    private final UserInterface ui;
    private final InternalLauncher internal;
    private final LocalBootstrapMeta meta;
    private File targetJar, targetLibFolder;

    Bootstrap(File targetJar, File targetLibFolder, boolean uiEnabled) {
        UserInterface userInterface = null;

        if(uiEnabled) {
            try {
                userInterface = new UserInterface();
            } catch (RuntimeException rE) {
                log("User interface is not loaded:", rE);
                userInterface = null;
            }
        }

        this.ui = userInterface;

        final String resourceName = "meta.json";
        try {
            meta = Json.parse(U.requireNotNull(getClass().getResourceAsStream(resourceName), resourceName), LocalBootstrapMeta.class);
        } catch (Exception e) {
            throw new Error("could not load meta", e);
        }

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

        recordBreadcrumb("initBootstrap", new DataBuilder().add("ui", ui).add("internalLauncher", String.valueOf(internal)).add("targetJar", targetJar).add("targetLibFolder", targetLibFolder));
    }

    public Bootstrap() {
        this(null, null, true);
    }

    UserInterface getUserInterface() {
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

    public LocalBootstrapMeta getMeta() {
        return meta;
    }

    DownloadEntry getBootstrapUpdate(UpdateMeta updateMeta) {
        RemoteBootstrapMeta remoteMeta = U.requireNotNull(updateMeta, "updateMeta").getBootstrap();

        U.requireNotNull(remoteMeta, "RemoteBootstrap meta");
        U.requireNotNull(remoteMeta.getDownload(), "RemoteBootstrap download URL");

        log("Local bootstrap version: " + meta.getVersion());
        log("Remote bootstrap version: " + remoteMeta.getVersion());

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

        recordBreadcrumb("getBootstrapUpdate", null);
        return remoteMeta.getDownload();
    }

    TaskList downloadLibraries(LocalLauncherMeta localLauncherMeta) {
        TaskList taskList = new TaskList("downloadLibraries", 4);
        File libDir = getTargetLibFolder();

        for (Library library : localLauncherMeta.getLibraries()) {
            taskList.submit(library.download(libDir));
        }

        recordBreadcrumb("downloadLibraries", DataBuilder.create("taskList", taskList));
        return taskList;
    }

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
        recordBreadcrumb("createBridge", DataBuilder.create("args", argsList.toString()).add("options", options));
        return bridge;
    }

    Task<LocalLauncherTask> prepareLauncher(final UpdateMeta updateMeta) {
        return new Task<LocalLauncherTask>("prepareLauncher") {
            @Override
            protected LocalLauncherTask execute() throws Exception {
                RemoteLauncher remoteLauncher = updateMeta == null? null : new RemoteLauncher(updateMeta.getLauncher());
                log("Remote launcher: " + remoteLauncher);
                recordBreadcrumb("remoteLauncher", DataBuilder.create("value", String.valueOf(remoteLauncher)));

                LocalLauncherTask localLauncherTask = bindTo(getLocalLauncher(remoteLauncher), .0, .25);
                LocalLauncher localLauncher = localLauncherTask.getLauncher();
                LocalLauncherMeta localLauncherMeta = localLauncher.getMeta();
                log("Local launcher: " + localLauncher);
                recordBreadcrumb("localLauncher", DataBuilder.create("value", String.valueOf(remoteLauncher)));

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
                recordBreadcrumb("startingLauncher", null);

                bridge.addListener(new BootListenerAdapter() {
                    @Override
                    public void onBootStateChanged(String stepName, double percentage) {
                        updateProgress(percentage);
                    }
                });

                return bindTo(meta.getLaunchType().getStarter().start(localLauncher, bridge), 0., 1.);
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
                UpdateMeta updateMeta;

                try {
                    updateMeta = bindTo(UpdateMeta.fetchFor(meta.getShortBrand()), .0, .25);
                } catch(ExceptionList list) {
                    log("Could not retrieve update meta:", list);
                    sendError(Event.Level.ERROR, list, DataBuilder.create("type", FatalExceptionType.getType(list)));

                    updateMeta = null;
                }

                if(updateMeta != null) {
                    DownloadEntry downloadEntry = getBootstrapUpdate(updateMeta);
                    if(downloadEntry != null) {
                        Updater updater = new Updater("bootstrapUpdate", U.getJar(Bootstrap.class), downloadEntry, true);
                        bindTo(updater, .25, 1.);
                        return null;
                    }
                }


                LocalLauncherTask localLauncherTask = bindTo(prepareLauncher(updateMeta), .25, .75);
                LocalLauncher localLauncher = localLauncherTask.getLauncher();

                BootBridge bridge = createBridge(args, updateMeta == null? null : updateMeta.getOptions());
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
                        log("... and we have no internal one?");
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

                        String localLauncherHash = U.getSHA256(local.getFile());
                        log("Local SHA256: " + localLauncherHash);
                        log("Remote SHA256: " + remoteLauncherMeta.getChecksum());

                        if (!localLauncherHash.equalsIgnoreCase(remoteLauncherMeta.getChecksum())) {
                            log("... local SHA256 checksum is not the same as remote");
                            break replaceSelect;
                        }

                        log("All done, local launcher is up to date.");

                        return new LocalLauncherTask(local);
                    }

                    updateProgress(.5);
                }

                if(remote == null) {
                    throw new LauncherNotFoundException("could not retrieve any launcher");
                }

                local = bindTo(remote.toLocalLauncher(file, getTargetLibFolder()), .5, 1.);
                return new LocalLauncherTask(local, true);
            }
        };
    }

    public static void recordBreadcrumbError(Class<?> clazz, String name, Throwable t, DataBuilder b) {
        recordBreadcrumb(name, "error", "class:" + clazz.getSimpleName(), b.add("exception", U.toString(t)));
    }

    public static void recordBreadcrumb(Class<?> clazz, String name, DataBuilder data) {
        recordBreadcrumb(name, "info", "class:" + clazz.getSimpleName(), data);
    }

    private static void recordBreadcrumb(String name, DataBuilder data) {
        recordBreadcrumb(name, "info", "general", data);
    }

    private void sendError(Event.Level level, Throwable t, DataBuilder b) {
        EventBuilder builder = new EventBuilder()
                .withEnvironment(System.getProperty("os.name"))
                .withLevel(level)
                .withSentryInterface(new ExceptionInterface(t))
                .withRelease(String.valueOf(getMeta().getVersion()))
                .withServerName(JavaVersion.getCurrent().getVersion());
        if(b != null) {
            for(Map.Entry<String, String> entry : b.build().entrySet()) {
                builder.withExtra(entry.getKey(), entry.getValue());
            }
        }

        Event event = builder.build();
        raven.sendEvent(event);

        log("Error sent:", DataBuilder.create("error", event)
            .add("environment", event.getEnvironment())
            .add("level", event.getLevel())
            .add("exception", t)
            .add("extra", b == null? null : b.build())
            .build()
        );
    }

    private static void recordBreadcrumb(String name, String level, String category, DataBuilder data) {
        BreadcrumbBuilder b = new BreadcrumbBuilder();
        b.setLevel(level);
        b.setCategory(category);
        b.setMessage(name);
        if(data != null) {
            b.setData(data.build());
        }

        Breadcrumb breadcrumb = b.build();
        raven.getContext().recordBreadcrumb(breadcrumb);

        log("Breadcrumb recorded:", DataBuilder.create("breadcrumb", breadcrumb)
            .add("level", level)
            .add("category", category)
            .add("message", name)
            .add("data", data == null? null : data.build())
            .build()
        );
    }

    private static RedirectPrintStream.Redirector out, err;

    private static void disableRedirectRecording() {
        if (out != null) {
            out.disableRecording();
        }
        if (err != null) {
            err.disableRecording();
        }
        recordBreadcrumb("disableRedirectRecording", null);
    }

    private static void checkRunningConditions() {
        JavaVersion supported = JavaVersion.create(1, 7, 0, 25);

        if(JavaVersion.getCurrent().compareTo(supported) == -1) {
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
                checkAccessible(jar, false);
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
                checkAccessible(tempFile, true);
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

    private static void checkAccessible(File file, boolean requireExistance) throws IOException {
        if(!file.exists()) {
            if(requireExistance) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
        } else {
            NoFileAccessException.throwIfNoAccess(file);
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
