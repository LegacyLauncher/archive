package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.*;
import com.moandjiezana.toml.Toml;
import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.options.OptionsFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.ConfigurationDefaults;
import ru.turikhay.util.sysinfo.*;
import ru.turikhay.tlauncher.minecraft.launcher.ChildProcessLogger;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.util.Compressor;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.Time;
import ru.turikhay.util.async.ExtendedThread;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class CrashManager {
    private static final Logger LOGGER = LogManager.getLogger(CrashManager.class);
    private static final Marker LOG_FLUSHER = MarkerManager.getMarker("log_flusher");

    private final ArrayList<CrashManagerListener> listeners = new ArrayList<>();
    private final Watchdog watchdog = new Watchdog();

    private final Gson gson;
    private final Crash crash;

    private final MinecraftLauncher launcher;
    private final String version;
    private final ChildProcessLogger processLogger;
    private final Charset charset;
    private final int exitCode;

    private final CrashEntryList.ListDeserializer listDeserializer;

    private final Map<String, IEntry> crashEntries = new LinkedHashMap<>();
    private final Map<String, BindableAction> actionsMap = new HashMap<>();
    private List<String> skipFolders = new ArrayList<>();

    private final SystemInfoReporter systemInfoReporter = initSystemInfoPrinter();

    private final Entry
            generatedFilesSeekerEntry = new GeneratedFilesSeeker(),
            crashDescriptionSeeker = new CrashDescriptionSeeker(),
            logFlusherEntry = new LogFlusherEntry();

    private final List<String> modVersionsFilter = Arrays.asList("forge", "fabric", "rift", "liteloader");

    private void setupActions() {
        actionsMap.clear();

        addAction(new BrowseAction(launcher == null ? new File("") : launcher.getGameDir()));
        addAction(new SetAction());
        addAction(new GuiAction());
        addAction(new ExitAction());
        addAction(new SetOptionAction(launcher == null ? new OptionsFile(new File("test.txt")) : launcher.getOptionsFile()));
        addAction(new ForceUpdateAction());
    }

    SystemInfoReporter getSystemInfoReporter() {
        return systemInfoReporter;
    }

    private void setupEntries() {
        crashEntries.clear();

        addEntry(new Java16Entry(this));
        addEntry(generatedFilesSeekerEntry);
        addEntry(crashDescriptionSeeker);
        addEntry(new ErroredModListAnalyzer());

        CrashEntryList internal = buildInternalSignatures();
        CrashEntryList external = buildExternalSignatures(internal);

        if (external == null) {
            addAllEntries(internal, "internal");
            skipFolders = internal.getSkipFolders();
        } else {
            addAllEntries(external, "external");
            skipFolders = external.getSkipFolders();

            LOGGER.info("Using external entries, because their revision ({}) is newer than the revision " +
                    "of the internal ones ({})", external.getRevision(), internal.getRevision());
        }

        addEntry(new GraphicsEntry(this));
        addEntry(new BadMainClassEntry(this));
        addEntry(logFlusherEntry);
    }

    private SystemInfoReporter initSystemInfoPrinter() {
        Optional<SystemInfoReporter> oshi = OSHISystemInfoReporter.createIfAvailable();
        if (OS.WINDOWS.isCurrent()) {
            DxDiagSystemInfoReporter dxDiag = new DxDiagSystemInfoReporter();
            return oshi.isPresent() ? new SequentialSystemInfoReporter(oshi.get(), dxDiag) : dxDiag;
        }
        return oshi.orElseGet(NoopSystemInfoReporter::new);
    }

    @Nonnull
    private CrashEntryList buildInternalSignatures() {
        try {
            return loadEntries(getClass().getResourceAsStream("signature.json"), "internal");
        } catch (Exception e) {
            throw new RuntimeException("could not load local signatures", e);
        }
    }

    @Nullable
    private CrashEntryList buildExternalSignatures(CrashEntryList internal) {
        CrashEntryList external;
        try {
            external = loadEntries(Compressor.uncompressMarked(Repository.EXTRA_VERSION_REPO.get("libraries/signature.json")), "external");
        } catch (Exception e) {
            LOGGER.warn("Could not load external entries", e);
            Sentry.capture(new EventBuilder()
                    .withLevel(Event.Level.WARNING)
                    .withMessage("cannot load external crash entries")
                    .withSentryInterface(new ExceptionInterface(e))
            );
            return null;
        }

        if (external.getRevision() <= internal.getRevision()) {
            LOGGER.info("External signatures are older or the same: {}", external.getRevision());
            return null;
        }

        return external;
    }

    private CrashManager(MinecraftLauncher launcher, String version,
                         ChildProcessLogger processLogger, Charset charset, int exitCode) {
        this.launcher = launcher;
        this.version = version;
        this.processLogger = processLogger;
        this.charset = Objects.requireNonNull(charset, "charset");
        this.exitCode = exitCode;

        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
                .registerTypeAdapter(CrashEntryList.class, listDeserializer = new CrashEntryList.ListDeserializer(this))
                .create();

        crash = new Crash(this);
    }

    public CrashManager(MinecraftLauncher launcher) {
        this(launcher, launcher.getVersion(), launcher.getProcessLogger(),
                launcher.getCharset(), launcher.getExitCode());
    }

    public CrashManager(String version, ChildProcessLogger processLogger,
                        Charset charset, int exitCode) {
        this(null, version, processLogger, charset, exitCode);
    }

    public void startAndJoin() {
        synchronized (watchdog) {
            checkWorking();
            watchdog.unlockThread("start");

            try {
                watchdog.join();
            } catch (InterruptedException e) {
                LOGGER.debug("Thread was interrupted", e);
            }
        }
    }

    private volatile boolean cancelled;

    public void cancel() {
        cancelled = true;
        if (watchdog.executor != null) {
            watchdog.executor.interrupt();
        }
    }

    private void addAction(BindableAction action) {
        actionsMap.put(Objects.requireNonNull(action).getName(), action);
    }

    private <T extends IEntry> T addEntry(T entry) {
        if (crashEntries.containsKey(entry.getName())) {
            LOGGER.trace("Removing {}", crashEntries.get(entry.getName()));
        }
        crashEntries.put(entry.getName(), entry);
        return entry;
    }

    private CrashEntry addEntry(String name) {
        return addEntry(new CrashEntry(this, name));
    }

    private PatternEntry addEntry(String name, boolean fake, Pattern pattern) {
        PatternEntry entry = new PatternEntry(this, name, pattern);
        entry.setFake(fake);
        return addEntry(entry);
    }

    private PatternEntry addEntry(String name, Pattern pattern) {
        return addEntry(name, false, pattern);
    }

    private void addAllEntries(CrashEntryList entryList, String type) {
        for (CrashEntry entry : entryList.getSignatures()) {
            //log("Processing", type, "entry:", entry);
            addEntry(entry);
        }
    }

    private CrashEntryList loadEntries(InputStream input, String type) throws Exception {
        LOGGER.trace("Loading {} entries...", type);

        try (Reader reader = new InputStreamReader(input, FileUtil.DEFAULT_CHARSET)) {
            return gson.fromJson(reader, CrashEntryList.class);
        }
    }

    public MinecraftLauncher getLauncher() {
        return launcher;
    }

    public String getVersion() {
        return version;
    }

    public ChildProcessLogger getProcessLogger() {
        return Objects.requireNonNull(processLogger, "processLogger");
    }

    public boolean hasProcessLogger() {
        return processLogger != null;
    }

    public int getExitCode() {
        return exitCode;
    }

    public Crash getCrash() {
        if (Thread.currentThread() != watchdog && Thread.currentThread() != watchdog.executor) {
            checkAlive();
        }
        synchronized (watchdog) {
            return crash;
        }
    }

    BindableAction getAction(String name) {
        return actionsMap.get(name);
    }

    String getVar(String key) {
        return listDeserializer.getVars().get(key);
    }

    Button getButton(String name) {
        return listDeserializer.getButtons().get(name);
    }

    public Exception getError() {
        checkAlive();
        return watchdog.executor.error;
    }

    public void addListener(CrashManagerListener listener) {
        checkWorking();
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private void checkAlive() {
        if (watchdog.isAlive()) {
            throw new IllegalStateException("thread is alive");
        }
    }

    private void checkWorking() {
        if (watchdog.isWorking()) {
            throw new IllegalStateException("thread is working");
        }
    }

    private Scanner getCrashFileScanner() throws IOException {
        File crashFile = crash.getCrashFile();
        if (crashFile == null || !crashFile.isFile()) {
            LOGGER.info("Crash report file doesn't exist. May be looking into logs?");
            return PatternEntry.getScanner(getProcessLogger());
        } else {
            LOGGER.info("Crash report file exist. We'll scan it.");
            return new Scanner(new InputStreamReader(new FileInputStream(crashFile), FileUtil.getCharset()));
        }
    }

    private class Watchdog extends ExtendedThread {
        private final Executor executor;

        Watchdog() {
            executor = new Executor();
            startAndWait();
        }

        boolean isWorking() {
            return isAlive() && !isThreadLocked();
        }

        @Override
        public void run() {
            lockThread("start");

            for (CrashManagerListener listener : listeners) {
                listener.onCrashManagerProcessing(CrashManager.this);
            }

            try {
                watchExecutor();
            } catch (CrashManagerInterrupted interrupted) {
                for (CrashManagerListener listener : listeners) {
                    listener.onCrashManagerCancelled(CrashManager.this);
                }
                return;
            } catch (Exception e) {
                Sentry.capture(new EventBuilder()
                        .withMessage("crash manager crashed")
                        .withSentryInterface(new ExceptionInterface(e))
                        .withExtra("crash", crash)
                );
                for (CrashManagerListener listener : listeners) {
                    listener.onCrashManagerFailed(CrashManager.this, e);
                }
                return;
            }
            for (CrashManagerListener listener : listeners) {
                listener.onCrashManagerComplete(CrashManager.this, crash);
            }
        }

        private void watchExecutor() throws Exception {
            executor.unlockThread("start");
            try {
                executor.join();
            } catch (InterruptedException e) {
                throw new CrashManagerInterrupted(e);
            }

            if (executor.error != null) {
                throw executor.error;
            }
        }
    }

    private class Executor extends ExtendedThread {
        private Exception error;

        Executor() {
            startAndWait();
        }

        private void scan() throws CrashManagerInterrupted, CrashEntryException {
            if (processLogger == null) {
                LOGGER.warn("Process logger not found. Assuming it is unknown crash");
                return;
            }

            Object timer = Time.start(new Object());

            setupActions();
            setupEntries();

            systemInfoReporter.queueReport();

            CrashEntry capableEntry = null;

            for (IEntry entry : crashEntries.values()) {
                if (cancelled) {
                    throw new CrashManagerInterrupted();
                }

                if (capableEntry != null && capableEntry.isFake()) {
                    break;
                }

                if (capableEntry == null && entry instanceof CrashEntry) {
                    //log("Checking entry:", entry.getName());
                    boolean capable;

                    try {
                        capable = ((CrashEntry) entry).checkCapability();
                    } catch (Exception e) {
                        throw new CrashEntryException(entry, e);
                    }

                    if (capable) {
                        capableEntry = (CrashEntry) entry;

                        LOGGER.info("Found relevant: {}", capableEntry.getName());
                        crash.setEntry(capableEntry);

                        if (capableEntry.isFake()) {
                            LOGGER.info("It is a \"fake\" crash, skipping remaining...");
                        }
                    }
                } else if (entry instanceof Entry) {
                    if (capableEntry != null) {
                        if (!((Entry) entry).isCapable(capableEntry)) {
                            LOGGER.trace("Skipping: {}", entry.getName());
                            continue;
                        }
                    }
                    LOGGER.trace("Executing: {}", entry.getName());
                    try {
                        ((Entry) entry).execute();
                    } catch (Exception e) {
                        throw new CrashEntryException(entry, e);
                    }
                }
            }

            LOGGER.info("Done in {} ms", Time.stop(timer));
        }

        @Override
        public void run() {
            lockThread("start");
            try {
                scan();
            } catch (Exception e) {
                LOGGER.error("Error", e);
                error = e;
            }
        }
    }

    private static class CrashManagerInterrupted extends Exception {
        CrashManagerInterrupted() {
        }

        CrashManagerInterrupted(Throwable cause) {
            super(cause);
        }
    }

    private static class CrashEntryException extends Exception {
        CrashEntryException(IEntry entry, Throwable cause) {
            super(entry.toString(), cause);
        }
    }

    private static class ErroredMod {
        private final String name, fileName;

        ErroredMod(String name, String fileName) {
            this.name = name;
            this.fileName = fileName;
        }

        ErroredMod(Matcher matcher) {
            this(matcher.group(2), matcher.group(3));
        }

        public String toString() {
            return name;
        }

        void append(StringBuilder b) {
            b.append(name).append(" (").append(fileName).append(")");
        }
    }

    private class ErroredModListAnalyzer extends CrashEntry {
        private final Pattern modPattern;
        private final ArrayList<Pattern> patterns = new ArrayList<>();

        ErroredModListAnalyzer() {
            super(CrashManager.this, "errored mod list analyzer");
            modPattern = Pattern.compile("^[\\W]+[ULCHIJAD]*([ULCHIJADE])[\\W]+.+\\{.+}[\\W]+\\[(.+)][\\W]+\\((.+)\\).*");

            patterns.add(Pattern.compile("^-- System Details --$"));
            patterns.add(Pattern.compile("^[\\W]+FML: MCP .+$"));

            // last pattern should always be before mod list
            patterns.add(Pattern.compile("^[\\W]+States: .+$"));
        }

        protected boolean checkCapability() throws Exception {
            final List<ErroredMod> errorModList = new ArrayList<>();

            try (Scanner scanner = getCrashFileScanner()) {
                if (PatternEntry.matchPatterns(scanner, patterns, null)) {
                    LOGGER.debug("Not all patterns met. Skipping");
                    return false;
                }
                LOGGER.debug("All patterns are met. Working on a mod list");
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    Matcher matcher = modPattern.matcher(line);


                    // check if the last state is "E"
                    if (matcher.matches() && "e".equalsIgnoreCase(matcher.group(1))) {
                        // add its name to the list
                        ErroredMod mod = new ErroredMod(matcher);
                        errorModList.add(mod);
                        LOGGER.debug("Added: {}", mod);
                    }
                }
            }

            if (errorModList.isEmpty()) {
                LOGGER.info("Could not find mods that caused the crash.");
                return false;
            } else {
                LOGGER.info("Crash probably caused by the following mods: {}", errorModList);
            }

            boolean multiple = errorModList.size() > 1;

            setTitle("crash.analyzer.errored-mod.title", (multiple ? StringUtils.join(errorModList, ", ") : errorModList.get(0)));

            StringBuilder body = new StringBuilder();
            if (multiple) {
                for (ErroredMod mod : errorModList) {
                    body.append("– ");
                    mod.append(body);
                    body.append("\n");
                }
            } else {
                body.append(errorModList.get(0).name);
            }
            setBody("crash.analyzer.errored-mod.body." + (multiple ? "multiple" : "single"), body.toString(), errorModList.get(0).fileName);
            addButton(getButton("logs"));
            if (getLauncher() != null) {
                newButton("errored-mod-delete." + (multiple ? "multiple" : "single"), () -> {
                    final String prefix = "crash.analyzer.buttons.errored-mod-delete.";

                    File modsDir = new File(getLauncher().getGameDir(), "mods");
                    if (!modsDir.isDirectory()) {
                        Alert.showLocError(prefix + "error.title", prefix + "error.no-mods-folder");
                        return;
                    }

                    boolean success = true;

                    for (ErroredMod mod : errorModList) {
                        File modFile = new File(modsDir, mod.fileName);
                        if (!modFile.delete() && modFile.isFile()) {
                            Alert.showLocError(prefix + "error.title", prefix + "error.could-not-delete", modFile);
                            success = false;
                        }
                    }

                    if (success) {
                        Alert.showLocMessage(prefix + "success.title", prefix + "success." + (errorModList.size() > 1 ? "multiple" : "single"), null);
                    }
                    getAction("exit").execute("");
                });
            }
            crash.addExtra("erroredModList", errorModList.toString());
            return true;
        }
    }

    private class CrashDescriptionSeeker extends Entry {
        private final List<Pattern> patternList = Arrays.asList(
                Pattern.compile(".*[-]+.*Minecraft Crash Report.*[-]+"),
                Pattern.compile(".*Description: (?i:(?!.*debug.*))(.*)")
        );

        CrashDescriptionSeeker() {
            super(CrashManager.this, "crash description seeker");
        }

        @Override
        protected void execute() throws Exception {
            LOGGER.debug("Looking for crash description...");
            try (Scanner scanner = getCrashFileScanner()) {
                ArrayList<String> matches = new ArrayList<>();
                String description = null;
                if (PatternEntry.matchPatterns(scanner, patternList, matches)) {
                    if (matches.isEmpty()) {
                        LOGGER.debug("No description?");
                    } else {
                        description = matches.get(0);
                    }
                }
                if (description == null) {
                    LOGGER.info("Could not find crash description");
                    return;
                }
                String line = null;
                if (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    if (StringUtils.isBlank(line) || line.endsWith("[STDOUT] ")) { // must be empty after "Description" line
                        line = scanner.nextLine();
                    }
                }
                if (StringUtils.isBlank(line) || line.endsWith("[STDOUT] ")) {
                    LOGGER.debug("Stack trace line is empty?");

                    StringBuilder moreLines = new StringBuilder();
                    int additionalLines = 0;

                    while (scanner.hasNextLine() && additionalLines < 10) {
                        additionalLines++;
                        moreLines.append('\n').append(scanner.nextLine());
                    }
                    crash.addExtra("moreLines", moreLines.toString());
                    crash.addExtra("stackTraceLineIsEmpty", "");

                    return;
                }
                crash.setJavaDescription(line);
                StringBuilder stackTraceBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    if (StringUtils.isBlank(line)) {
                        break;
                    } else {
                        stackTraceBuilder.append('\n').append(line);
                    }
                }
                if (stackTraceBuilder.length() > 1) {
                    crash.setStackTrace(stackTraceBuilder.substring(1));
                }
            }
        }
    }

    private class GeneratedFilesSeeker extends Entry {
        final Pattern
                crashFilePattern = Pattern.compile("^.*#@!@# Game crashed!.+@!@# (.+)$"),
                nativeCrashFilePattern = Pattern.compile("# (.+)$");

        GeneratedFilesSeeker() {
            super(CrashManager.this, "generated files seeker");
        }

        @Override
        protected void execute() throws Exception {
            try (Scanner scanner = PatternEntry.getScanner(getProcessLogger())) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String crashFile = get(crashFilePattern, line);
                    if (crashFile != null) {
                        crash.setCrashFile(crashFile);
                        continue;
                    }
                    if (line.equals("# An error report file with more information is saved as:") && scanner.hasNextLine()) {
                        String nativeCrashFile = get(nativeCrashFilePattern, line = scanner.nextLine());
                        if (nativeCrashFile != null) {
                            crash.setNativeCrashFile(nativeCrashFile);
                        }
                    }
                }
            }
        }

        private String get(Pattern pattern, String line) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches() && matcher.groupCount() == 1) {
                return matcher.group(1);
            }
            return null;
        }
    }

    private class LogFlusherEntry extends Entry {
        public LogFlusherEntry() {
            super(CrashManager.this, "log flusher");
        }

        @Override
        protected void execute() {
            readFile(getCrash().getCrashFile());
            readFile(getCrash().getNativeCrashFile());

            if (getLauncher() != null
                    && modVersionsFilter.stream().anyMatch(getVersion().toLowerCase(java.util.Locale.ROOT)::contains)) {
                File modsDir = new File(getLauncher().getGameDir(), "mods");
                if (!modsDir.isDirectory()) {
                    LOGGER.info("No \"mods\" folder found");
                } else {
                    treeDir(modsDir, 2);
                }
                writeDelimiter();
            }

            if (TLauncher.getInstance() != null) {
                SystemInfo systemInfo;
                try {
                    systemInfo = systemInfoReporter.getReport().get();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.warn("Could not retrieve system info", e);
                    systemInfo = null;
                }
                if (systemInfo == null) {
                    LOGGER.warn("No system info is available");
                } else {
                    LOGGER.info("System info:");
                    systemInfo.getLines().forEach(LOGGER::info);
                }
            }
        }

        private void writeDelimiter() {
            LOGGER.info("++++++++++++++++++++++++++++++++++");
        }

        private void readFile(File file) {
            if (file == null) {
                return;
            }

            try {
                if (!file.isFile()) {
                    LOGGER.warn("File doesn't exist: {}", file);
                    return;
                }

                LOGGER.info("Reading file: {}", file);

                try (Scanner scanner = new Scanner(
                        new InputStreamReader(new FileInputStream(file), charset)
                )) {
                    while (scanner.hasNextLine()) {
                        LOGGER.info(LOG_FLUSHER, scanner.nextLine());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Could not read file: {}", file, e);
                }
            } finally {
                writeDelimiter();
            }
        }

        private void treeDir(File dir, int levelLimit) {
            treeDir(dir, 0, levelLimit, new StringBuilder());
        }

        private void treeDir(File dir, int currentLevel, int levelLimit, StringBuilder buffer) {
            if (dir == null) {
                LOGGER.info("skipping null directory");
                return;
            }

            if (!dir.isDirectory()) {
                LOGGER.info(LOG_FLUSHER, "{} (not a dir)", dir);
                return;
            }

            File[] list = Objects.requireNonNull(dir.listFiles(), "dir listing: " + dir.getAbsolutePath());

            if (currentLevel == 0) {
                LOGGER.info(LOG_FLUSHER, "{}", dir);
            } else if (list.length == 0) {
                LOGGER.info(LOG_FLUSHER, "{}└ [empty]", buffer);
            }

            File file;
            StringBuilder name;
            boolean skipDir;
            File[] subList;

            for (int i = 0; i < list.length; i++) {
                file = list[i];
                name = new StringBuilder(file.getName());

                subList = null;
                skipDir = false;

                long length = 0L;

                if (file.isDirectory()) {
                    subList = file.listFiles();

                    for (String skipFolder : skipFolders) {
                        if (file.getName().equalsIgnoreCase(skipFolder)) {
                            skipDir = true;
                            name.append(" [skipped]");
                            break;
                        }
                    }
                    if (!skipDir && (subList == null || subList.length == 0)) {
                        name.append(" [empty dir]");
                        skipDir = true;
                    }
                } else {
                    length = file.length();
                    if (length < 0L) {
                        name.append(" [unknown size]");
                    } else if (length == 0L) {
                        name.append(" [empty file]");
                    } else {
                        name.append(" [").append(length < 2048L ? length + " B" : (length / 1024L) + " KiB").append("]");
                    }
                }

                boolean currentlyLatestLevel = i == list.length - 1;
                if (currentlyLatestLevel)
                    LOGGER.info(LOG_FLUSHER, "{}└ {}", buffer, name);
                else
                    LOGGER.info(LOG_FLUSHER, "{}├ {}", buffer, name);

                StringBuilder subLevelBuffer = new StringBuilder()
                        .append(buffer)
                        .append(currentlyLatestLevel ? "  " : "│ ").append(' ');

                if (file.isFile() && file.getName().endsWith(".jar")) {
                    ZipFile zipFile;
                    try {
                        zipFile = new ZipFile(file, ZipFile.OPEN_READ);
                    } catch (IOException ioE) {
                        LOGGER.info(LOG_FLUSHER, "{}└ [!!!] Corrupted zip: {}", subLevelBuffer, ioE.toString());
                        continue;
                    }
                    try {
                        // also compute md5 hash, just in case.
                        // curseforge shows md5 of each file on the download page
                        if (length > 0L) {
                            String md5Message;
                            try {
                                md5Message = FileUtil.getMd5(file);
                            } catch (IOException e) {
                                md5Message = e.toString();
                            }
                            LOGGER.debug(LOG_FLUSHER, "{}├ md5 = {}", subLevelBuffer, md5Message);
                        }
                        boolean mcmod = tryMcModInfo(zipFile, subLevelBuffer);
                        mcmod |= tryModsToml(zipFile, subLevelBuffer);
                        mcmod |= tryFabricMod(zipFile, subLevelBuffer);
                        if (!mcmod) {
                            LOGGER.debug(LOG_FLUSHER, "{}└ [unknown mod format]", subLevelBuffer);
                        }
                    } finally {
                        IOUtils.closeQuietly(zipFile);
                    }
                } else if (file.isDirectory() && !skipDir) {
                    if (currentLevel == levelLimit) {
                        String str;

                        if (subList != null) {
                            StringBuilder s = new StringBuilder();

                            int files = 0, directories = 0;
                            for (File subFile : subList) {
                                if (subFile.isFile()) {
                                    files++;
                                }
                                if (subFile.isDirectory()) {
                                    directories++;
                                }
                            }

                            s.append("[");

                            switch (files) {
                                case 0:
                                    s.append("no files");
                                    break;
                                case 1:
                                    s.append("1 file");
                                    break;
                                default:
                                    s.append(files).append(" files");
                            }

                            s.append("; ");

                            switch (directories) {
                                case 0:
                                    s.append("no dirs");
                                    break;
                                case 1:
                                    s.append("1 dir");
                                    break;
                                default:
                                    s.append(directories).append(" dirs");
                            }

                            s.append(']');

                            str = s.toString();
                        } else {
                            str = "[empty dir]";
                        }
                        LOGGER.info(LOG_FLUSHER, "{}└ {}", subLevelBuffer, str);
                        continue;
                    }
                    treeDir(file, currentLevel + 1, levelLimit, subLevelBuffer);
                }
            }
        }

        private boolean tryFabricMod(ZipFile zipFile, StringBuilder buffer) {
            ZipEntry fabricModZipEntry = zipFile.getEntry("fabric.mod.json");
            if (fabricModZipEntry == null) {
                return false;
            }
            JsonElement fabricModRoot;
            try (InputStreamReader reader = new InputStreamReader(
                    zipFile.getInputStream(fabricModZipEntry),
                    StandardCharsets.UTF_8)) {
                fabricModRoot = Objects.requireNonNull(
                        JsonParser.parseReader(reader), "fabricModRoot");
            } catch (IOException | RuntimeException e) {
                LOGGER.info(LOG_FLUSHER, "{}└ [!!!] Couldn't read fabric.mod.json: {}",
                        buffer, e.toString());
                return false;
            }
            if (!fabricModRoot.isJsonObject()) {
                LOGGER.info(LOG_FLUSHER, "{}├ [!!!] Not a JSON object: {}", buffer,
                        fabricModRoot);
                return false;
            }
            JsonObject fabricModObj = fabricModRoot.getAsJsonObject();
            List<String> keys = Arrays.asList(
                    "id",
                    "version",
                    "depends"
            );
            List<Pair<String, JsonElement>> keyPairs = keys.stream()
                    .map(key -> Pair.of(key, fabricModObj.get(key)))
                    .filter(p -> p.getValue() != null)
                    .collect(Collectors.toList());
            displayKeyPairs(keyPairs, buffer);
            return true;
        }

        @SuppressWarnings("unchecked")
        private boolean tryModsToml(ZipFile zipFile, StringBuilder buffer) {
            ZipEntry modsTomlZipEntry = zipFile.getEntry("META-INF/mods.toml");
            if (modsTomlZipEntry == null) {
                return false;
            }
            Toml toml = new Toml();
            try (InputStreamReader reader = new InputStreamReader(
                    zipFile.getInputStream(modsTomlZipEntry),
                    StandardCharsets.UTF_8
            )) {
                toml.read(reader);
            } catch (IOException | RuntimeException e) {
                LOGGER.info(LOG_FLUSHER, "{}└ [!!!] Couldn't read mods.toml: {}",
                        buffer, e.toString());
                return false;
            }
            Map<String, Object> map = toml.toMap();
            Object dependenciesObj = map.get("dependencies");
            if (map.containsKey("mods")) {
                Object modsObj = map.get("mods");
                if (modsObj instanceof List) {
                    List<Map<String, Object>> mods = (List<Map<String, Object>>) modsObj;
                    for (Map<String, Object> mod : mods) {
                        displayModsTomlMod(
                                mod,
                                findModDependenciesToml(dependenciesObj, getModId(mod)),
                                buffer
                        );
                    }
                }
            }
            return true;
        }

        private String getModId(Map<String, Object> mod) {
            Object modIdObj = mod.get("modId");
            if (modIdObj instanceof String) {
                return (String) modIdObj;
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<Map<String, Object>> findModDependenciesToml(Object dependenciesObj,
                                                                  String modId) {
            if (dependenciesObj == null || modId == null) {
                return null;
            }
            if (dependenciesObj instanceof Map) {
                Map<?, ?> dependencies = (Map<?, ?>) dependenciesObj;
                Object modDependencies = dependencies.get(modId);
                if (modDependencies instanceof List) {
                    return (List<Map<String, Object>>) modDependencies;
                }
            } else {
                LOGGER.warn("dependenciesObj is not a Map: {}", dependenciesObj);
            }
            return null;
        }

        private void displayModsTomlMod(
                Map<String, Object> mod,
                List<Map<String, Object>> dependencies,
                StringBuilder buffer) {
            List<String> keys = Arrays.asList(
                    "modId",
                    "version",
                    "displayURL"
            );
            List<Pair<String, Object>> keyPairs = keys.stream()
                    .map(key -> Pair.of(key, mod.get(key)))
                    .filter(p -> p.getValue() != null)
                    .filter(p -> {
                        // filter values like ${VERSION}, etc.
                        Object v = p.getValue();
                        if (!(v instanceof String)) {
                            return true;
                        }
                        String s = (String) v;
                        return !s.startsWith("${");
                    })
                    .collect(Collectors.toList());
            if (keyPairs.isEmpty()) {
                LOGGER.info(LOG_FLUSHER, "{}└ [no known toml keys]: {}", buffer, mod);
            } else {
                displayKeyPairs(keyPairs, buffer);
                if (dependencies != null && !dependencies.isEmpty()) {
                    List<Pair<String, Object>> depKeyPairs = dependencies.stream()
                            .filter(d -> {
                                Object side = d.get("side");
                                return "BOTH".equals(side) || "CLIENT".equals(side);
                            })
                            .filter(d -> d.get("mandatory") == Boolean.TRUE)
                            .filter(d -> d.containsKey("modId"))
                            .map(d -> Pair.of(
                                    String.valueOf(d.get("modId")),
                                    d.getOrDefault("versionRange", "any")
                            ))
                            .collect(Collectors.toList());
                    StringBuilder depBuffer = new StringBuilder(buffer).append("    ");
                    LOGGER.info(LOG_FLUSHER, "{}└ [dependencies]", buffer);
                    displayKeyPairs(depKeyPairs, depBuffer);
                }
            }
        }

        private boolean tryMcModInfo(ZipFile zipFile, StringBuilder buffer) {
            ZipEntry mcmodZipEntry = zipFile.getEntry("mcmod.info");
            if (mcmodZipEntry == null) {
                return false;
            }
            JsonElement mcmodRoot;
            try (InputStreamReader reader = new InputStreamReader(
                    zipFile.getInputStream(mcmodZipEntry),
                    StandardCharsets.UTF_8)) {
                mcmodRoot = Objects.requireNonNull(
                        JsonParser.parseReader(reader), "mcmodRoot");
            } catch (IOException | RuntimeException e) {
                LOGGER.info(LOG_FLUSHER, "{}├ [!!!] Couldn't read mcmod.info: {}",
                        buffer, e.toString());
                return false;
            }
            if (mcmodRoot.isJsonArray()) {
                // modListVersion = 1
                for (JsonElement mcmodEntry : mcmodRoot.getAsJsonArray()) {
                    if (mcmodEntry.isJsonObject()) {
                        displayMcModInfo(mcmodEntry.getAsJsonObject(), buffer);
                    } else {
                        LOGGER.info(LOG_FLUSHER, "{}├ [!!!] Not a JSON object: {}", buffer, mcmodRoot);
                    }
                }
                return true;
            } else if (mcmodRoot.isJsonObject()) {
                // modListVersion > 1
                JsonElement modListElement = mcmodRoot.getAsJsonObject().get("modList");
                if (modListElement != null && modListElement.isJsonArray()) {
                    for (JsonElement modListEntry : modListElement.getAsJsonArray()) {
                        if (modListEntry.isJsonObject()) {
                            displayMcModInfo(modListEntry.getAsJsonObject(), buffer);
                        } else {
                            LOGGER.info(LOG_FLUSHER, "{}├ [!!!] Not a JSON object: {}",
                                    buffer, modListEntry);
                        }
                    }
                    return true;
                }
            }
            LOGGER.info(LOG_FLUSHER, "{}├ [!!!] Unknown or invalid mcmod.info: {}", buffer, mcmodRoot);
            return false;
        }

        private void displayMcModInfo(JsonObject mcmod, StringBuilder buffer) {
            List<String> keys = Arrays.asList(
                    "modid",
                    "version",
                    "mcversion",
                    "url",
                    "requiredMods",
                    "dependencies"
            );
            List<Pair<String, JsonElement>> keyPairs = keys.stream()
                    .map(key -> Pair.of(key, mcmod.get(key)))
                    .filter(p -> p.getValue() != null)
                    .filter(p -> {
                        // filter values like @VERSION@, etc.
                        JsonElement v = p.getValue();
                        if (!v.isJsonPrimitive()) {
                            return true;
                        }
                        JsonPrimitive pv = v.getAsJsonPrimitive();
                        if (!pv.isString()) {
                            return true;
                        }
                        String s = pv.getAsString();
                        return !s.startsWith("@") || !s.endsWith("@");
                    })
                    .collect(Collectors.toList());
            if (keyPairs.isEmpty()) {
                LOGGER.info(LOG_FLUSHER, "{}└ [no known mcmod keys]: {}", buffer, mcmod);
            } else {
                displayKeyPairs(keyPairs, buffer);
            }
        }

        private void displayKeyPairs(List<? extends Pair<String, ?>> keyPairs, StringBuilder buffer) {
            if (keyPairs.isEmpty()) {
                return;
            }
            if (keyPairs.size() > 1) {
                LOGGER.info(LOG_FLUSHER, "{}├ {} = {}", buffer,
                        keyPairs.get(0).getKey(), keyPairs.get(0).getValue());
                for (int i = 1; i < keyPairs.size() - 1; i++) {
                    Pair<String, ?> pair = keyPairs.get(i);
                    LOGGER.info(LOG_FLUSHER, "{}├ {} = {}", buffer, pair.getKey(), pair.getValue());
                }
            }
            int lastIndex = keyPairs.size() - 1;
            LOGGER.info(LOG_FLUSHER, "{}└ {} = {}", buffer,
                    keyPairs.get(lastIndex).getKey(), keyPairs.get(lastIndex).getValue());
        }
    }

    private static class BrowseAction extends ArgsAction {
        private final File gameDir;

        BrowseAction(File gameDir) {
            super("browse", new String[]{"www", "folder"});
            this.gameDir = gameDir;
        }

        @Override
        void execute(OptionSet args) {
            if (args.has("www")) {
                OS.openLink(args.valueOf("www").toString());
                return;
            }

            if (args.has("folder")) {
                String folderName = args.valueOf("folder").toString();
                File folder;
                if (folderName.startsWith(".")) {
                    folder = new File(gameDir, folderName.substring(1));
                } else {
                    folder = new File(folderName);
                }
                if (folder.isDirectory()) {
                    OS.openFolder(folder);
                }
            }
        }
    }

    private static class SetOptionAction extends BindableAction {
        private final OptionsFile file;

        public SetOptionAction(OptionsFile file) {
            super("option");
            this.file = Objects.requireNonNull(file, "file");
        }

        @Override
        public void execute(String arg) throws Exception {
            for (String optionPair : StringUtils.split(arg, ';')) {
                String[] pair = StringUtils.split(optionPair, ':');
                String key = pair[0], value = pair[1];
                file.set(key, value);
            }
            file.save();
            Alert.showLocMessage("crash.actions.set-options");
        }
    }

    private static class SetAction extends ArgsAction {
        private static final Logger LOGGER = LogManager.getLogger(SetAction.class);
        private final Map<OptionSpec<String>, String> optionMap = new HashMap<>();

        SetAction() {
            super("set");

            for (String key : ConfigurationDefaults.getInstance().getMap().keySet()) {
                optionMap.put(parser.accepts(key).withRequiredArg().ofType(String.class), key);
            }
        }

        @Override
        void execute(OptionSet args) {
            for (OptionSpec<?> spec : args.specs()) {
                String key = optionMap.get(spec);

                if (key == null) {
                    LOGGER.warn("Could not find key for spec {}", spec);
                    continue;
                }


                String value = (String) spec.value(args);

                if ("minecraft.memory".equals(key) && "fix".equals(value)) {
                    LOGGER.info("Migrating minecraft.memory = fix => minecraft.xmx = \"auto\"");
                    key = "minecraft.xmx";
                    value = "auto";
                }
                LOGGER.info("Set configuration key {} = {}", key, value);
                TLauncher.getInstance().getSettings().set(key, value);
                if (TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.isLoaded()) {
                    TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get().updateValues();
                }
            }
        }
    }

    private static class GuiAction extends BindableAction {
        public GuiAction() {
            super("gui");
        }

        @Override
        public void execute(String args) {
            if (args.startsWith("settings")) {
                TLauncher.getInstance().getFrame().mp.setScene(TLauncher.getInstance().getFrame().mp.defaultScene);
                TLauncher.getInstance().getFrame().mp.defaultScene.setSidePanel(DefaultScene.SidePanel.SETTINGS);
                if (args.equals("settings-tlauncher")) {
                    TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get().getTabPane().setSelectedIndex(1);
                }
                return;
            }

            if (args.equals("accounts")) {
                TLauncher.getInstance().getFrame().mp.setScene(TLauncher.getInstance().getFrame().mp.accountManager.get());
            }

            if (args.equals("versions")) {
                TLauncher.getInstance().getFrame().mp.setScene(TLauncher.getInstance().getFrame().mp.versionManager.get());
            }
        }
    }

    private static class ExitAction extends BindableAction {
        public ExitAction() {
            super("exit");
        }

        @Override
        public void execute(String arg) {
            TLauncher.getInstance().getUIListeners().getMinecraftUIListener().getCrashProcessingFrame().get().getCrashFrame().setVisible(false);
        }
    }

    private static class ForceUpdateAction extends BindableAction {
        public ForceUpdateAction() {
            super("force-update");
        }

        @Override
        public void execute(String arg) {
            TLauncher.getInstance().getUIListeners().getMinecraftUIListener().getCrashProcessingFrame().get().getCrashFrame().setVisible(false);
            TLauncher.getInstance().getFrame().mp.defaultScene.loginForm.checkbox.forceupdate.setSelected(true);
            TLauncher.getInstance().getFrame().mp.defaultScene.loginForm.startLauncher();
        }
    }
}
