package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.ConfigurationDefaults;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.util.*;
import ru.turikhay.util.async.ExtendedThread;
import ru.turikhay.util.windows.DxDiag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
            generatedFilesSearcherEntry = new GeneratedFilesSearcher(),
            dxDiagAheadProcessorEntry = DxDiag.isScannable()? new DxDiagAheadProcessor() : null,
            logFlusherEntry = new LogFlusherEntry();

    private void setupActions() {
        actionsMap.clear();

        addAction(new BrowseAction());
        addAction(new SetAction());
        addAction(new GuiAction());
        addAction(new ExitAction());
    }

    private void setupEntries() {
        crashEntries.clear();

        addEntry(generatedFilesSearcherEntry);

        loadLocal:
        {
            CrashEntryList internal;
            try {
                internal = loadEntries(getClass().getResourceAsStream("signatures.json"), "internal");
            } catch (Exception e) {
                throw new RuntimeException("could not load local signatures", e);
            }

            loadExternal:
            {
                CrashEntryList external;
                try {
                    external = loadEntries(Compressor.uncompressMarked(Repository.EXTRA_VERSION_REPO.get("signatures.json")), "external");
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
        this(launcher, launcher.getVersion(), launcher.getOutput(), launcher.getExitCode());
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
                    log("Checking entry:", entry.getName());
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

    private class GeneratedFilesSearcher extends Entry {

        GeneratedFilesSearcher() {
            super(CrashManager.this, "generated files searcher");
        }

        @Override
        protected void execute() throws Exception {
            Scanner scanner = PatternEntry.getScanner(getOutput());

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String crashFile = get(Pattern.compile("^.*#@!@# Game crashed!.+@!@# (.+)$"), line);
                if (crashFile != null) {
                    crash.setCrashFile(crashFile);
                    continue;
                }

                if (line.equals("# An error report file with more information is saved as:") && scanner.hasNextLine()) {
                    String nativeCrashFile = get(Pattern.compile("# (.+)$"), line = scanner.nextLine());
                    if (nativeCrashFile != null) {
                        crash.setNativeCrashFile(nativeCrashFile);
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
        private String[] skipFolderList;

        public LogFlusherEntry() {
            super(CrashManager.this, "log flusher");
        }

        @Override
        protected void execute() throws Exception {
            String skipFolderListStr = getVar("skip-folders");

            if(StringUtils.isBlank(skipFolderListStr)) {
                skipFolderList = null;
            } else {
                log("Skip folder list:", skipFolderListStr);
                skipFolderList = StringUtils.split(skipFolderListStr, ';');
            }

            synchronized (U.lock) {
                readFile(getCrash().getCrashFile());
                readFile(getCrash().getNativeCrashFile());

                if (getLauncher() != null && getVersion().toLowerCase().contains("forge")) {
                    treeDir(new File(getLauncher().getGameDir(), "mods"), 2);
                    writeDelimiter();
                }

                if (DxDiag.isScannable()) {
                    try {
                        DxDiag.get().flushToLogger();
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

        private void treeDir(File dir, int limit) {
            treeDir(dir, 0, limit, new StringBuilder());
        }

        private void treeDir(File dir, int current, int limit, StringBuilder buffer) {
            if(!dir.isDirectory()) {
                plog(dir, " (not a dir)");
                return;
            }

            File[] list = U.requireNotNull(dir.listFiles(), "dir listing: " + dir.getAbsolutePath());

            if(current == 0) {
                plog(dir);
            } else if(list == null || list.length == 0) {
                plog(buffer, "└ [empty]");
            }

            StringBuilder dirBuffer = null;
            File file; String name; boolean skipDir;
            File[] subList;

            for (int i = 0; i < list.length; i++) {
                file = list[i];
                name = file.getName();

                subList = null;
                skipDir = false;

                if(file.isDirectory()) {
                    subList = file.listFiles();

                    skipIt:
                    {
                        if (skipFolderList != null) {
                            for (String skipFolder : skipFolderList) {
                                if (name.equalsIgnoreCase(skipFolder)) {
                                    skipDir = true;
                                    name += " [skipped]";
                                    break skipIt;
                                }
                            }
                        }

                        if (subList == null || subList.length == 0) {
                            name += " [empty dir]";
                            skipDir = true;
                        }
                    }
                } else {
                    long length = file.length();
                    if(length == 0L) {
                        name += " [empty file]";
                    } else {
                        name += " ["+ length / 1024L +" kbytes]";
                    }
                }

                plog(buffer, i == list.length - 1? "└ " : "├ ", name);

                if(file.isDirectory() && !skipDir) {
                    if(dirBuffer == null || i == list.length - 1) {
                        dirBuffer = new StringBuilder().append(buffer).append(i == list.length - 1? "  " : "│ ").append(' ');
                    }

                    if(current == limit) {
                        String str;

                        if(subList != null && subList.length > 0) {
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

                    treeDir(file, current + 1, limit, dirBuffer);
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
        BrowseAction() {
            super("browse", new String[]{"www", "folder"});
        }

        @Override
        void execute(OptionSet args) {
            if (args.has("www")) {
                OS.openLink(args.valueOf("www").toString());
                return;
            }

            if (args.has("folder")) {
                File folder = new File(args.valueOf("folder").toString());
                if (folder.isDirectory()) {
                    OS.openFolder(folder);
                }
            }
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
                TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.updateValues();
            }
        }
    }

    private class GuiAction extends BindableAction {
        public GuiAction() {
            super("gui");
        }

        @Override
        public void execute(String args) throws Exception {
            if (args.startsWith("settings")) {
                TLauncher.getInstance().getFrame().mp.setScene(TLauncher.getInstance().getFrame().mp.defaultScene);
                TLauncher.getInstance().getFrame().mp.defaultScene.setSidePanel(DefaultScene.SidePanel.SETTINGS);
                if (args.equals("settings-tlauncher")) {
                    TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.getTabPane().setSelectedIndex(1);
                }
                return;
            }

            if (args.equals("accounts")) {
                TLauncher.getInstance().getFrame().mp.setScene(TLauncher.getInstance().getFrame().mp.accountEditor);
            }

            if (args.equals("versions")) {
                TLauncher.getInstance().getFrame().mp.setScene(TLauncher.getInstance().getFrame().mp.versionManager);
            }
        }
    }

    private class ExitAction extends BindableAction {
        public ExitAction() {
            super("exit");
        }

        @Override
        public void execute(String arg) throws Exception {
            TLauncher.getInstance().getMinecraftListener().getCrashProcessingFrame().getCrashFrame().setVisible(false);
        }
    }
}
