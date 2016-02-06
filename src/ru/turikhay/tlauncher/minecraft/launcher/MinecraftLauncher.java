package ru.turikhay.tlauncher.minecraft.launcher;

import com.google.gson.Gson;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;
import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ExtractRules;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.downloader.AbortedDownloadException;
import ru.turikhay.tlauncher.downloader.DownloadableContainer;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.managers.AssetsManager;
import ru.turikhay.tlauncher.managers.ComponentManager;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.managers.ServerListManager;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionSyncInfoContainer;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.crash.CrashDescriptor;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.console.Console;
import ru.turikhay.tlauncher.updater.Stats;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;
import ru.turikhay.util.stream.LinkedOutputStringStream;
import ru.turikhay.util.stream.PrintLogger;

public class MinecraftLauncher implements JavaProcessListener {
   private boolean working;
   private boolean killed;
   private final Thread parentThread;
   private final Gson gson;
   private final DateTypeAdapter dateAdapter;
   private final Downloader downloader;
   private final Configuration settings;
   private final boolean forceUpdate;
   private final boolean assistLaunch;
   private final VersionManager vm;
   private final AssetsManager am;
   private final ProfileManager pm;
   private final StringBuffer output;
   private final PrintLogger logger;
   private Console console;
   private final MinecraftLauncher.ConsoleVisibility consoleVis;
   private CrashDescriptor descriptor;
   private final List listeners;
   private final List extListeners;
   private final List assistants;
   private MinecraftLauncher.MinecraftLauncherStep step;
   private String versionName;
   private VersionSyncInfo versionSync;
   private CompleteVersion version;
   private CompleteVersion deJureVersion;
   private String accountName;
   private Account account;
   private String cmd;
   private String family;
   private File rootDir;
   private File gameDir;
   private File localAssetsDir;
   private File nativeDir;
   private File globalAssetsDir;
   private File assetsIndexesDir;
   private File assetsObjectsDir;
   private int[] windowSize;
   private boolean fullScreen;
   private boolean fullCommand;
   private int ramSize;
   private JavaProcessLauncher launcher;
   private String javaArgs;
   private String programArgs;
   private boolean minecraftWorking;
   private long startupTime;
   private int exitCode;
   private ServerList.Server server;
   private JavaProcess process;
   private boolean firstLine;

   public boolean isLaunchAssist() {
      return this.assistLaunch;
   }

   public String getOutput() {
      return this.console != null ? this.console.getOutput() : (this.output != null ? this.output.toString() : null);
   }

   public boolean isWorking() {
      return this.working;
   }

   private MinecraftLauncher(ComponentManager manager, Downloader downloader, Configuration configuration, boolean forceUpdate, MinecraftLauncher.ConsoleVisibility visibility, boolean exit) {
      this.firstLine = true;
      if (manager == null) {
         throw new NullPointerException("Ti sovsem s duba ruhnul?");
      } else if (downloader == null) {
         throw new NullPointerException("Downloader is NULL!");
      } else if (configuration == null) {
         throw new NullPointerException("Configuration is NULL!");
      } else if (visibility == null) {
         throw new NullPointerException("ConsoleVisibility is NULL!");
      } else {
         this.parentThread = Thread.currentThread();
         this.gson = new Gson();
         this.dateAdapter = new DateTypeAdapter();
         this.downloader = downloader;
         this.settings = configuration;
         this.assistants = manager.getComponentsOf(MinecraftLauncherAssistant.class);
         this.vm = (VersionManager)manager.getComponent(VersionManager.class);
         this.am = (AssetsManager)manager.getComponent(AssetsManager.class);
         this.pm = (ProfileManager)manager.getComponent(ProfileManager.class);
         this.forceUpdate = forceUpdate;
         this.assistLaunch = !exit;
         this.consoleVis = visibility;
         this.logger = this.consoleVis.equals(MinecraftLauncher.ConsoleVisibility.NONE) ? null : new PrintLogger(new LinkedOutputStringStream());
         this.console = this.logger == null ? null : new Console(this.settings, this.logger, "Minecraft", this.consoleVis.equals(MinecraftLauncher.ConsoleVisibility.ALWAYS) && this.assistLaunch);
         this.output = this.console == null ? new StringBuffer() : null;
         if (this.console != null) {
            this.console.frame.addWindowListener(new WindowAdapter() {
               public void windowClosing(WindowEvent e) {
                  Console con = MinecraftLauncher.this.console;
                  MinecraftLauncher.this.console = null;
                  con.kill();
               }
            });
         }

         this.descriptor = new CrashDescriptor(this);
         this.listeners = Collections.synchronizedList(new ArrayList());
         this.extListeners = Collections.synchronizedList(new ArrayList());
         this.step = MinecraftLauncher.MinecraftLauncherStep.NONE;
         this.log("Minecraft Launcher [18;8] has initialized");
         this.log("Running under TLauncher " + TLauncher.getVersion() + " " + TLauncher.getBrand());
         this.log("Current machine:", OS.getSummary());
      }
   }

   public MinecraftLauncher(TLauncher t, boolean forceUpdate) {
      this(t.getManager(), t.getDownloader(), t.getSettings(), forceUpdate, t.getSettings().getConsoleType().getVisibility(), t.getSettings().getActionOnLaunch() == Configuration.ActionOnLaunch.EXIT);
   }

   public void addListener(MinecraftListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         if (listener instanceof MinecraftExtendedListener) {
            this.extListeners.add((MinecraftExtendedListener)listener);
         }

         this.listeners.add(listener);
      }
   }

   public void start() {
      this.checkWorking();
      this.working = true;

      try {
         this.collectInfo();
      } catch (Throwable var6) {
         Throwable e = var6;
         this.log("Error occurred:", var6);
         Iterator listener1;
         if (var6 instanceof MinecraftException) {
            MinecraftException listener2 = (MinecraftException)var6;
            listener1 = this.listeners.iterator();

            while(listener1.hasNext()) {
               MinecraftListener listener3 = (MinecraftListener)listener1.next();
               listener3.onMinecraftKnownError(listener2);
            }
         } else {
            MinecraftListener listener;
            if (var6 instanceof MinecraftLauncher.MinecraftLauncherAborted) {
               listener1 = this.listeners.iterator();

               while(listener1.hasNext()) {
                  listener = (MinecraftListener)listener1.next();
                  listener.onMinecraftAbort();
               }
            } else {
               listener1 = this.listeners.iterator();

               while(listener1.hasNext()) {
                  listener = (MinecraftListener)listener1.next();
                  listener.onMinecraftError(e);
               }
            }
         }
      }

      this.working = false;
      this.step = MinecraftLauncher.MinecraftLauncherStep.NONE;
      this.log("Launcher stopped.");
   }

   public void stop() {
      if (this.step == MinecraftLauncher.MinecraftLauncherStep.NONE) {
         throw new IllegalStateException();
      } else {
         if (this.step == MinecraftLauncher.MinecraftLauncherStep.DOWNLOADING) {
            this.downloader.stopDownload();
         }

         this.working = false;
      }
   }

   public String getVersion() {
      return this.versionName;
   }

   public int getExitCode() {
      return this.exitCode;
   }

   public void setServer(ServerList.Server server) {
      this.checkWorking();
      this.server = server;
   }

   private void collectInfo() throws MinecraftException {
      this.checkStep(MinecraftLauncher.MinecraftLauncherStep.NONE, MinecraftLauncher.MinecraftLauncherStep.COLLECTING);
      this.log("Collecting info...");
      Iterator command = this.listeners.iterator();

      while(command.hasNext()) {
         MinecraftListener type = (MinecraftListener)command.next();
         type.onMinecraftPrepare();
      }

      command = this.extListeners.iterator();

      while(command.hasNext()) {
         MinecraftExtendedListener type1 = (MinecraftExtendedListener)command.next();
         type1.onMinecraftCollecting();
      }

      this.log("Force update:", this.forceUpdate);
      this.versionName = this.settings.get("login.version");
      if (this.versionName != null && !this.versionName.isEmpty()) {
         this.log("Selected version:", this.versionName);
         this.accountName = this.settings.get("login.account");
         if (this.accountName != null && !this.accountName.isEmpty()) {
            Account.AccountType type2 = (Account.AccountType)Reflect.parseEnum(Account.AccountType.class, this.settings.get("login.account.type"));
            this.account = this.pm.getAuthDatabase().getByUsername(this.accountName, type2);
            if (this.account == null) {
               this.account = new Account(this.accountName);
            }

            this.log("Selected account:", this.account.toDebugString());
            this.versionSync = this.vm.getVersionSyncInfo(this.versionName);
            if (this.versionSync == null) {
               throw new IllegalArgumentException("Cannot find version " + this.version);
            } else {
               this.log("Version sync info:", this.versionSync);

               try {
                  this.deJureVersion = this.versionSync.resolveCompleteVersion(this.vm, this.forceUpdate);
               } catch (IOException var12) {
                  throw new RuntimeException("Cannot get complete version!", var12);
               }

               if (this.deJureVersion == null) {
                  throw new NullPointerException("Could not get complete version");
               } else {
                  if (this.account.getType() == Account.AccountType.ELY) {
                     this.version = TLauncher.getInstance().getElyManager().elyficate(this.deJureVersion);
                  } else {
                     this.version = this.deJureVersion;
                  }

                  if (this.console != null) {
                     this.console.setName(this.version.getID());
                  }

                  this.family = this.version.getFamily();
                  if (StringUtils.isEmpty(this.family)) {
                     this.family = "unknown";
                  }

                  String command1 = this.settings.get("minecraft.cmd");
                  this.cmd = command1 == null ? OS.getJavaPath() : command1;
                  this.log("Command:", this.cmd);
                  this.rootDir = new File(this.settings.get("minecraft.gamedir"));
                  if (this.settings.getBoolean("minecraft.gamedir.separate")) {
                     this.gameDir = new File(this.rootDir, "home/" + this.family);
                  } else {
                     this.gameDir = this.rootDir;
                  }

                  try {
                     FileUtil.createFolder(this.rootDir);
                  } catch (Exception var11) {
                     throw new MinecraftException("Cannot create working directory!", "folder-not-found", var11);
                  }

                  try {
                     FileUtil.createFolder(this.gameDir);
                  } catch (Exception var10) {
                     throw new MinecraftException("Cannot create game directory!", "folder-not-found", var10);
                  }

                  this.log("Root directory:", this.rootDir);
                  this.log("Game directory:", this.gameDir);
                  this.globalAssetsDir = new File(this.rootDir, "assets");

                  try {
                     FileUtil.createFolder(this.globalAssetsDir);
                  } catch (IOException var9) {
                     throw new RuntimeException("Cannot create assets directory!", var9);
                  }

                  this.assetsIndexesDir = new File(this.globalAssetsDir, "indexes");

                  try {
                     FileUtil.createFolder(this.assetsIndexesDir);
                  } catch (IOException var8) {
                     throw new RuntimeException("Cannot create assets indexes directory!", var8);
                  }

                  this.assetsObjectsDir = new File(this.globalAssetsDir, "objects");

                  try {
                     FileUtil.createFolder(this.assetsObjectsDir);
                  } catch (IOException var7) {
                     throw new RuntimeException("Cannot create assets objects directory!", var7);
                  }

                  this.nativeDir = new File(this.rootDir, "versions/" + this.version.getID() + "/" + "natives");

                  try {
                     FileUtil.createFolder(this.nativeDir);
                  } catch (IOException var6) {
                     throw new RuntimeException("Cannot create native files directory!", var6);
                  }

                  this.javaArgs = this.settings.get("minecraft.javaargs");
                  if (this.javaArgs != null && this.javaArgs.isEmpty()) {
                     this.javaArgs = null;
                  }

                  this.programArgs = this.settings.get("minecraft.args");
                  if (this.programArgs != null && this.programArgs.isEmpty()) {
                     this.programArgs = null;
                  }

                  this.windowSize = this.settings.getClientWindowSize();
                  if (this.windowSize[0] < 1) {
                     throw new IllegalArgumentException("Invalid window width!");
                  } else if (this.windowSize[1] < 1) {
                     throw new IllegalArgumentException("Invalid window height!");
                  } else {
                     this.fullScreen = this.settings.getBoolean("minecraft.fullscreen");
                     this.ramSize = this.settings.getInteger("minecraft.memory");
                     if (this.ramSize < 512) {
                        throw new IllegalArgumentException("Invalid RAM size!");
                     } else {
                        this.fullCommand = this.settings.getBoolean("gui.console.fullcommand");
                        Iterator var4 = this.assistants.iterator();

                        while(var4.hasNext()) {
                           MinecraftLauncherAssistant assistant = (MinecraftLauncherAssistant)var4.next();
                           assistant.collectInfo();
                        }

                        this.log("Checking conditions...");
                        if (this.version.getMinimumCustomLauncherVersion() > 8) {
                           throw new MinecraftException("Alternative launcher is incompatible with launching version!", "incompatible", new Object[0]);
                        } else {
                           if (this.version.getMinimumCustomLauncherVersion() == 0 && this.version.getMinimumLauncherVersion() > 18) {
                              Alert.showLocWarning("launcher.warning.title", "launcher.warning.incompatible.launcher", (Object)null);
                           }

                           if (!this.version.appliesToCurrentEnvironment()) {
                              Alert.showLocWarning("launcher.warning.title", "launcher.warning.incompatible.environment", (Object)null);
                           }

                           this.downloadResources();
                        }
                     }
                  }
               }
            }
         } else {
            throw new IllegalArgumentException("Account is NULL or empty!");
         }
      } else {
         throw new IllegalArgumentException("Version name is NULL or empty!");
      }
   }

   public File getGameDir() {
      return this.gameDir;
   }

   private void downloadResources() throws MinecraftException {
      this.checkStep(MinecraftLauncher.MinecraftLauncherStep.COLLECTING, MinecraftLauncher.MinecraftLauncherStep.DOWNLOADING);
      Iterator execContainer = this.extListeners.iterator();

      while(execContainer.hasNext()) {
         MinecraftExtendedListener assets = (MinecraftExtendedListener)execContainer.next();
         assets.onMinecraftComparingAssets();
      }

      List assets1 = this.compareAssets();
      Iterator listenerContainer = this.extListeners.iterator();

      while(listenerContainer.hasNext()) {
         MinecraftExtendedListener execContainer1 = (MinecraftExtendedListener)listenerContainer.next();
         execContainer1.onMinecraftDownloading();
      }

      VersionSyncInfoContainer versionContainer;
      try {
         versionContainer = this.vm.downloadVersion(this.versionSync, this.account.getType() == Account.AccountType.ELY, this.forceUpdate);
      } catch (IOException var10) {
         throw new MinecraftException("Cannot download version!", "download-jar", var10);
      }

      DownloadableContainer assetsContainer = this.am.downloadResources(this.version, assets1);
      versionContainer.setConsole(this.console);
      assetsContainer.setConsole(this.console);
      this.checkAborted();
      if (assetsContainer != null) {
         this.downloader.add(assetsContainer);
      }

      this.downloader.add((DownloadableContainer)versionContainer);
      Iterator message = this.assistants.iterator();

      while(message.hasNext()) {
         MinecraftLauncherAssistant e = (MinecraftLauncherAssistant)message.next();
         e.collectResources(this.downloader);
      }

      this.downloader.startDownloadAndWait();
      if (versionContainer.isAborted()) {
         throw new MinecraftLauncher.MinecraftLauncherAborted(new AbortedDownloadException());
      } else if (!versionContainer.getErrors().isEmpty()) {
         boolean e1 = versionContainer.getErrors().size() == 1;
         StringBuilder message1 = new StringBuilder();
         message1.append(versionContainer.getErrors().size()).append(" error").append(e1 ? "" : "s").append(" occurred while trying to download binaries.");
         if (!e1) {
            message1.append(" Cause is the first of them.");
         }

         throw new MinecraftException(message1.toString(), "download", (Throwable)versionContainer.getErrors().get(0));
      } else {
         try {
            this.vm.getLocalList().saveVersion(this.deJureVersion);
         } catch (IOException var9) {
            this.log("Cannot save version!", var9);
         }

         this.constructProcess();
      }
   }

   private void constructProcess() throws MinecraftException {
      this.checkStep(MinecraftLauncher.MinecraftLauncherStep.DOWNLOADING, MinecraftLauncher.MinecraftLauncherStep.CONSTRUCTING);
      Iterator address = this.extListeners.iterator();

      MinecraftExtendedListener assistant;
      while(address.hasNext()) {
         assistant = (MinecraftExtendedListener)address.next();
         assistant.onMinecraftReconstructingAssets();
      }

      try {
         this.localAssetsDir = this.reconstructAssets();
      } catch (IOException var13) {
         throw new MinecraftException("Cannot reconstruct assets!", "reconstruct-assets", var13);
      }

      address = this.extListeners.iterator();

      while(address.hasNext()) {
         assistant = (MinecraftExtendedListener)address.next();
         assistant.onMinecraftUnpackingNatives();
      }

      try {
         this.unpackNatives(this.forceUpdate);
      } catch (IOException var12) {
         throw new MinecraftException("Cannot unpack natives!", "unpack-natives", var12);
      }

      this.checkAborted();
      address = this.extListeners.iterator();

      while(address.hasNext()) {
         assistant = (MinecraftExtendedListener)address.next();
         assistant.onMinecraftDeletingEntries();
      }

      try {
         this.deleteEntries();
      } catch (IOException var11) {
         throw new MinecraftException("Cannot delete entries!", "delete-entries", var11);
      }

      try {
         this.deleteLibraryEntries();
      } catch (Exception var10) {
         throw new MinecraftException("Cannot delete library entries!", "delete-entries", var10);
      }

      this.checkAborted();
      this.log("Constructing process...");
      address = this.extListeners.iterator();

      while(address.hasNext()) {
         assistant = (MinecraftExtendedListener)address.next();
         assistant.onMinecraftConstructing();
      }

      this.launcher = new JavaProcessLauncher(this.cmd, new String[0]);
      this.launcher.directory(this.gameDir);
      if (OS.OSX.isCurrent()) {
         File assistant1 = null;

         try {
            assistant1 = this.getAssetObject("icons/minecraft.icns");
         } catch (IOException var9) {
            this.log("Cannot get icon file from assets.", var9);
         }

         if (assistant1 != null) {
            this.launcher.addCommand("-Xdock:icon=\"" + assistant1.getAbsolutePath() + "\"", "-Xdock:name=Minecraft");
         }
      }

      this.launcher.addCommand("-XX:HeapDumpPath=ThisTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
      this.launcher.addCommand("-Xmx" + this.ramSize + "M");
      this.launcher.addCommand("-Djava.library.path=" + this.nativeDir.getAbsolutePath());
      this.launcher.addCommand("-cp", this.constructClassPath(this.version));
      this.launcher.addCommand("-Dfml.ignoreInvalidMinecraftCertificates=true");
      this.launcher.addCommand("-Dfml.ignorePatchDiscrepancies=true");
      this.launcher.addCommands(this.getJVMArguments());
      if (this.javaArgs != null) {
         this.launcher.addSplitCommands(this.javaArgs);
      }

      address = this.assistants.iterator();

      MinecraftLauncherAssistant assistant2;
      while(address.hasNext()) {
         assistant2 = (MinecraftLauncherAssistant)address.next();
         assistant2.constructJavaArguments();
      }

      this.launcher.addCommand(this.version.getMainClass());
      if (!this.fullCommand) {
         this.log("Half command (characters are not escaped):\n" + this.launcher.getCommandsAsString());
      }

      this.launcher.addCommands(this.getMinecraftArguments());
      this.launcher.addCommand("--width", this.windowSize[0]);
      this.launcher.addCommand("--height", this.windowSize[1]);
      if (this.fullScreen) {
         this.launcher.addCommand("--fullscreen");
      }

      try {
         File serversDat = new File(this.gameDir, "servers.dat");
         if (serversDat.isFile()) {
            FileUtil.copyFile(serversDat, new File(serversDat.getAbsolutePath() + ".bak"), true);
         }
      } catch (IOException var8) {
         this.log("Could not make backup for servers.dat", var8);
      }

      try {
         this.fixResourceFolder();
      } catch (IOException var7) {
         this.log("Cannot create resource folder. This could have fixed [MCL-3732].", var7);
      }

      if (this.server != null) {
         ServerList assistant3 = new ServerList();
         assistant3.add(this.server);

         try {
            ServerListManager.reconstructList(assistant3, new File(this.gameDir, "servers.dat"));
         } catch (Exception var6) {
            this.log("Couldn't reconstruct server list.", var6);
         }

         String[] address1 = StringUtils.split(this.server.getAddress(), ':');
         switch(address1.length) {
         case 2:
            this.launcher.addCommand("--port", address1[1]);
         case 1:
            this.launcher.addCommand("--server", address1[0]);
            break;
         default:
            this.log("Cannot recognize server:", this.server);
         }
      }

      if (this.programArgs != null) {
         this.launcher.addSplitCommands(this.programArgs);
      }

      address = this.assistants.iterator();

      while(address.hasNext()) {
         assistant2 = (MinecraftLauncherAssistant)address.next();
         assistant2.constructProgramArguments();
      }

      if (this.fullCommand) {
         this.log("Full command (characters are not escaped):\n" + this.launcher.getCommandsAsString());
      }

      this.launchMinecraft();
   }

   private File reconstructAssets() throws IOException, MinecraftException {
      String assetVersion = this.version.getAssetIndex().getId();
      File indexFile = new File(this.assetsIndexesDir, assetVersion + ".json");
      File virtualRoot = new File(new File(this.globalAssetsDir, "virtual"), assetVersion);
      if (!indexFile.isFile()) {
         this.log("No assets index file " + virtualRoot + "; can't reconstruct assets");
         return virtualRoot;
      } else {
         AssetIndex index;
         try {
            index = (AssetIndex)this.gson.fromJson((Reader)(new FileReader(indexFile)), (Class)AssetIndex.class);
         } catch (Exception var9) {
            throw new MinecraftException("Cannot read index file!", "index-file", var9);
         }

         if (index.isVirtual()) {
            this.log("Reconstructing virtual assets folder at " + virtualRoot);
            Iterator var6 = index.getFileMap().entrySet().iterator();

            while(true) {
               while(var6.hasNext()) {
                  Entry entry = (Entry)var6.next();
                  this.checkAborted();
                  File target = new File(virtualRoot, (String)entry.getKey());
                  File original = new File(new File(this.assetsObjectsDir, ((AssetIndex.AssetObject)entry.getValue()).getHash().substring(0, 2)), ((AssetIndex.AssetObject)entry.getValue()).getHash());
                  if (!original.isFile()) {
                     this.log("Skipped reconstructing:", original);
                  } else if (this.forceUpdate || !target.isFile()) {
                     FileUtils.copyFile(original, target, false);
                     this.log(original, "->", target);
                  }
               }

               FileUtil.writeFile(new File(virtualRoot, ".lastused"), this.dateAdapter.toString(new Date()));
               break;
            }
         }

         return virtualRoot;
      }
   }

   private File getAssetObject(String name) throws IOException {
      String assetVersion = this.version.getAssetIndex().getId();
      File indexFile = new File(this.assetsIndexesDir, assetVersion + ".json");
      AssetIndex index = (AssetIndex)this.gson.fromJson(FileUtil.readFile(indexFile), AssetIndex.class);
      if (index.getFileMap() == null) {
         throw new IOException("Cannot get filemap!");
      } else {
         String hash = ((AssetIndex.AssetObject)index.getFileMap().get(name)).getHash();
         return new File(this.assetsObjectsDir, hash.substring(0, 2) + "/" + hash);
      }
   }

   private void unpackNatives(boolean force) throws IOException {
      this.log("Unpacking natives...");
      Collection libraries = this.version.getRelevantLibraries();
      OS os = OS.CURRENT;
      ZipFile zip = null;
      if (force) {
         this.nativeDir.delete();
      }

      Iterator var7 = libraries.iterator();

      label78:
      while(true) {
         Library library;
         Map nativesPerOs;
         do {
            do {
               if (!var7.hasNext()) {
                  return;
               }

               library = (Library)var7.next();
               nativesPerOs = library.getNatives();
            } while(nativesPerOs == null);
         } while(nativesPerOs.get(os) == null);

         File file = new File(MinecraftUtil.getWorkingDirectory(), "libraries/" + library.getArtifactPath((String)nativesPerOs.get(os)));
         if (!file.isFile()) {
            throw new IOException("Required archive doesn't exist: " + file.getAbsolutePath());
         }

         try {
            zip = new ZipFile(file);
         } catch (IOException var18) {
            throw new IOException("Error opening ZIP archive: " + file.getAbsolutePath(), var18);
         }

         ExtractRules extractRules = library.getExtractRules();
         Enumeration entries = zip.entries();

         while(true) {
            ZipEntry entry;
            File targetFile;
            do {
               do {
                  do {
                     if (!entries.hasMoreElements()) {
                        zip.close();
                        continue label78;
                     }

                     entry = (ZipEntry)entries.nextElement();
                  } while(extractRules != null && !extractRules.shouldExtract(entry.getName()));

                  targetFile = new File(this.nativeDir, entry.getName());
               } while(!force && targetFile.isFile());

               if (targetFile.getParentFile() != null) {
                  targetFile.getParentFile().mkdirs();
               }
            } while(entry.isDirectory());

            BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));
            byte[] buffer = new byte[2048];
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

            int length;
            while((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
               bufferedOutputStream.write(buffer, 0, length);
            }

            inputStream.close();
            bufferedOutputStream.close();
         }
      }
   }

   private void deleteEntries() throws IOException {
      List entries = this.version.getDeleteEntries();
      if (entries != null && entries.size() != 0) {
         this.log("Removing entries...");
         File file = this.version.getFile(this.rootDir);
         this.removeFrom(file, entries);
      }

   }

   private void deleteLibraryEntries() throws IOException {
      Iterator var2 = this.version.getLibraries().iterator();

      while(var2.hasNext()) {
         Library lib = (Library)var2.next();
         List entries = lib.getDeleteEntriesList();
         if (entries != null && !entries.isEmpty()) {
            this.log("Processing entries of", lib.getName());
            this.removeFrom(new File(this.rootDir, "libraries/" + lib.getArtifactPath()), entries);
         }
      }

   }

   private String constructClassPath(CompleteVersion version) throws MinecraftException {
      this.log("Constructing classpath...");
      StringBuilder result = new StringBuilder();
      Collection classPath = version.getClassPath(OS.CURRENT, this.rootDir);
      String separator = System.getProperty("path.separator");

      File file;
      for(Iterator var6 = classPath.iterator(); var6.hasNext(); result.append(file.getAbsolutePath())) {
         file = (File)var6.next();
         if (!file.isFile()) {
            throw new MinecraftException("Classpath is not found: " + file, "classpath", new Object[]{file});
         }

         if (result.length() > 0) {
            result.append(separator);
         }
      }

      return result.toString();
   }

   private String[] getMinecraftArguments() throws MinecraftException {
      this.log("Getting Minecraft arguments...");
      if (this.version.getMinecraftArguments() == null) {
         throw new MinecraftException("Can't run version, missing minecraftArguments", "noArgs", new Object[0]);
      } else {
         HashMap map = new HashMap();
         StrSubstitutor substitutor = new StrSubstitutor(map);
         String assets = this.version.getAssetIndex().getId();
         String[] split = this.version.getMinecraftArguments().split(" ");
         map.put("auth_username", this.accountName);
         if (!this.account.isFree()) {
            map.put("auth_session", String.format("token:%s:%s", this.account.getAccessToken(), this.account.getProfile().getId()));
            map.put("auth_access_token", this.account.getAccessToken());
            map.put("user_properties", this.gson.toJson((Object)this.account.getProperties()));
            map.put("auth_player_name", this.account.getDisplayName());
            map.put("auth_uuid", this.account.getUUID());
            map.put("user_type", "mojang");
            map.put("profile_name", this.account.getProfile().getName());
         } else {
            map.put("auth_session", "null");
            map.put("auth_access_token", "null");
            map.put("user_properties", "[]");
            map.put("auth_player_name", this.accountName);
            map.put("auth_uuid", (new UUID(0L, 0L)).toString());
            map.put("user_type", "legacy");
            map.put("profile_name", "(Default)");
         }

         map.put("version_name", this.version.getID());
         map.put("version_type", this.version.getReleaseType());
         map.put("game_directory", this.gameDir.getAbsolutePath());
         map.put("game_assets", this.localAssetsDir.getAbsolutePath());
         map.put("assets_root", this.globalAssetsDir.getAbsolutePath());
         map.put("assets_index_name", assets == null ? "legacy" : assets);

         for(int i = 0; i < split.length; ++i) {
            split[i] = substitutor.replace(split[i]);
         }

         return split;
      }
   }

   private String[] getJVMArguments() {
      List args = new ArrayList();
      if (this.settings.getBoolean("minecraft.improvedargs")) {
         args.add("-Xmn128M");
         args.add("-XX:+UseConcMarkSweepGC");
         args.add("-XX:-UseAdaptiveSizePolicy");
         args.add("-XX:-UseGCOverheadLimit");
         args.add("-XX:+CMSParallelRemarkEnabled");
         args.add("-XX:+ParallelRefProcEnabled");
         args.add("-XX:+CMSClassUnloadingEnabled");
         args.add("-XX:+UseCMSInitiatingOccupancyOnly");
         args.add("-Xms256M");
      }

      String rawArgs = this.version.getJVMArguments();
      if (StringUtils.isNotEmpty(rawArgs)) {
         args.addAll(Arrays.asList(StringUtils.split(rawArgs, ' ')));
      }

      return (String[])args.toArray(new String[args.size()]);
   }

   private List compareAssets() throws MinecraftException {
      try {
         this.migrateOldAssets();
      } catch (Exception var8) {
         throw new MinecraftException("Could not migrate old assets", "migrate-assets", var8);
      }

      this.log("Comparing assets...");
      long start = System.nanoTime();
      List result = this.am.checkResources(this.version, !this.forceUpdate);
      long end = System.nanoTime();
      long delta = end - start;
      this.log("Delta time to compare assets: " + delta / 1000000L + " ms.");
      return result;
   }

   private void migrateOldAssets() {
      if (this.globalAssetsDir.isDirectory()) {
         File skinsDir = new File(this.globalAssetsDir, "skins");
         if (skinsDir.isDirectory()) {
            FileUtil.deleteDirectory(skinsDir);
         }

         IOFileFilter migratableFilter = FileFilterUtils.notFileFilter(FileFilterUtils.or(FileFilterUtils.nameFileFilter("indexes"), FileFilterUtils.nameFileFilter("objects"), FileFilterUtils.nameFileFilter("virtual")));

         File assets;
         for(Iterator file = (new TreeSet(FileUtils.listFiles(this.globalAssetsDir, TrueFileFilter.TRUE, migratableFilter))).iterator(); file.hasNext(); FileUtils.deleteQuietly(assets)) {
            assets = (File)file.next();
            String hash = FileUtil.getDigest(assets, "SHA-1", 40);
            File destinationFile = new File(this.assetsObjectsDir, hash.substring(0, 2) + "/" + hash);
            if (!destinationFile.exists()) {
               this.log("Migrated old asset", assets, "into", destinationFile);

               try {
                  FileUtils.copyFile(assets, destinationFile);
               } catch (IOException var9) {
                  this.log("Couldn't migrate old asset", var9);
               }
            }
         }

         File[] var9 = this.globalAssetsDir.listFiles();
         if (var9 != null) {
            File[] e = var9;
            int var12 = var9.length;

            for(int var11 = 0; var11 < var12; ++var11) {
               File var10 = e[var11];
               if (!var10.getName().equals("indexes") && !var10.getName().equals("objects") && !var10.getName().equals("virtual")) {
                  this.log("Cleaning up old assets directory", var10, "after migration");
                  FileUtils.deleteQuietly(var10);
               }
            }
         }
      }

   }

   private void fixResourceFolder() throws IOException {
      File serverResourcePacksFolder = new File(this.gameDir, "server-resource-packs");
      if (serverResourcePacksFolder.isDirectory()) {
         File[] arr$ = serverResourcePacksFolder.listFiles();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            File file = arr$[i$];
            U.log(file, file.length());
            if (file.length() == 0L) {
               FileUtil.deleteFile(file);
            }
         }
      }

      FileUtil.createFolder(serverResourcePacksFolder);
   }

   private void launchMinecraft() throws MinecraftException {
      this.checkStep(MinecraftLauncher.MinecraftLauncherStep.CONSTRUCTING, MinecraftLauncher.MinecraftLauncherStep.LAUNCHING);
      this.log("Launching Minecraft...");
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         MinecraftListener e = (MinecraftListener)var2.next();
         e.onMinecraftLaunch();
      }

      this.log("Starting Minecraft " + this.versionName + "...");
      this.log("Launching in:", this.gameDir.getAbsolutePath());
      this.startupTime = System.currentTimeMillis();
      TLauncher.getConsole().setLauncher(this);
      if (this.console != null) {
         Calendar e1 = Calendar.getInstance();
         e1.setTimeInMillis(this.startupTime);
         this.console.setName(this.version.getID() + " (" + (new SimpleDateFormat("yyyy-MM-dd")).format(e1.getTime()) + ")");
         this.console.setLauncher(this);
      }

      try {
         this.process = this.launcher.start();
         this.process.safeSetExitRunnable(this);
         this.minecraftWorking = true;
      } catch (Exception var3) {
         this.notifyClose();
         throw new MinecraftException("Cannot start the game!", "start", var3);
      }

      this.postLaunch();
   }

   private void postLaunch() {
      this.checkStep(MinecraftLauncher.MinecraftLauncherStep.LAUNCHING, MinecraftLauncher.MinecraftLauncherStep.POSTLAUNCH);
      this.log("Processing post-launch actions. Assist launch:", this.assistLaunch);
      Iterator var2 = this.extListeners.iterator();

      while(var2.hasNext()) {
         MinecraftExtendedListener listener = (MinecraftExtendedListener)var2.next();
         listener.onMinecraftPostLaunch();
      }

      Stats.minecraftLaunched(this.account, this.version, this.server);
      if (this.assistLaunch) {
         this.waitForClose();
      } else {
         U.sleepFor(30000L);
         if (this.minecraftWorking) {
            TLauncher.kill();
         }
      }

   }

   public void killProcess() {
      if (!this.minecraftWorking) {
         throw new IllegalStateException();
      } else {
         this.log("Killing Minecraft forcefully");
         this.killed = true;
         this.process.stop();
      }
   }

   public void plog(Object... o) {
      String text = U.toLog(o);
      if (this.console == null) {
         if (this.output != null) {
            StringBuffer var3 = this.output;
            synchronized(this.output) {
               this.output.append(text).append('\n');
            }
         }
      } else {
         this.console.log(text);
      }

   }

   public void log(Object... o) {
      U.log("[Launcher]", o);
      this.plog("[L]", o);
   }

   private void checkThread() {
      if (!Thread.currentThread().equals(this.parentThread)) {
         throw new IllegalStateException("Illegal thread!");
      }
   }

   private void checkStep(MinecraftLauncher.MinecraftLauncherStep prevStep, MinecraftLauncher.MinecraftLauncherStep currentStep) {
      this.checkAborted();
      if (prevStep != null && currentStep != null) {
         if (!this.step.equals(prevStep)) {
            throw new IllegalStateException("Called from illegal step: " + this.step);
         } else {
            this.checkThread();
            this.step = currentStep;
         }
      } else {
         throw new NullPointerException("NULL: " + prevStep + " " + currentStep);
      }
   }

   private void checkAborted() {
      if (!this.working) {
         throw new MinecraftLauncher.MinecraftLauncherAborted("Aborted at step: " + this.step);
      }
   }

   private void checkWorking() {
      if (this.working) {
         throw new IllegalStateException("Launcher is working!");
      }
   }

   public void onJavaProcessLog(JavaProcess jp, String line) {
      if (this.firstLine) {
         this.firstLine = false;
         U.plog("===============================================================================================");
         this.plog("===============================================================================================");
      }

      U.plog(">", line);
      this.plog(line);
   }

   public void onJavaProcessEnded(JavaProcess jp) {
      this.notifyClose();
      if (TLauncher.getConsole().getLauncher() == this) {
         TLauncher.getConsole().setLauncher((MinecraftLauncher)null);
      }

      if (this.console != null) {
         this.console.setLauncher((MinecraftLauncher)null);
      }

      int exit = jp.getExitCode();
      this.log("Minecraft closed with exit code: " + exit);
      this.exitCode = exit;
      Crash crash;
      if (this.killed || System.currentTimeMillis() - this.startupTime >= 5000L && exit == 0) {
         crash = null;
      } else {
         crash = this.descriptor.scan();
      }

      if (crash == null) {
         if (!this.assistLaunch) {
            TLauncher.kill();
         }

         if (this.console != null) {
            this.console.killIn(7000L);
         }
      } else {
         if (this.console != null) {
            this.console.show();
         }

         Iterator var5 = this.listeners.iterator();

         while(var5.hasNext()) {
            MinecraftListener listener = (MinecraftListener)var5.next();
            listener.onMinecraftCrash(crash);
         }
      }

   }

   private synchronized void waitForClose() {
      while(this.minecraftWorking) {
         try {
            this.wait();
         } catch (InterruptedException var2) {
         }
      }

   }

   private synchronized void notifyClose() {
      this.minecraftWorking = false;
      if (System.currentTimeMillis() - this.startupTime < 5000L) {
         U.sleepFor(1000L);
      }

      this.notifyAll();
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         MinecraftListener listener = (MinecraftListener)var2.next();
         listener.onMinecraftClose();
      }

   }

   private void removeFrom(File zipFile, List entries) throws IOException {
      File tempFile = new File(zipFile.getAbsolutePath() + "." + System.currentTimeMillis());
      tempFile.delete();
      tempFile.deleteOnExit();
      boolean renameOk = zipFile.renameTo(tempFile);
      if (!renameOk) {
         throw new IOException("Could not rename the file " + zipFile.getAbsolutePath() + " -> " + tempFile.getAbsolutePath());
      } else {
         this.log("Removing entries from", zipFile);
         byte[] buf = new byte[1024];
         ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
         ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));

         for(ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
            String name = entry.getName();
            if (entries.contains(name)) {
               this.log("Removed:", name);
            } else {
               zout.putNextEntry(new ZipEntry(name));

               int len;
               while((len = zin.read(buf)) > 0) {
                  zout.write(buf, 0, len);
               }
            }
         }

         zin.close();
         zout.close();
         tempFile.delete();
      }
   }

   public static enum MinecraftLauncherStep {
      NONE,
      COLLECTING,
      DOWNLOADING,
      CONSTRUCTING,
      LAUNCHING,
      POSTLAUNCH;
   }

   class MinecraftLauncherAborted extends RuntimeException {
      MinecraftLauncherAborted(String message) {
         super(message);
      }

      MinecraftLauncherAborted(Throwable cause) {
         super(cause);
      }
   }

   public static enum ConsoleVisibility {
      ALWAYS,
      ON_CRASH,
      NONE;
   }
}
