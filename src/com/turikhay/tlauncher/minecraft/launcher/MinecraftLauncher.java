package com.turikhay.tlauncher.minecraft.launcher;

import com.google.gson.Gson;
import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.TLauncherLite;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.downloader.AbortedDownloadException;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.DownloadableContainerHandler;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.downloader.RetryDownloadException;
import com.turikhay.tlauncher.managers.AssetsManager;
import com.turikhay.tlauncher.managers.ComponentManager;
import com.turikhay.tlauncher.managers.ProfileManager;
import com.turikhay.tlauncher.managers.VersionManager;
import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.minecraft.crash.Crash;
import com.turikhay.tlauncher.minecraft.crash.CrashDescriptor;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.console.Console;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;
import com.turikhay.util.logger.LinkedStringStream;
import com.turikhay.util.logger.PrintLogger;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import joptsimple.OptionSet;
import net.minecraft.launcher.OperatingSystem;
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
import org.apache.commons.lang3.text.StrSubstitutor;

public class MinecraftLauncher implements JavaProcessListener {
   private static final int OFFICIAL_VERSION = 14;
   private static final int ALTERNATIVE_VERSION = 6;
   private boolean working;
   private final Thread parentThread;
   private final Gson gson;
   private final DateTypeAdapter dateAdapter;
   private final OperatingSystem os;
   private final Downloader downloader;
   private final Configuration settings;
   private final boolean forceUpdate;
   private final boolean assistLaunch;
   private final VersionManager vm;
   private final AssetsManager am;
   private final ProfileManager pm;
   private final LinkedStringStream output;
   private final PrintLogger logger;
   private final Console console;
   private CrashDescriptor descriptor;
   private final List listeners;
   private final List extListeners;
   private final List assistants;
   private MinecraftLauncher.MinecraftLauncherStep step;
   private String versionName;
   private VersionSyncInfo versionSync;
   private CompleteVersion version;
   private String accountName;
   private Account account;
   private File javaDir;
   private File gameDir;
   private File localAssetsDir;
   private File nativeDir;
   private File globalAssetsDir;
   private File assetsIndexesDir;
   private File assetsObjectsDir;
   private int[] windowSize;
   private JavaProcessLauncher launcher;
   private String javaArgs;
   private String programArgs;
   private boolean minecraftWorking;
   private int exitCode;

   public Downloader getDownloader() {
      return this.downloader;
   }

   public Configuration getConfiguration() {
      return this.settings;
   }

   public boolean isForceUpdate() {
      return this.forceUpdate;
   }

   public boolean isLaunchAssist() {
      return this.assistLaunch;
   }

   public LinkedStringStream getStream() {
      return this.output;
   }

   public PrintLogger getLogger() {
      return this.logger;
   }

   public Console getConsole() {
      return this.console;
   }

   public CrashDescriptor getCrashDescriptor() {
      return this.descriptor;
   }

   public MinecraftLauncher.MinecraftLauncherStep getStep() {
      return this.step;
   }

   public boolean isWorking() {
      return this.working;
   }

   private MinecraftLauncher(ComponentManager manager, Downloader downloader, Configuration configuration, boolean forceUpdate, boolean console, boolean exit) {
      if (manager == null) {
         throw new NullPointerException("Ti ohuel?");
      } else if (downloader == null) {
         throw new NullPointerException("Downloader is NULL!");
      } else if (configuration == null) {
         throw new NullPointerException("Configuration is NULL!");
      } else {
         this.parentThread = Thread.currentThread();
         this.gson = new Gson();
         this.dateAdapter = new DateTypeAdapter();
         this.os = OperatingSystem.getCurrentPlatform();
         this.downloader = downloader;
         this.settings = configuration;
         this.assistants = manager.getComponentsOf(MinecraftLauncherAssistant.class);
         this.vm = (VersionManager)manager.getComponent(VersionManager.class);
         this.am = (AssetsManager)manager.getComponent(AssetsManager.class);
         this.pm = (ProfileManager)manager.getComponent(ProfileManager.class);
         this.forceUpdate = forceUpdate;
         this.assistLaunch = !exit;
         this.output = new LinkedStringStream();
         this.logger = new PrintLogger(this.output);
         this.console = console ? new Console(this.settings, this.logger, "Minecraft Logger", this.assistLaunch) : null;
         this.descriptor = new CrashDescriptor(this);
         this.listeners = Collections.synchronizedList(new ArrayList());
         this.extListeners = Collections.synchronizedList(new ArrayList());
         this.step = MinecraftLauncher.MinecraftLauncherStep.NONE;
         this.log("Minecraft Launcher [14;6] has initialized");
         this.log("Running under TLauncher " + TLauncher.getVersion() + " " + TLauncher.getBrand());
         this.log("Current machine:", OperatingSystem.getCurrentInfo());
      }
   }

   public MinecraftLauncher(TLauncher t, boolean forceUpdate) {
      this(t.getManager(), t.getDownloader(), t.getSettings(), forceUpdate, t.getSettings().getConsoleType() == Configuration.ConsoleType.MINECRAFT, t.getSettings().getActionOnLaunch() == Configuration.ActionOnLaunch.EXIT);
   }

   public MinecraftLauncher(TLauncherLite tl, OptionSet options) {
      this(tl.getLauncher().getManager(), tl.getLauncher().getDownloader(), tl.getLauncher().getSettings(), options.has("force"), tl.getLauncher().getSettings().getConsoleType() != Configuration.ConsoleType.GLOBAL, false);
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
      if (this.step.ordinal() > MinecraftLauncher.MinecraftLauncherStep.NONE.ordinal()) {
         throw new IllegalStateException("Launcher is already working!");
      } else {
         this.working = true;

         try {
            this.collectInfo();
         } catch (Throwable var5) {
            Throwable e = var5;
            this.log("Error occurred:", var5);
            if (var5 instanceof MinecraftException) {
               MinecraftException me = (MinecraftException)var5;
               Iterator var4 = this.listeners.iterator();

               while(var4.hasNext()) {
                  MinecraftListener listener = (MinecraftListener)var4.next();
                  listener.onMinecraftKnownError(me);
               }
            } else {
               MinecraftListener listener;
               Iterator var3;
               if (var5 instanceof AbortedDownloadException) {
                  var3 = this.listeners.iterator();

                  while(var3.hasNext()) {
                     listener = (MinecraftListener)var3.next();
                     listener.onMinecraftAbort();
                  }
               } else {
                  var3 = this.listeners.iterator();

                  while(var3.hasNext()) {
                     listener = (MinecraftListener)var3.next();
                     listener.onMinecraftError(e);
                  }
               }
            }
         }

         this.working = false;
         this.step = MinecraftLauncher.MinecraftLauncherStep.NONE;
         this.log("Launcher exited.");
      }
   }

   public String getVersion() {
      return this.versionName;
   }

   public int getExitCode() {
      return this.exitCode;
   }

   private void collectInfo() throws MinecraftException, AbortedDownloadException {
      this.checkStep(MinecraftLauncher.MinecraftLauncherStep.NONE, MinecraftLauncher.MinecraftLauncherStep.COLLECTING);
      this.log("Collecting info...");
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         MinecraftListener listener = (MinecraftListener)var2.next();
         listener.onMinecraftPrepare();
      }

      var2 = this.extListeners.iterator();

      while(var2.hasNext()) {
         MinecraftExtendedListener listener = (MinecraftExtendedListener)var2.next();
         listener.onMinecraftCollecting();
      }

      this.log("Force update:", this.forceUpdate);
      this.versionName = this.settings.get("login.version");
      if (this.versionName != null && !this.versionName.isEmpty()) {
         this.log("Selected version:", this.versionName);
         this.accountName = this.settings.get("login.account");
         if (this.accountName != null && !this.accountName.isEmpty()) {
            this.account = this.pm.getAuthDatabase().getByUsername(this.accountName);
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
                  this.version = this.versionSync.getCompleteVersion(this.forceUpdate);
               } catch (IOException var9) {
                  throw new RuntimeException("Cannot get complete version!");
               }

               if (this.version == null) {
                  throw new NullPointerException("Complete version is NULL");
               } else {
                  String javaDirPath = this.settings.get("minecraft.javadir");
                  this.javaDir = new File(javaDirPath == null ? this.os.getJavaDir() : javaDirPath);
                  this.gameDir = new File(this.settings.get("minecraft.gamedir"));

                  try {
                     FileUtil.createFolder(this.gameDir);
                  } catch (IOException var8) {
                     throw new MinecraftException("Cannot create working directory!", "folder-not-found", var8);
                  }

                  this.globalAssetsDir = new File(this.gameDir, "assets");

                  try {
                     FileUtil.createFolder(this.globalAssetsDir);
                  } catch (IOException var7) {
                     throw new RuntimeException("Cannot create assets directory!", var7);
                  }

                  this.assetsIndexesDir = new File(this.globalAssetsDir, "indexes");

                  try {
                     FileUtil.createFolder(this.assetsIndexesDir);
                  } catch (IOException var6) {
                     throw new RuntimeException("Cannot create assets indexes directory!", var6);
                  }

                  this.assetsObjectsDir = new File(this.globalAssetsDir, "objects");

                  try {
                     FileUtil.createFolder(this.assetsObjectsDir);
                  } catch (IOException var5) {
                     throw new RuntimeException("Cannot create assets objects directory!", var5);
                  }

                  this.nativeDir = new File(this.gameDir, "versions/" + this.version.getID() + "/" + "natives");

                  try {
                     FileUtil.createFolder(this.nativeDir);
                  } catch (IOException var4) {
                     throw new RuntimeException("Cannot create native files directory!", var4);
                  }

                  this.javaArgs = this.settings.get("minecraft.javaargs");
                  if (this.javaArgs != null && this.javaArgs.isEmpty()) {
                     this.javaArgs = null;
                  }

                  this.programArgs = this.settings.get("minecraft.args");
                  if (this.programArgs != null && this.programArgs.isEmpty()) {
                     this.programArgs = null;
                  }

                  this.windowSize = this.settings.getWindowSize();
                  if (this.windowSize[0] < 1) {
                     throw new IllegalArgumentException("Invalid window width!");
                  } else if (this.windowSize[1] < 1) {
                     throw new IllegalArgumentException("Invalid window height!");
                  } else {
                     Iterator var3 = this.assistants.iterator();

                     while(var3.hasNext()) {
                        MinecraftLauncherAssistant assistant = (MinecraftLauncherAssistant)var3.next();
                        assistant.collectInfo();
                     }

                     this.log("Checking conditions...");
                     if (this.version.getMinimumCustomLauncherVersion() > 6) {
                        throw new MinecraftException("Alternative launcher is incompatible with launching version!", "incompatible", new Object[0]);
                     } else {
                        if (this.version.getMinimumCustomLauncherVersion() == 0 && this.version.getMinimumLauncherVersion() > 14) {
                           Alert.showLocAsyncWarning("launcher.warning.title", "launcher.warning.incompatible.launcher");
                        }

                        if (!this.version.appliesToCurrentEnvironment()) {
                           Alert.showLocAsyncWarning("launcher.warning.title", "launcher.warning.incompatible.environment");
                        }

                        this.downloadResources();
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

   private void downloadResources() throws MinecraftException, AbortedDownloadException {
      this.checkStep(MinecraftLauncher.MinecraftLauncherStep.COLLECTING, MinecraftLauncher.MinecraftLauncherStep.DOWNLOADING);
      Iterator var2 = this.extListeners.iterator();

      while(var2.hasNext()) {
         MinecraftExtendedListener listener = (MinecraftExtendedListener)var2.next();
         listener.onMinecraftComparingAssets();
      }

      final List assets = this.compareAssets();
      Iterator var3 = this.extListeners.iterator();

      while(var3.hasNext()) {
         MinecraftExtendedListener listener = (MinecraftExtendedListener)var3.next();
         listener.onMinecraftDownloading();
      }

      DownloadableContainer execContainer;
      try {
         execContainer = this.vm.downloadVersion(this.versionSync, this.forceUpdate);
      } catch (IOException var8) {
         throw new MinecraftException("Cannot download version!", "download-jar", new Object[0]);
      }

      execContainer.setConsole(this.console);
      DownloadableContainer assetsContainer = null;

      try {
         assetsContainer = this.am.downloadResources(this.version, assets, this.forceUpdate);
      } catch (IOException var7) {
         this.log("Cannot get assets container!", var7);
      }

      if (assetsContainer != null) {
         assetsContainer.addHandler(new DownloadableContainerHandler() {
            public void onStart(DownloadableContainer c) {
            }

            public void onAbort(DownloadableContainer c) {
            }

            public void onError(DownloadableContainer c, Downloadable d, Throwable e) {
            }

            public void onComplete(DownloadableContainer c, Downloadable d) throws RetryDownloadException {
               String filename = d.getFilename();
               AssetIndex.AssetObject object = null;
               Iterator var6 = assets.iterator();

               while(var6.hasNext()) {
                  AssetIndex.AssetObject asset = (AssetIndex.AssetObject)var6.next();
                  if (filename.equals(asset.getHash())) {
                     object = asset;
                  }
               }

               if (object == null) {
                  MinecraftLauncher.this.log("Couldn't find object:", filename);
               } else {
                  File destination = d.getDestination();
                  String hash = FileUtil.getDigest(destination, "SHA-1", 40);
                  if (hash == null) {
                     throw new RetryDownloadException("File hash is NULL!");
                  } else {
                     String assetHash = object.getHash();
                     if (assetHash == null) {
                        MinecraftLauncher.this.log("Hash of", object.getHash(), "is NULL");
                     } else if (!hash.equals(assetHash)) {
                        throw new RetryDownloadException("Hashes are not equal: " + hash + ";" + assetHash);
                     }
                  }
               }
            }

            public void onFullComplete(DownloadableContainer c) {
               MinecraftLauncher.this.log("Assets have been downloaded");
            }
         });
         assetsContainer.setConsole(this.console);
      }

      if (assetsContainer != null) {
         this.downloader.add(assetsContainer);
      }

      this.downloader.add(execContainer);
      Iterator var5 = this.assistants.iterator();

      while(var5.hasNext()) {
         MinecraftLauncherAssistant assistant = (MinecraftLauncherAssistant)var5.next();
         assistant.collectResources(this.downloader);
      }

      this.downloader.startDownloadAndWait();
      if (execContainer.isAborted()) {
         throw new AbortedDownloadException();
      } else if (!execContainer.getErrors().isEmpty()) {
         throw new MinecraftException(execContainer.getErrors().size() + " errors occurred while trying to download executable files.", "download", new Object[0]);
      } else {
         try {
            this.vm.getLocalList().saveVersion(this.version);
         } catch (IOException var6) {
            this.log("Cannot save version!", var6);
         }

         this.constructProcess();
      }
   }

   private void constructProcess() throws MinecraftException {
      this.checkStep(MinecraftLauncher.MinecraftLauncherStep.DOWNLOADING, MinecraftLauncher.MinecraftLauncherStep.CONSTRUCTING);
      Iterator var2 = this.extListeners.iterator();

      MinecraftExtendedListener listener;
      while(var2.hasNext()) {
         listener = (MinecraftExtendedListener)var2.next();
         listener.onMinecraftReconstructingAssets();
      }

      try {
         this.localAssetsDir = this.reconstructAssets();
      } catch (IOException var6) {
         throw new MinecraftException("Cannot recounstruct assets!", "reconstruct-assets", var6);
      }

      var2 = this.extListeners.iterator();

      while(var2.hasNext()) {
         listener = (MinecraftExtendedListener)var2.next();
         listener.onMinecraftUnpackingNatives();
      }

      try {
         this.unpackNatives(this.forceUpdate);
      } catch (IOException var5) {
         throw new MinecraftException("Cannot unpack natives!", "unpack-natives", var5);
      }

      var2 = this.extListeners.iterator();

      while(var2.hasNext()) {
         listener = (MinecraftExtendedListener)var2.next();
         listener.onMinecraftDeletingEntries();
      }

      try {
         this.deleteEntries();
      } catch (IOException var4) {
         throw new MinecraftException("Cannot delete entries!", "delete-entries", var4);
      }

      this.log("Constructing process...");
      var2 = this.extListeners.iterator();

      while(var2.hasNext()) {
         listener = (MinecraftExtendedListener)var2.next();
         listener.onMinecraftConstructing();
      }

      this.launcher = new JavaProcessLauncher(this.javaDir.getAbsolutePath(), new String[0]);
      this.launcher.directory(this.gameDir);
      if (this.os.equals(OperatingSystem.OSX)) {
         File icon = null;

         try {
            icon = this.getAssetObject("icons/minecraft.icns");
         } catch (IOException var3) {
            this.log("Cannot get icon file from assets.", var3);
         }

         if (icon != null) {
            this.launcher.addCommand("-Xdock:icon=\"" + icon.getAbsolutePath() + "\"", "-Xdock:name=Minecraft");
         }
      }

      this.launcher.addCommand(this.os.is32Bit() ? "-Xmx512M" : "-Xmx1G");
      this.launcher.addCommand("-Djava.library.path=" + this.nativeDir.getAbsolutePath());
      this.launcher.addCommand("-cp", this.constructClassPath(this.version));
      if (this.javaArgs != null) {
         this.launcher.addSplitCommands(this.javaArgs);
      }

      this.launcher.addCommands(this.getJVMArguments());
      var2 = this.assistants.iterator();

      MinecraftLauncherAssistant assistant;
      while(var2.hasNext()) {
         assistant = (MinecraftLauncherAssistant)var2.next();
         assistant.constructJavaArguments();
      }

      this.launcher.addCommand(this.version.getMainClass());
      this.log("Half command (characters are not escaped, without Minecraft arguments):");
      this.log(this.launcher.getCommandsAsString());
      this.launcher.addCommands(this.getMinecraftArguments());
      this.launcher.addCommand("--width", this.windowSize[0]);
      this.launcher.addCommand("--height", this.windowSize[1]);
      if (this.programArgs != null) {
         this.launcher.addSplitCommands(this.programArgs);
      }

      var2 = this.assistants.iterator();

      while(var2.hasNext()) {
         assistant = (MinecraftLauncherAssistant)var2.next();
         assistant.constructProgramArguments();
      }

      this.launchMinecraft();
   }

   private File reconstructAssets() throws IOException {
      String assetVersion = this.version.getAssets() == null ? "legacy" : this.version.getAssets();
      File indexFile = new File(this.assetsIndexesDir, assetVersion + ".json");
      File virtualRoot = new File(new File(this.globalAssetsDir, "virtual"), assetVersion);
      if (!indexFile.isFile()) {
         this.log("No assets index file " + virtualRoot + "; can't reconstruct assets");
         return virtualRoot;
      } else {
         AssetIndex index = (AssetIndex)this.gson.fromJson(FileUtil.readFile(indexFile), AssetIndex.class);
         if (index.isVirtual()) {
            this.log("Reconstructing virtual assets folder at " + virtualRoot);
            Iterator var6 = index.getFileMap().entrySet().iterator();

            while(true) {
               while(var6.hasNext()) {
                  Entry entry = (Entry)var6.next();
                  File target = new File(virtualRoot, (String)entry.getKey());
                  File original = new File(new File(this.assetsObjectsDir, ((AssetIndex.AssetObject)entry.getValue()).getHash().substring(0, 2)), ((AssetIndex.AssetObject)entry.getValue()).getHash());
                  if (!original.isFile()) {
                     this.log("Skipped reconstructing:", original);
                  } else if (this.forceUpdate || !target.isFile()) {
                     FileUtils.copyFile(original, target, false);
                  }
               }

               FileUtil.writeFile(new File(virtualRoot, ".lastused"), this.dateAdapter.serializeToString(new Date()));
               break;
            }
         }

         return virtualRoot;
      }
   }

   private File getAssetObject(String name) throws IOException {
      String assetVersion = this.version.getAssets();
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
      if (force) {
         this.nativeDir.delete();
      }

      Iterator var6 = libraries.iterator();

      label68:
      while(true) {
         Library library;
         Map nativesPerOs;
         do {
            do {
               if (!var6.hasNext()) {
                  return;
               }

               library = (Library)var6.next();
               nativesPerOs = library.getNatives();
            } while(nativesPerOs == null);
         } while(nativesPerOs.get(this.os) == null);

         File file = new File(MinecraftUtil.getWorkingDirectory(), "libraries/" + library.getArtifactPath((String)nativesPerOs.get(this.os)));
         ZipFile zip = new ZipFile(file);
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
                        continue label68;
                     }

                     entry = (ZipEntry)entries.nextElement();
                  } while(extractRules != null && !extractRules.shouldExtract(entry.getName()));

                  targetFile = new File(this.nativeDir, entry.getName());
               } while(!force && targetFile.exists());

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
      List entries = this.version.getRemovableEntries();
      if (entries != null && entries.size() != 0) {
         this.log("Removing entries...");
         File file = this.version.getFile(this.gameDir);
         FileUtil.removeFromZip(file, entries);
      }
   }

   private String constructClassPath(CompleteVersion version) throws MinecraftException {
      this.log("Constructing classpath...");
      StringBuilder result = new StringBuilder();
      Collection classPath = version.getClassPath(this.os, this.gameDir);
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
         Map map = new HashMap();
         StrSubstitutor substitutor = new StrSubstitutor(map);
         String assets = this.version.getAssets();
         String[] split = this.version.getMinecraftArguments().split(" ");
         map.put("auth_username", this.accountName);
         if (this.account.hasLicense()) {
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
      String jvmargs = this.version.getJVMArguments();
      return jvmargs != null ? jvmargs.split(" ") : new String[0];
   }

   private List compareAssets() {
      this.migrateOldAssets();
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
         IOFileFilter migratableFilter = FileFilterUtils.notFileFilter(FileFilterUtils.or(FileFilterUtils.nameFileFilter("indexes"), FileFilterUtils.nameFileFilter("objects"), FileFilterUtils.nameFileFilter("virtual")));

         File file;
         for(Iterator var3 = (new TreeSet(FileUtils.listFiles(this.globalAssetsDir, TrueFileFilter.TRUE, migratableFilter))).iterator(); var3.hasNext(); FileUtils.deleteQuietly(file)) {
            file = (File)var3.next();
            String hash = FileUtil.getDigest(file, "SHA-1", 40);
            File destinationFile = new File(this.assetsObjectsDir, hash.substring(0, 2) + "/" + hash);
            if (!destinationFile.exists()) {
               this.log("Migrated old asset", file, "into", destinationFile);

               try {
                  FileUtils.copyFile(file, destinationFile);
               } catch (IOException var7) {
                  this.log("Couldn't migrate old asset", var7);
               }
            }
         }

         File[] assets = this.globalAssetsDir.listFiles();
         if (assets != null) {
            File[] var6 = assets;
            int var11 = assets.length;

            for(int var10 = 0; var10 < var11; ++var10) {
               File file = var6[var10];
               if (!file.getName().equals("indexes") && !file.getName().equals("objects") && !file.getName().equals("virtual")) {
                  this.log("Cleaning up old assets directory", file, "after migration");
                  FileUtils.deleteQuietly(file);
               }
            }
         }

      }
   }

   private void launchMinecraft() throws MinecraftException {
      this.checkStep(MinecraftLauncher.MinecraftLauncherStep.CONSTRUCTING, MinecraftLauncher.MinecraftLauncherStep.LAUNCHING);
      this.log("Launching Minecraft...");
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         MinecraftListener listener = (MinecraftListener)var2.next();
         listener.onMinecraftLaunch();
      }

      U.gc();
      this.log("Starting Minecraft " + this.versionName + "...");
      this.log("Launching in:", this.gameDir.getAbsolutePath());

      try {
         JavaProcess process = this.launcher.start();
         process.safeSetExitRunnable(this);
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

      if (this.assistLaunch) {
         this.waitForClose();
      } else {
         U.sleepFor(30000L);
         if (this.minecraftWorking) {
            TLauncher.kill();
         }
      }

   }

   private void log(Object... o) {
      U.log("[Launcher]", o);
      this.logger.log("[L]", o);
   }

   private void checkThread() {
      if (!Thread.currentThread().equals(this.parentThread)) {
         throw new IllegalStateException("Illegal thread!");
      }
   }

   private void checkStep(MinecraftLauncher.MinecraftLauncherStep prevStep, MinecraftLauncher.MinecraftLauncherStep currentStep) {
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

   public void onJavaProcessLog(JavaProcess jp, String line) {
      U.plog(">", line);
      this.logger.log(line);
   }

   public void onJavaProcessEnded(JavaProcess jp) {
      this.notifyClose();
      int exit = jp.getExitCode();
      Iterator var4 = this.listeners.iterator();

      while(var4.hasNext()) {
         MinecraftListener listener = (MinecraftListener)var4.next();
         listener.onMinecraftClose();
      }

      this.log("Minecraft closed with exit code: " + exit);
      this.exitCode = exit;
      Crash crash = this.descriptor.scan();
      if (crash == null) {
         if (!this.assistLaunch) {
            TLauncher.kill();
         }

         if (this.console != null) {
            this.console.killIn(5000L);
         }
      } else {
         Iterator var5 = this.listeners.iterator();

         while(var5.hasNext()) {
            MinecraftListener listener = (MinecraftListener)var5.next();
            listener.onMinecraftCrash(crash);
         }

         if (this.console != null) {
            this.console.show();
         }
      }

      U.gc();
   }

   public void onJavaProcessError(JavaProcess jp, Throwable e) {
      this.notifyClose();
      Iterator var4 = this.listeners.iterator();

      while(var4.hasNext()) {
         MinecraftListener listener = (MinecraftListener)var4.next();
         listener.onMinecraftError(e);
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
      this.notifyAll();
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         MinecraftListener listener = (MinecraftListener)var2.next();
         listener.onMinecraftClose();
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
}
