package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.options.OptionsFile;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.ConfigurationDefaults;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.sentry.SentryContext;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.util.*;
import ru.turikhay.util.async.ExtendedThread;
import ru.turikhay.util.windows.DxDiag;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CrashManager {
    private final ArrayList<CrashManagerListener> listeners = new ArrayList<CrashManagerListener>();
    private final Watchdog watchdog = new Watchdog();

    private final Gson gson;
    private final Crash crash;

    private final MinecraftLauncher launcher;
    private final String version;
    private final CharSequence output;
    private final int exitCode;

    private final CrashEntryList.ListDeserializer listDeserializer;

    private final Map<String, IEntry> crashEntries = new LinkedHashMap<String, IEntry>();
    private final Map<String, BindableAction> actionsMap = new HashMap<String, BindableAction>();

    private final Entry
            generatedFilesSeekerEntry = new GeneratedFilesSeeker(),
            crashDescriptionSeeker = new CrashDescriptionSeeker(),
            dxDiagAheadProcessorEntry = DxDiag.isScannable()? new DxDiagAheadProcessor() : null,
            logFlusherEntry = new LogFlusherEntry();

    private void setupActions() {
        actionsMap.clear();

        addAction(new BrowseAction(launcher == null? new File("") : launcher.getGameDir()));
        addAction(new SetAction());
        addAction(new GuiAction());
        addAction(new ExitAction());
        addAction(new SetOptionAction(launcher == null? new OptionsFile(new File("test.txt")) : launcher.getOptionsFile()));
    }

    private void setupEntries() {
        crashEntries.clear();

        addEntry(generatedFilesSeekerEntry);
        addEntry(crashDescriptionSeeker);
        addEntry(new ErroredModListAnalyzer());

        loadLocal:
        {
            CrashEntryList internal;
            try {
                internal = loadEntries(getClass().getResourceAsStream("signature.json"), "internal");
            } catch (Exception e) {
                throw new RuntimeException("could not load local signatures", e);
            }

            loadExternal:
            {
                CrashEntryList external;
                try {
                    external = loadEntries(Compressor.uncompressMarked(Repository.EXTRA_VERSION_REPO.get("libraries/signature.json")), "external");
                } catch (Exception e) {
                    log("Could not load external entries", e);
                    break loadExternal;
                }

                if (external.getRevision() <= internal.getRevision()) {
                    log("External signatures are older or the same:", external.getRevision());
                    break loadExternal;
                }

                addAllEntries(external, "external");

                log("External entries revision (" + external.getRevision() + ") is newer than internal (" + internal.getRevision() + "), we'll load them instead of internal ones.");
                break loadLocal; // don't load internal if we have newer external
            }
            addAllEntries(internal, "internal");
        }

        addEntry(new GraphicsEntry(this));
        addEntry(new BadMainClassEntry(this));
        if(DxDiag.isScannable()) {
            addEntry(dxDiagAheadProcessorEntry);
        }
        addEntry(logFlusherEntry);
    }

    private CrashManager(MinecraftLauncher launcher, String version, CharSequence output, int exitCode) {
        this.launcher = launcher;
        this.version = version;
        this.output = U.requireNotNull(output, "output");
        this.exitCode = exitCode;

        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
                .registerTypeAdapter(CrashEntryList.class, listDeserializer = new CrashEntryList.ListDeserializer(this))
                .create();

        crash = new Crash(this);
    }

    public CrashManager(MinecraftLauncher launcher) {
        this(launcher, launcher.getVersion(), launcher.getLogOutput(), launcher.getExitCode());
    }

    public CrashManager(String version, CharSequence output, int exitCode) {
        this(null, version, output, exitCode);
    }

    public void startAndJoin() {
        synchronized (watchdog) {
            checkWorking();
            watchdog.unlockThread("start");

            try {
                watchdog.join();
            } catch (InterruptedException e) {
                log("Thread was interrupted", e);
            }
        }
    }

    private volatile boolean cancelled;

    public void cancel() {
        DxDiag.cancel();
        cancelled = true;
    }

    private void addAction(BindableAction action) {
        actionsMap.put(U.requireNotNull(action).getName(), action);
    }

    private <T extends IEntry> T addEntry(T entry) {
        if (crashEntries.containsKey(entry.getName())) {
            log("Removing", crashEntries.get(entry.getName()));
        }
        //log("Adding", entry.getName());
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
        log("Loading", type, "entries...");

        try {
            return gson.fromJson(new InputStreamReader(input, FileUtil.DEFAULT_CHARSET), CrashEntryList.class);
        } finally {
            U.close(input);
        }
    }

    public MinecraftLauncher getLauncher() {
        return launcher;
    }

    public String getVersion() {
        return version;
    }

    public CharSequence getOutput() {
        return output;
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
        listeners.add(U.requireNotNull(listener, "listener"));
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
        if(crashFile == null || !crashFile.isFile()) {
            log("Crash report file doesn't exist. May be looking into logs?");
            return PatternEntry.getScanner(getOutput());
        } else {
            log("Crash report file exist. We'll scan it.");
            return new Scanner(new InputStreamReader(new FileInputStream(crashFile), FileUtil.getCharset()));
        }
    }

    private SentryContext sentry() {
        return SentryContext.getContextOrGlobal(MinecraftLauncher.SENTRY_CONTEXT_NAME);
    }

    void log(Object... o) {
        U.log("[Crash]", o);
    }

    private class Watchdog extends ExtendedThread {
        private Executor executor;

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

            executor:
            {
                for (CrashManagerListener listener : listeners) {
                    listener.onCrashManagerProcessing(CrashManager.this);
                }

                try {
                    watchExecutor();
                } catch (CrashManagerInterrupted interrupted) {
                    for (CrashManagerListener listener : listeners) {
                        listener.onCrashManagerCancelled(CrashManager.this);
                    }
                    break executor;
                } catch (Exception e) {
                    for (CrashManagerListener listener : listeners) {
                        listener.onCrashManagerFailed(CrashManager.this, e);
                    }
                    sentry().sendError(CrashManager.class, "crashmanager fatal error", e, null);
                    break executor;
                }
                for (CrashManagerListener listener : listeners) {
                    listener.onCrashManagerComplete(CrashManager.this, crash);
                }
            }
        }

        private void watchExecutor() throws CrashManagerInterrupted, Exception {
            executor.unlockThread("start");
            try {
                executor.join();
            } catch (InterruptedException e) {
                throw new CrashManagerInterrupted(e);
            }

            if (executor.error != null) {
                if (executor.error instanceof CrashManagerInterrupted) {
                    throw (CrashManagerInterrupted) executor.error;
                }
                throw executor.error;
            }
        }
    }

    private class Executor extends ExtendedThread {
        private Exception error;

        Executor() {
            startAndWait();
        }

        private void scan() throws CrashManagerInterrupted, CrashEntryException, IOException {
            Object timer = Time.start(new Object());

            setupActions();
            setupEntries();

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

                        log("Found capable:", capableEntry.getName());
                        crash.setEntry(capableEntry);

                        if (capableEntry.isFake()) {
                            log("It is fake, skipping remaining...");
                        }
                    }
                } else if (entry instanceof Entry) {
                    if (capableEntry != null) {
                        if (!((Entry) entry).isCapable(capableEntry)) {
                            log("Skipping:", entry.getName());
                            continue;
                        }
                    }
                    log("Executing:", entry.getName());
                    try {
                        ((Entry) entry).execute();
                    } catch (Exception e) {
                        throw new CrashEntryException(entry, e);
                    }
                }
            }

            String sentryMessage;
            if(capableEntry == null) {
                if(crash.getJavaDescription() != null) {
                    sentryMessage = "crash:\""+  crash.getJavaDescription() +"\"";
                } else {
                    sentryMessage = "unknown crash";
                }
            } else {
                sentryMessage = "crash:" + capableEntry.getName();
            }

            DataBuilder dataBuilder = DataBuilder.create("crash", crash)
                    .add("exitCode", exitCode).add("description", crash.getDescription())
                    .add("stackTrace", crash.getStackTrace()).add("javaDescription", crash.getJavaDescription())
                    .add(crash.getExtraInfo());

            sentry().sendWarning(CrashManager.class, sentryMessage, dataBuilder,
                DataBuilder.create("mcVersion", version).add("exitCode", exitCode).add("javaDescription", crash.getJavaDescription())
            );

            log("Done in", Time.stop(timer), "ms");
        }

        @Override
        public void run() {
            lockThread("start");
            try {
                scan();
            } catch (Exception e) {
                error = e;
            }
        }
    }

    private class CrashManagerInterrupted extends Exception {
        CrashManagerInterrupted() {
        }

        CrashManagerInterrupted(Throwable cause) {
            super(cause);
        }
    }

    private class CrashEntryException extends Exception {
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
        private final ArrayList<Pattern> patterns = new ArrayList<Pattern>();

        ErroredModListAnalyzer() {
            super(CrashManager.this, "errored mod list analyzer");
            modPattern = Pattern.compile("^[\\W]+[ULCHIJAD]*([ULCHIJADE])[\\W]+.+\\{.+\\}[\\W]+\\[(.+)\\][\\W]+\\((.+)\\).*");

            patterns.add(Pattern.compile("^-- System Details --$"));
            patterns.add(Pattern.compile("^[\\W]+FML: MCP .+$"));

            // last pattern should always be before mod list
            patterns.add(Pattern.compile("^[\\W]+States: .+$"));
        }

        protected boolean checkCapability() throws Exception {
            Scanner scanner = getCrashFileScanner();

            if(PatternEntry.matchPatterns(scanner, patterns, null)) {
                log("Not all patterns met. Skipping");
                return false;
            }

            log("All patterns are met. Working on a mod list");
            final List<ErroredMod> errorModList = new ArrayList<>();

            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher matcher = modPattern.matcher(line);


                // check if the last state is "E"
                if(matcher.matches() && "e".equalsIgnoreCase(matcher.group(1))) {
                    // add its name to the list
                    ErroredMod mod = new ErroredMod(matcher);
                    errorModList.add(mod);
                    log("Added:", mod);
                }
            }

            if(errorModList.isEmpty()) {
                log("Could not find any errored mods. Well, okay...");
                return false;
            }

            boolean multiple = errorModList.size() > 1;

            setTitle("crash.analyzer.errored-mod.title", (multiple? StringUtils.join(errorModList, ", ") : errorModList.get(0)));

            StringBuilder body = new StringBuilder();
            if(multiple) {
                for(ErroredMod mod : errorModList) {
                    body.append("– ");
                    mod.append(body);
                    body.append("\n");
                }
            } else {
                body.append(errorModList.get(0).name);
            }
            setBody("crash.analyzer.errored-mod.body." + (multiple? "multiple" : "single"), body.toString(), errorModList.get(0).fileName);
            addButton(getButton("logs"));
            if(getLauncher() != null) {
                newButton("errored-mod-delete." + (multiple ? "multiple" : "single"), new Action() {
                    @Override
                    public void execute() throws Exception {
                        final String prefix = "crash.analyzer.buttons.errored-mod-delete.";

                        File modsDir = new File(getLauncher().getGameDir(), "mods");
                        if(!modsDir.isDirectory()) {
                            Alert.showLocError(prefix + "error.title", prefix + "error.no-mods-folder");
                            return;
                        }

                        boolean success = true;

                        for (ErroredMod mod : errorModList) {
                            File modFile = new File(modsDir, mod.fileName);
                            if(!modFile.delete() && modFile.isFile()) {
                                Alert.showLocError(prefix + "error.title", prefix + "error.could-not-delete", modFile);
                                success = false;
                            }
                        }

                        if(success) {
                            Alert.showLocMessage(prefix + "success.title", prefix + "success." + (errorModList.size() > 1 ? "multiple" : "single"), null);
                        }
                        getAction("exit").execute("");
                    }
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
            log("Looking for crash description...");
            Scanner scanner = getCrashFileScanner();
            ArrayList<String> matches = new ArrayList<>();
            String description = null;
            findDescription:
            {
                if (!PatternEntry.matchPatterns(scanner, patternList, matches)) {
                    break findDescription;
                }
                if (matches.isEmpty()) {
                    log("No description?");
                    break findDescription;
                }
                description = matches.get(0);
            }
            if(description == null) {
                log("Could not find crash description");
                return;
            }
            String line = null;
            if(scanner.hasNextLine()) {
                line = scanner.nextLine();
                if(StringUtils.isBlank(line) || line.endsWith("[STDOUT] ")) { // must be empty after "Description" line
                    line = scanner.nextLine();
                }
            }
            if(StringUtils.isBlank(line) || line.endsWith("[STDOUT] ")) {
                log("Stack trace line is empty?");

                StringBuilder moreLines = new StringBuilder();
                int additionalLines = 0;

                while(scanner.hasNextLine() && additionalLines < 10) {
                    additionalLines++;
                    moreLines.append('\n').append(scanner.nextLine());
                }
                sentry().sendWarning(CrashManager.class, "stack trace line is empty", DataBuilder.create("description", description).add("lines", moreLines.length() > 1? moreLines.substring(1) : null));
                crash.addExtra("stackTraceLineIsEmpty", "");

                return;
            }
            crash.setJavaDescription(line);
            StringBuilder stackTraceBuilder = new StringBuilder();
            while(scanner.hasNextLine()) {
                line = scanner.nextLine();
                if(StringUtils.isBlank(line)) {
                    break;
                } else {
                    stackTraceBuilder.append('\n').append(line);
                }
            }
            if(stackTraceBuilder.length() > 1) {
                crash.setStackTrace(stackTraceBuilder.substring(1));
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
            Scanner scanner = PatternEntry.getScanner(getOutput());

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                /*String crashDescription = get(crashDescriptionPattern, line);
                if(crashDescription != null) {
                    crash.setDescription(crashDescription);
                    if(scanner.hasNextLine()) {
                        String nextLine = null;
                        while(scanner.hasNextLine()) {
                            nextLine = scanner.nextLine();
                            if(!StringUtils.isBlank(nextLine)) {
                                break;
                            }
                        }
                        StringBuilder stackTraceBuilder = new StringBuilder();
                        if(!StringUtils.isBlank(nextLine)) {
                            crash.setJavaDescription(nextLine);
                            while(scanner.hasNextLine()) {
                                nextLine = scanner.nextLine();
                                if(StringUtils.isBlank(nextLine)) {
                                    break;
                                } else {
                                    stackTraceBuilder.append('\n').append(nextLine);
                                }
                            }
                        }
                        if(stackTraceBuilder.length() > 1) {
                            crash.setStackTrace(stackTraceBuilder.substring(1));
                        }
                    }
                }*/

                String crashFile = get(crashFilePattern, line);
                if (crashFile != null) {
                    crash.setCrashFile(crashFile);
                    continue;
                }

                if (line.equals("# An error report file with more information is saved as:") && scanner.hasNextLine()) {
                    String nativeCrashFile = get(nativeCrashFilePattern, line = scanner.nextLine());
                    if (nativeCrashFile != null) {
                        crash.setNativeCrashFile(nativeCrashFile);
                        continue;
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

    private class DxDiagAheadProcessor extends Entry {
        DxDiagAheadProcessor() {
            super(CrashManager.this, "dxdiag ahead processor");
        }

        @Override
        protected void execute() throws Exception {
            try {
                DxDiag.get();
            } catch (Exception e) {
            }
        }
    }

    private class LogFlusherEntry extends Entry {
        public LogFlusherEntry() {
            super(CrashManager.this, "log flusher");
        }

        @Override
        protected void execute() throws Exception {
            synchronized (U.lock) {
                readFile(getCrash().getCrashFile());
                readFile(getCrash().getNativeCrashFile());

                if (getLauncher() != null && getVersion().toLowerCase().contains("forge")) {
                    treeDir(new File(getLauncher().getGameDir(), "mods"), 2);
                    writeDelimiter();
                }

                if (DxDiag.isScannable()) {
                    try {
                        DxDiag.get();
                    } catch (Exception e) {
                        U.log("Could not retrieve DxDiag", e);
                    }
                }
            }
        }

        private void writeDelimiter() {
            nlog("++++++++++++++++++++++++++++++++++");
        }

        private void readFile(File file) {
            if (file == null) {
                return;
            }

            nlog("<File", file, ">");
            try {
                if (!file.isFile()) {
                    log("File doesn't exist:", file);
                    return;
                }

                nlog("Reading file:", file);
                nlog();

                Scanner scanner = null;
                try {
                    scanner = new Scanner(file);
                    while (scanner.hasNextLine()) {
                        nlog(scanner.nextLine());
                    }
                } catch (Exception e) {
                    log("Could not read file:", file, e);
                } finally {
                    U.close(scanner);
                }
            } finally {
                nlog("</File", file, ">");
                writeDelimiter();
            }
        }

        private void treeDir(File dir, int levelLimit) {
            treeDir(dir, 0, levelLimit, new StringBuilder());
        }

        private void treeDir(File dir, int currentLevel, int levelLimit, StringBuilder buffer) {
            if(!dir.isDirectory()) {
                plog(dir, " (not a dir)");
                return;
            }

            File[] list = U.requireNotNull(dir.listFiles(), "dir listing: " + dir.getAbsolutePath());

            if(currentLevel == 0) {
                plog(dir);
            } else if(list == null || list.length == 0) {
                plog(buffer, "└ [empty]");
            }

            StringBuilder dirBuffer = null;
            File file; StringBuilder name; boolean skipDir;
            File[] subList;

            for (int i = 0; i < list.length; i++) {
                file = list[i];
                name = new StringBuilder(file.getName());

                subList = null;
                skipDir = false;

                if(file.isDirectory()) {
                    subList = file.listFiles();

                    skipIt:
                    {
                        for (String skipFolder : listDeserializer.getSkipFolders()) {
                            if (file.getName().equalsIgnoreCase(skipFolder)) {
                                skipDir = true;
                                name.append(" [skipped]");
                                break skipIt;
                            }
                        }
                        if (subList == null || subList.length == 0) {
                            name.append(" [empty dir]");
                            skipDir = true;
                        }
                    }
                } else {
                    long length = file.length();
                    if(length == 0L) {
                        name.append(" [empty file]");
                    } else {
                        name.append("[").append(length < 2048L ? length + " B" : (length / 1024L) + " KiB").append("]");
                    }
                }

                boolean currentlyLatestLevel = i == list.length - 1;
                plog(buffer, currentlyLatestLevel? "└ " : "├ ", name.toString());

                if(file.isDirectory() && !skipDir) {
                    if(dirBuffer == null || currentlyLatestLevel) {
                        dirBuffer = new StringBuilder().append(buffer).append(currentlyLatestLevel? "  " : "│ ").append(' ');
                    }

                    if(currentLevel == levelLimit) {
                        String str;

                        if(subList != null) {
                            StringBuilder s = new StringBuilder();

                            int files = 0, directories = 0;
                            for(File subFile : subList) {
                                if(subFile.isFile()) {
                                    files++;
                                }
                                if(subFile.isDirectory()) {
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

                            switch(directories) {
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
                        plog(dirBuffer, "└ ", str);
                        continue;
                    }

                    treeDir(file, currentLevel + 1, levelLimit, dirBuffer);
                }
            }
        }

        private void nlog(Object... o) {
            U.plog("+", o);
        }

        private void plog(Object... objs) {
            StringBuilder b = new StringBuilder();

            for(Object o : objs) {
                b.append(o);
            }

            nlog(b);
        }
    }

    private class BrowseAction extends ArgsAction {
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
                if(folderName.startsWith(".")) {
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

    private class SetOptionAction extends BindableAction {
        private final OptionsFile file;

        public SetOptionAction(OptionsFile file) {
            super("option");
            this.file = U.requireNotNull(file, "file");
        }

        @Override
        public void execute(String arg) throws Exception {
            for(String optionPair : StringUtils.split(arg, ';')) {
                String[] pair = StringUtils.split(optionPair, ':');
                String key = pair[0], value = pair[1];
                file.set(key, value);
            }
            file.save();
            Alert.showLocMessage("crash.actions.set-options");
        }
    }

    private class SetAction extends ArgsAction {
        private final Map<OptionSpec<String>, String> optionMap = new HashMap<OptionSpec<String>, String>();

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
                    log("Could not find key for spec", spec);
                    continue;
                }


                String value = (String) spec.value(args);

                if ("minecraft.memory".equals(key) && "fix".equals(value)) {
                    int current = TLauncher.getInstance().getSettings().getInteger("minecraft.memory"), set;

                    if (current > OS.Arch.PREFERRED_MEMORY) {
                        set = OS.Arch.PREFERRED_MEMORY;
                    } else {
                        set = OS.Arch.MIN_MEMORY;
                    }

                    value = String.valueOf(set);
                }
                log("Setting:", key, value);
                TLauncher.getInstance().getSettings().set(key, value);
                if(TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.isLoaded()) {
                    TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get().updateValues();
                }
            }
        }
    }

    private class GuiAction extends BindableAction {
        public GuiAction() {
            super("gui");
        }

        @Override
        public void execute(String args) throws Exception {
            if(args.equals("logs")) {
                if (getLauncher() != null && getLauncher().getLogger() != null && !getLauncher().getLogger().isKilled()) {
                    getLauncher().getLogger().show(true);
                } else {
                    TLauncher.getInstance().getLogger().show(true);
                }
                return;
            }

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

    private class ExitAction extends BindableAction {
        public ExitAction() {
            super("exit");
        }

        @Override
        public void execute(String arg) throws Exception {
            TLauncher.getInstance().getUIListeners().getMinecraftUIListener().getCrashProcessingFrame().get().getCrashFrame().setVisible(false);
        }
    }
}
