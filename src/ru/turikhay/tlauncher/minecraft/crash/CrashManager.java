package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.ConfigurationDefaults;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.util.Compressor;
import ru.turikhay.util.DXDiagScanner;
import ru.turikhay.util.OS;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

public final class CrashManager {
   private final ArrayList listeners;
   private final CrashManager.Watchdog watchdog;
   private final Gson gson;
   private final Crash crash;
   private final MinecraftLauncher launcher;
   private final String version;
   private final CharSequence output;
   private final int exitCode;
   private final CrashEntryList.ListDeserializer listDeserializer;
   private final Map crashEntries;
   private final Map actionsMap;
   private final Entry generatedFilesSearcherEntry;
   private final Entry dxDiagAheadProcessorEntry;
   private final Entry logFlusherEntry;
   private volatile boolean cancelled;

   private void setupActions() {
      this.actionsMap.clear();
      this.addAction(new CrashManager.BrowseAction());
      this.addAction(new CrashManager.SetAction());
      this.addAction(new CrashManager.GuiAction());
      this.addAction(new CrashManager.ExitAction());
   }

   private void setupEntries() {
      this.crashEntries.clear();
      this.addEntry(this.generatedFilesSearcherEntry);

      CrashEntryList internal;
      try {
         internal = this.loadEntries(this.getClass().getResourceAsStream("signatures.json"), "internal");
      } catch (Exception var4) {
         throw new RuntimeException("could not load local signatures", var4);
      }

      label27: {
         label31: {
            CrashEntryList external;
            try {
               external = this.loadEntries(Compressor.uncompressMarked(Repository.EXTRA_VERSION_REPO.get("signatures.json")), "external");
            } catch (Exception var5) {
               this.log("Could not load external entries", var5);
               break label31;
            }

            if (external.getRevision() > internal.getRevision()) {
               this.addAllEntries(external, "external");
               this.log("External entries revision (" + external.getRevision() + ") is newer than internal (" + internal.getRevision() + "), we'll load them instead of internal ones.");
               break label27;
            }

            this.log("External signatures are older or the same:", external.getRevision());
         }

         this.addAllEntries(internal, "internal");
      }

      this.addEntry(new GraphicsEntry(this));
      this.addEntry(this.dxDiagAheadProcessorEntry);
      this.addEntry(this.logFlusherEntry);
   }

   private CrashManager(MinecraftLauncher launcher, String version, CharSequence output, int exitCode) {
      this.listeners = new ArrayList();
      this.watchdog = new CrashManager.Watchdog();
      this.crashEntries = new LinkedHashMap();
      this.actionsMap = new HashMap();
      this.generatedFilesSearcherEntry = new CrashManager.GeneratedFilesSearcher();
      this.dxDiagAheadProcessorEntry = new CrashManager.DxDiagAheadProcessor();
      this.logFlusherEntry = new CrashManager.LogFlusherEntry();
      this.launcher = launcher;
      this.version = version;
      this.output = (CharSequence)U.requireNotNull(output, "output");
      this.exitCode = exitCode;
      this.gson = (new GsonBuilder()).registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory()).registerTypeAdapter(CrashEntryList.class, this.listDeserializer = new CrashEntryList.ListDeserializer(this)).create();
      this.crash = new Crash(this);
   }

   public CrashManager(MinecraftLauncher launcher) {
      this(launcher, launcher.getVersion(), launcher.getOutput(), launcher.getExitCode());
   }

   public void startAndJoin() {
      synchronized(this.watchdog) {
         this.checkWorking();
         this.watchdog.unlockThread("start");

         try {
            this.watchdog.join();
         } catch (InterruptedException var4) {
            this.log("Thread was interrupted", var4);
         }

      }
   }

   public void cancel() {
      DXDiagScanner.cancelScan();
      this.cancelled = true;
   }

   private void addAction(BindableAction action) {
      this.actionsMap.put(((BindableAction)U.requireNotNull(action)).getName(), action);
   }

   private IEntry addEntry(IEntry entry) {
      if (this.crashEntries.containsKey(entry.getName())) {
         this.log("Removing", this.crashEntries.get(entry.getName()));
      }

      this.log("Adding", entry.getName());
      this.crashEntries.put(entry.getName(), entry);
      return entry;
   }

   private void addAllEntries(CrashEntryList entryList, String type) {
      Iterator var3 = entryList.getSignatures().iterator();

      while(var3.hasNext()) {
         CrashEntry entry = (CrashEntry)var3.next();
         this.log("Processing", type, "entry:", entry);
         this.addEntry(entry);
      }

   }

   private CrashEntryList loadEntries(InputStream input, String type) throws Exception {
      this.log("Loading", type, "entries...");

      CrashEntryList var3;
      try {
         var3 = (CrashEntryList)this.gson.fromJson((Reader)(new InputStreamReader(input, "UTF-8")), (Class)CrashEntryList.class);
      } finally {
         U.close(input);
      }

      return var3;
   }

   public MinecraftLauncher getLauncher() {
      return this.launcher;
   }

   public String getVersion() {
      return this.version;
   }

   public CharSequence getOutput() {
      return this.output;
   }

   public int getExitCode() {
      return this.exitCode;
   }

   public Crash getCrash() {
      if (Thread.currentThread() != this.watchdog && Thread.currentThread() != this.watchdog.executor) {
         this.checkAlive();
      }

      synchronized(this.watchdog) {
         return this.crash;
      }
   }

   BindableAction getAction(String name) {
      return (BindableAction)this.actionsMap.get(name);
   }

   String getVar(String key) {
      return (String)this.listDeserializer.getVars().get(key);
   }

   public void addListener(CrashManagerListener listener) {
      this.checkWorking();
      this.listeners.add(U.requireNotNull(listener, "listener"));
   }

   private void checkAlive() {
      if (this.watchdog.isAlive()) {
         throw new IllegalStateException("thread is alive");
      }
   }

   private void checkWorking() {
      if (this.watchdog.isWorking()) {
         throw new IllegalStateException("thread is working");
      }
   }

   void log(Object... o) {
      U.log("[Crash]", o);
   }

   private class ExitAction extends BindableAction {
      public ExitAction() {
         super("exit");
      }

      public void execute(String arg) throws Exception {
         TLauncher.getInstance().getMinecraftListener().getCrashProcessingFrame().getCrashFrame().setVisible(false);
      }
   }

   private class GuiAction extends BindableAction {
      public GuiAction() {
         super("gui");
      }

      public void execute(String args) throws Exception {
         if (args.startsWith("settings")) {
            TLauncher.getInstance().getFrame().mp.setScene(TLauncher.getInstance().getFrame().mp.defaultScene);
            TLauncher.getInstance().getFrame().mp.defaultScene.setSidePanel(DefaultScene.SidePanel.SETTINGS);
            if (args.equals("settings-tlauncher")) {
               TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.getTabPane().setSelectedIndex(1);
            }

         } else {
            if (args.equals("accounts")) {
               TLauncher.getInstance().getFrame().mp.setScene(TLauncher.getInstance().getFrame().mp.accountEditor);
            }

            if (args.equals("versions")) {
               TLauncher.getInstance().getFrame().mp.setScene(TLauncher.getInstance().getFrame().mp.versionManager);
            }

         }
      }
   }

   private class SetAction extends ArgsAction {
      private final Map optionMap = new HashMap();

      SetAction() {
         super("set");
         Iterator var2 = ConfigurationDefaults.getInstance().getMap().keySet().iterator();

         while(var2.hasNext()) {
            String key = (String)var2.next();
            this.optionMap.put(this.parser.accepts(key).withRequiredArg().ofType(String.class), key);
         }

      }

      void execute(OptionSet args) {
         Iterator var2 = args.specs().iterator();

         while(var2.hasNext()) {
            OptionSpec spec = (OptionSpec)var2.next();
            String key = (String)this.optionMap.get(spec);
            if (key == null) {
               this.log(new Object[]{"Could not find key for spec", spec});
            } else {
               String value = (String)spec.value(args);
               if ("minecraft.memory".equals(key) && "fix".equals(value)) {
                  int current = TLauncher.getInstance().getSettings().getInteger("minecraft.memory");
                  int set;
                  if (current > OS.Arch.PREFERRED_MEMORY) {
                     set = OS.Arch.PREFERRED_MEMORY;
                  } else {
                     set = 512;
                  }

                  value = String.valueOf(set);
               }

               this.log(new Object[]{"Setting:", key, value});
               TLauncher.getInstance().getSettings().set(key, value);
               TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.updateValues();
            }
         }

      }
   }

   private class BrowseAction extends ArgsAction {
      BrowseAction() {
         super("browse", new String[]{"www", "folder"});
      }

      void execute(OptionSet args) {
         if (args.has("www")) {
            OS.openLink(args.valueOf("www").toString());
         } else {
            if (args.has("folder")) {
               File folder = new File(args.valueOf("folder").toString());
               if (folder.isDirectory()) {
                  OS.openFolder(folder);
               }
            }

         }
      }
   }

   private class LogFlusherEntry extends Entry {
      public LogFlusherEntry() {
         super(CrashManager.this, "log flusher");
      }

      protected void execute() throws Exception {
         synchronized(U.lock) {
            this.readFile(CrashManager.this.getCrash().getCrashFile());
            this.readFile(CrashManager.this.getCrash().getNativeCrashFile());
            if (CrashManager.this.getLauncher() != null && CrashManager.this.getVersion().toLowerCase().contains("forge")) {
               this.readDirectory(new File(CrashManager.this.getLauncher().getGameDir(), "mods"));
               this.writeDelimiter();
            }

            if (DXDiagScanner.isScannable()) {
               this.plog("<DXDiag>");

               try {
                  DXDiagScanner.DXDiagScannerResult result = DXDiagScanner.getInstance().getResult();
                  Iterator var3 = result.getLines().iterator();

                  while(var3.hasNext()) {
                     String l = (String)var3.next();
                     this.plog(l);
                  }

                  this.writeDelimiter();
                  this.plog("In a nutshell:");
                  this.plog("System info:", result.getSystemInfo());
                  this.plog("Display devices:", result.getDisplayDevices());
               } catch (DXDiagScanner.DXDiagException var6) {
                  this.log(new Object[]{"Could not fetch DXDiag info:", var6});
               } catch (InterruptedException var7) {
                  this.log(new Object[]{"Interrupted", var7});
               }

               this.plog("</DXDiag>");
               this.writeDelimiter();
            }

         }
      }

      private void writeDelimiter() {
         this.plog("++++++++++++++++++++++++++++++++++");
      }

      private void readFile(File file) {
         if (file != null) {
            this.plog("<File", file, ">");

            try {
               if (!file.isFile()) {
                  this.log(new Object[]{"File doesn't exist:", file});
                  return;
               }

               this.plog("Reading file:", file);
               this.plog();
               Scanner scanner = null;

               try {
                  scanner = new Scanner(file);

                  while(scanner.hasNextLine()) {
                     this.plog(scanner.nextLine());
                  }
               } catch (Exception var12) {
                  this.log(new Object[]{"Could not read file:", file, var12});
               } finally {
                  U.close(scanner);
               }
            } finally {
               this.plog("</File", file, ">");
               this.writeDelimiter();
            }

         }
      }

      private void readDirectory(File dir) {
         if (dir.isDirectory()) {
            this.plog("<Dir", dir, ">");

            try {
               File[] var2 = dir.listFiles();
               int var3 = var2.length;

               for(int var4 = 0; var4 < var3; ++var4) {
                  File file = var2[var4];
                  this.plog(file, file.isDirectory() ? "<DIR>" : file.length());
                  if (file.isDirectory()) {
                     this.readDirectory(file);
                  }
               }
            } catch (Exception var9) {
               this.log(new Object[]{"Could not read file list from", dir});
            } finally {
               this.plog("</Dir", dir, ">");
            }

         }
      }

      private void plog(Object... o) {
         U.plog("+", o);
      }
   }

   private class DxDiagAheadProcessor extends Entry {
      DxDiagAheadProcessor() {
         super(CrashManager.this, "dxdiag ahead processor");
      }

      protected void execute() throws Exception {
         try {
            DXDiagScanner.getInstance().getResult();
         } catch (Exception var2) {
         }

      }
   }

   private class GeneratedFilesSearcher extends Entry {
      GeneratedFilesSearcher() {
         super(CrashManager.this, "generated files searcher");
      }

      protected void execute() throws Exception {
         Scanner scanner = PatternEntry.getScanner(CrashManager.this.getOutput());

         while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String crashFile = this.get(Pattern.compile("^.*#@!@# Game crashed!.+@!@# (.+)$"), line);
            if (crashFile != null) {
               CrashManager.this.crash.setCrashFile(crashFile);
            } else if (line.equals("# An error report file with more information is saved as:") && scanner.hasNextLine()) {
               String nativeCrashFile = this.get(Pattern.compile("# (.+)$"), scanner.nextLine());
               if (nativeCrashFile != null) {
                  CrashManager.this.crash.setNativeCrashFile(nativeCrashFile);
               }
            }
         }

      }

      private String get(Pattern pattern, String line) {
         Matcher matcher = pattern.matcher(line);
         return matcher.matches() && matcher.groupCount() == 1 ? matcher.group(1) : null;
      }
   }

   private class CrashEntryException extends Exception {
      CrashEntryException(IEntry entry, Throwable cause) {
         super(entry.toString(), cause);
      }
   }

   private class CrashManagerInterrupted extends Exception {
      CrashManagerInterrupted() {
      }

      CrashManagerInterrupted(Throwable cause) {
         super(cause);
      }
   }

   private class Executor extends ExtendedThread {
      private Exception error;

      Executor() {
         this.startAndWait();
      }

      private void scan() throws CrashManager.CrashManagerInterrupted, CrashManager.CrashEntryException, IOException {
         Object timer = Time.start(new Object());
         CrashManager.this.setupActions();
         CrashManager.this.setupEntries();
         boolean foundCapable = false;
         Iterator var3 = CrashManager.this.crashEntries.values().iterator();

         while(true) {
            while(var3.hasNext()) {
               IEntry entry = (IEntry)var3.next();
               if (CrashManager.this.cancelled) {
                  throw CrashManager.this.new CrashManagerInterrupted();
               }

               if (!foundCapable && entry instanceof CrashEntry) {
                  CrashManager.this.log("Checking entry:", entry.getName());

                  boolean capable;
                  try {
                     capable = ((CrashEntry)entry).checkCapability();
                  } catch (Exception var7) {
                     throw CrashManager.this.new CrashEntryException(entry, var7);
                  }

                  if (capable) {
                     CrashManager.this.log("Found capable:", entry.getName());
                     CrashManager.this.crash.setEntry((CrashEntry)entry);
                     foundCapable = true;
                  }
               } else if (entry instanceof Entry) {
                  CrashManager.this.log("Executing:", entry.getName());

                  try {
                     ((Entry)entry).execute();
                  } catch (Exception var8) {
                     throw CrashManager.this.new CrashEntryException(entry, var8);
                  }
               }
            }

            CrashManager.this.log("Done in", Time.stop(timer), "ms");
            return;
         }
      }

      public void run() {
         this.lockThread("start");

         try {
            this.scan();
         } catch (Exception var2) {
            this.error = var2;
         }

      }
   }

   private class Watchdog extends ExtendedThread {
      private CrashManager.Executor executor = CrashManager.this.new Executor();

      Watchdog() {
         this.startAndWait();
      }

      boolean isWorking() {
         return this.isAlive() && !this.isThreadLocked();
      }

      public void run() {
         this.lockThread("start");
         Iterator var1 = CrashManager.this.listeners.iterator();

         CrashManagerListener listener;
         while(var1.hasNext()) {
            listener = (CrashManagerListener)var1.next();
            listener.onCrashManagerProcessing(CrashManager.this);
         }

         CrashManagerListener listenerx;
         Iterator var7;
         try {
            this.watchExecutor();
         } catch (CrashManager.CrashManagerInterrupted var4) {
            var7 = CrashManager.this.listeners.iterator();

            while(var7.hasNext()) {
               listenerx = (CrashManagerListener)var7.next();
               listenerx.onCrashManagerCancelled(CrashManager.this);
            }

            return;
         } catch (Exception var5) {
            Exception e = var5;
            var7 = CrashManager.this.listeners.iterator();

            while(var7.hasNext()) {
               listenerx = (CrashManagerListener)var7.next();
               listenerx.onCrashManagerFailed(CrashManager.this, e);
            }

            return;
         }

         var1 = CrashManager.this.listeners.iterator();

         while(var1.hasNext()) {
            listener = (CrashManagerListener)var1.next();
            listener.onCrashManagerComplete(CrashManager.this, CrashManager.this.crash);
         }

      }

      private void watchExecutor() throws CrashManager.CrashManagerInterrupted, Exception {
         this.executor.unlockThread("start");

         try {
            this.executor.join();
         } catch (InterruptedException var2) {
            throw CrashManager.this.new CrashManagerInterrupted(var2);
         }

         if (this.executor.error != null) {
            if (this.executor.error instanceof CrashManager.CrashManagerInterrupted) {
               throw (CrashManager.CrashManagerInterrupted)this.executor.error;
            } else {
               throw this.executor.error;
            }
         }
      }
   }
}
