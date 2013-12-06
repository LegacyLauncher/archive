package com.turikhay.tlauncher.minecraft;

import com.google.gson.Gson;
import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.ui.Console;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
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
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;
import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.updater.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ExtractRules;
import net.minecraft.launcher.versions.Library;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.text.StrSubstitutor;

public class MinecraftLauncher extends Thread implements JavaProcessListener {
   public static final int VERSION = 12;
   public static final int TLAUNCHER_VERSION = 6;
   final String prefix;
   final OperatingSystem os;
   final Gson gson;
   final DateTypeAdapter dateAdapter;
   GlobalSettings s;
   Downloader d;
   Console con;
   final MinecraftLauncherListener listener;
   final VersionManager vm;
   final boolean exit;
   boolean init;
   boolean working;
   boolean launching;
   boolean forcecompare;
   final boolean forceupdate;
   final boolean check;
   final boolean console;
   VersionSyncInfo syncInfo;
   CompleteVersion version;
   final String username;
   final String version_name;
   final String jargs;
   final String margs;
   final String gamedir;
   final String javadir;
   final int width;
   final int height;
   DownloadableContainer ver;
   DownloadableContainer res;
   List assetsList;
   private boolean downloadVersion;
   private boolean downloadAssets;
   private boolean downloadFlag;
   JavaProcessLauncher processLauncher;
   File nativeDir;
   File gameDir;
   File assetsDir;

   private MinecraftLauncher(MinecraftLauncherListener listener, VersionManager vm, String version_name, String username, String token, String gamedir, String javadir, String jargs, String margs, int[] sizes, boolean force, boolean check, boolean exit, boolean console) {
      this.prefix = "[MinecraftLauncher]";
      this.os = OperatingSystem.getCurrentPlatform();
      this.gson = new Gson();
      this.dateAdapter = new DateTypeAdapter();
      this.ver = new DownloadableContainer();
      this.res = new DownloadableContainer();
      Thread.setDefaultUncaughtExceptionHandler(new MinecraftLauncherExceptionHandler(this));
      this.listener = listener;
      this.vm = vm;
      this.version_name = version_name;
      this.forceupdate = force;
      this.username = username;
      this.gamedir = gamedir;
      this.javadir = javadir == null ? this.os.getJavaDir() : javadir;
      this.jargs = jargs == null ? "" : jargs;
      this.margs = margs == null ? "" : margs;
      this.console = console;
      this.check = check;
      this.exit = exit;
      this.width = sizes[0];
      this.height = sizes[1];
   }

   public MinecraftLauncher(MinecraftLauncherListener listener, Downloader d, GlobalSettings s, VersionManager vm, boolean force, boolean check) {
      this(listener, vm, s.get("login.version"), s.get("login.username"), s.get("login.token"), s.get("minecraft.gamedir"), s.get("minecraft.javadir"), s.get("minecraft.javaargs"), s.get("minecraft.args"), s.getWindowSize(), force, check, s.getActionOnLaunch() == GlobalSettings.ActionOnLaunch.EXIT, s.getBoolean("gui.console"));
      this.s = s;
      this.d = d;
      this.init();
   }

   public MinecraftLauncher(TLauncher t, MinecraftLauncherListener listener, boolean force, boolean check) {
      this(listener, t.getDownloader(), t.getSettings(), t.getVersionManager(), force, check);
      this.init();
   }

   public void init() {
      if (!this.init) {
         this.init = true;
         if (!this.exit && this.s != null) {
            this.con = new Console(this.s, "Minecraft Logger", this.console);
         }

         this.log("Minecraft Launcher [12;6] is started!");
         this.log("Running under TLauncher " + TLauncher.getVersion() + " " + TLauncher.getBrand());
      }
   }

   public void run() {
      try {
         this.check();
      } catch (MinecraftLauncherException var2) {
         this.onError(var2);
      } catch (Exception var3) {
         this.onError((Throwable)var3);
      }

   }

   private void check() throws MinecraftLauncherException {
      if (this.working) {
         throw new IllegalStateException("MinecraftLauncher is already working!");
      } else if (this.version_name != null && this.version_name.length() != 0) {
         if (!FileUtil.folderExists(this.gamedir)) {
            this.forcecompare = true;
         }

         try {
            FileUtil.createFolder(this.gamedir);
         } catch (Exception var3) {
            throw new MinecraftLauncherException("Cannot find folder: " + this.gamedir, "folder-not-found", this.gamedir);
         }

         this.syncInfo = this.vm.getVersionSyncInfo(this.version_name);
         if (this.syncInfo == null) {
            throw new MinecraftLauncherException("SyncInfo is NULL!", "version-not-found", this.version_name + "\n" + this.gamedir);
         } else {
            try {
               this.version = this.vm.getLatestCompleteVersion(this.syncInfo);
            } catch (Exception var2) {
               throw new MinecraftLauncherException("Cannot get version info!", "version-info", var2);
            }

            if (this.check) {
               this.log("Checking files for version " + this.version_name + "...");
               this.working = true;
               this.onCheck();
               if (this.version.getTLauncherVersion() != 0) {
                  if (this.version.getTLauncherVersion() > 6) {
                     throw new MinecraftLauncherException("TLauncher is incompatible with this extra version (needed " + this.version.getTLauncherVersion() + ").", "incompatible");
                  }
               } else {
                  if (!this.version.appliesToCurrentEnvironment()) {
                     this.showWarning("Version " + this.version_name + " is incompatible with your environment.", "incompatible");
                  }

                  if (this.version.getMinimumLauncherVersion() > 12) {
                     this.showWarning("Current launcher version is incompatible with selected version " + this.version_name + " (version " + this.version.getMinimumLauncherVersion() + " required).", "incompatible.launcher");
                  }
               }

               this.assetsList = this.check ? this.compareAssets() : null;
               this.downloadAssets = this.assetsList != null && !this.assetsList.isEmpty();
               if (this.forceupdate) {
                  this.downloadVersion = true;
               } else {
                  this.downloadVersion = !this.syncInfo.isInstalled() || !this.vm.getLocalVersionList().hasAllFiles(this.version, this.os);
               }

               if (!this.forceupdate && !this.downloadVersion && !this.downloadAssets) {
                  this.prepare_();
               } else {
                  this.downloadResources();
               }
            } else {
               this.log("Checking files for version " + this.version_name + " skipped.");
               this.prepare_();
            }
         }
      } else {
         throw new MinecraftLauncherException("Version name is invalid: \"" + this.version_name + "\"", "version-invalid", this.version_name);
      }
   }

   private void prepare() {
      try {
         this.prepare_();
      } catch (Exception var2) {
         this.onError((Throwable)var2);
      }

   }

   private void downloadResources() throws MinecraftLauncherException {
      if (this.d == null) {
         throw new MinecraftLauncherException("Downloader is NULL. Cannot download version!");
      } else {
         if (this.downloadVersion) {
            try {
               this.vm.downloadVersion(this.ver, this.syncInfo, this.forceupdate);
            } catch (IOException var3) {
               throw new MinecraftLauncherException("Cannot get downloadable jar!", "download-jar", var3);
            }
         }

         if (this.downloadAssets) {
            try {
               this.vm.downloadResources(this.res, this.version, this.assetsList, this.forceupdate);
            } catch (IOException var2) {
               throw new MinecraftLauncherException("Cannot download resources!", "download-resources", var2);
            }
         }

         this.ver.addHandler(new DownloadableHandler() {
            public void onStart() {
            }

            public void onCompleteError() {
               MinecraftLauncher.this.onError(new MinecraftLauncherException("Errors occurred, cancelling.", "download"));
            }

            public void onAbort() {
               MinecraftLauncher.this.onStop();
            }

            public void onComplete() {
               MinecraftLauncher.this.log("Version " + MinecraftLauncher.this.version_name + " downloaded!");
               MinecraftLauncher.this.vm.getLocalVersionList().saveVersion(MinecraftLauncher.this.version);
               if (MinecraftLauncher.this.downloadFlag) {
                  MinecraftLauncher.this.prepare();
               } else {
                  MinecraftLauncher.this.downloadFlag = true;
               }

            }
         });
         this.ver.setConsole(this.con);
         this.res.addHandler(new DownloadableHandler() {
            public void onStart() {
            }

            public void onCompleteError() {
               MinecraftLauncher.this.log("Error occurred while downloading the assets. Minecraft will be be launched, though");
               this.onContinue();
            }

            public void onComplete() {
               MinecraftLauncher.this.log("Assets have been downloaded!");
               this.onContinue();
            }

            public void onAbort() {
               MinecraftLauncher.this.onStop();
            }

            private void onContinue() {
               if (MinecraftLauncher.this.downloadFlag) {
                  MinecraftLauncher.this.prepare();
               } else {
                  MinecraftLauncher.this.downloadFlag = true;
               }

            }
         });
         this.res.setConsole(this.con);
         if (!this.downloadVersion || !this.downloadAssets) {
            this.downloadFlag = true;
         }

         if (this.downloadVersion) {
            this.log("Downloading version " + this.version_name + "...");
         }

         if (this.downloadAssets) {
            this.log("Downloading assets...");
         }

         this.d.add(this.ver);
         this.d.add(this.res);
         this.d.startLaunch();
      }
   }

   private void prepare_() throws MinecraftLauncherException {
      if (this.launching) {
         throw new IllegalStateException("The game is already launching!");
      } else {
         this.launching = true;
         this.onPrepare();
         this.gameDir = new File(this.gamedir);
         this.nativeDir = new File(this.gameDir, "versions/" + this.version.getId() + "/" + "natives");
         if (!this.nativeDir.isDirectory()) {
            this.nativeDir.mkdirs();
         }

         try {
            this.unpackNatives(this.forceupdate);
         } catch (IOException var6) {
            throw new MinecraftLauncherException("Cannot unpack natives!", "unpack-natives", var6);
         }

         try {
            this.deleteEntries();
         } catch (IOException var5) {
            throw new MinecraftLauncherException("Cannot delete entries!", "delete-entries", var5);
         }

         this.processLauncher = new JavaProcessLauncher(this.javadir, new String[0]);
         this.processLauncher.directory(this.gameDir);

         try {
            this.assetsDir = this.reconstructAssets();
         } catch (IOException var4) {
            throw new MinecraftLauncherException("Cannot reconstruct assets!", "reconstruct-assets", var4);
         }

         if (this.os.equals(OperatingSystem.OSX)) {
            File icon = null;

            try {
               icon = this.getAssetObject("icons/minecraft.icns");
            } catch (IOException var3) {
               this.log("Cannot get icon file from assets.", var3);
            }

            if (icon != null) {
               this.processLauncher.addCommand("-Xdock:icon=\"" + icon.getAbsolutePath() + "\"", "-Xdock:name=Minecraft");
            }
         }

         if (this.os.equals(OperatingSystem.WINDOWS)) {
            this.processLauncher.addCommand("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
         }

         this.processLauncher.addCommand(this.os.is32Bit() ? "-Xmx512M" : "-Xmx1G");
         this.processLauncher.addCommand("-Djava.library.path=" + this.nativeDir.getAbsolutePath());
         this.processLauncher.addCommand("-cp", this.constructClassPath(this.version));
         this.processLauncher.addCommands(this.getJVMArguments());
         if (this.jargs.length() > 0) {
            this.processLauncher.addCommand(this.jargs);
         }

         this.processLauncher.addCommand(this.version.getMainClass());
         this.processLauncher.addCommands(this.getMinecraftArguments());
         this.processLauncher.addCommand("--width", this.width);
         this.processLauncher.addCommand("--height", this.height);
         if (this.margs.length() > 0) {
            this.processLauncher.addSplitCommands(this.margs);
         }

         this.launch();
      }
   }

   public void launch() {
      try {
         this.launch_();
      } catch (Exception var2) {
         this.onError((Throwable)var2);
      }

      U.gc();
   }

   private void launch_() throws MinecraftLauncherException {
      U.gc();
      this.log("Starting Minecraft " + this.version_name + "...");

      try {
         this.log("Running (characters are not escaped):");
         this.log(this.processLauncher.getCommandsAsString());
         if (!this.exit) {
            this.onLaunch();
         }

         JavaProcess process = this.processLauncher.start();
         if (this.exit) {
            TLauncher.kill();
         } else {
            process.safeSetExitRunnable(this);
         }

      } catch (Exception var2) {
         throw new MinecraftLauncherException("Cannot start the game!", "start", var2);
      }
   }

   private void removeNatives() {
      this.nativeDir.delete();
   }

   private List compareAssets() {
      this.migrateOldAssets();
      this.log("Comparing assets...");
      long start = System.nanoTime();
      if (this.forcecompare) {
         this.log("Assets will be compared from the server.");
      }

      List result = this.vm.checkResources(this.version);
      long end = System.nanoTime();
      long delta = end - start;
      this.log("Delta time to compare assets: " + delta / 1000000L + " ms.");
      return result;
   }

   private void deleteEntries() throws IOException {
      List entries = this.version.getUnnecessaryEntries();
      if (entries != null && entries.size() != 0) {
         this.log("Removing entries...");
         File file = this.version.getJARFile(this.gameDir);
         FileUtil.removeFromZip(file, entries);
      }
   }

   private void unpackNatives(boolean force) throws IOException {
      this.log("Unpacking natives...");
      Collection libraries = this.version.getRelevantLibraries();
      if (force) {
         this.removeNatives();
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

   private File getAssetObject(String name) throws IOException {
      File assetsDir = new File(this.gamedir, "assets");
      File indexDir = new File(assetsDir, "indexes");
      File objectsDir = new File(assetsDir, "objects");
      String assetVersion = this.version.getAssets() == null ? "legacy" : this.version.getAssets();
      File indexFile = new File(indexDir, assetVersion + ".json");
      AssetIndex index = (AssetIndex)this.gson.fromJson(FileUtil.readFile(indexFile), AssetIndex.class);
      String hash = ((AssetIndex.AssetObject)index.getFileMap().get(name)).getHash();
      return new File(objectsDir, hash.substring(0, 2) + "/" + hash);
   }

   private File reconstructAssets() throws IOException {
      File assetsDir = new File(this.gameDir, "assets");
      File indexDir = new File(assetsDir, "indexes");
      File objectDir = new File(assetsDir, "objects");
      String assetVersion = this.version.getAssets() == null ? "legacy" : this.version.getAssets();
      File indexFile = new File(indexDir, assetVersion + ".json");
      File virtualRoot = new File(new File(assetsDir, "virtual"), assetVersion);
      if (!indexFile.isFile()) {
         this.log("No assets index file " + virtualRoot + "; can't reconstruct assets");
         return virtualRoot;
      } else {
         AssetIndex index = (AssetIndex)this.gson.fromJson(FileUtil.readFile(indexFile), AssetIndex.class);
         if (index.isVirtual()) {
            this.log("Reconstructing virtual assets folder at " + virtualRoot);
            Iterator var9 = index.getFileMap().entrySet().iterator();

            while(var9.hasNext()) {
               Entry entry = (Entry)var9.next();
               File target = new File(virtualRoot, (String)entry.getKey());
               File original = new File(new File(objectDir, ((AssetIndex.AssetObject)entry.getValue()).getHash().substring(0, 2)), ((AssetIndex.AssetObject)entry.getValue()).getHash());
               if (!target.isFile()) {
                  FileUtils.copyFile(original, target, false);
               }
            }

            FileUtil.writeFile(new File(virtualRoot, ".lastused"), this.dateAdapter.serializeToString(new Date()));
         }

         return virtualRoot;
      }
   }

   private void migrateOldAssets() {
      File sourceDir = new File(this.gamedir, "assets");
      File objectsDir = new File(sourceDir, "objects");
      if (sourceDir.isDirectory()) {
         this.log("Migrating old assets...");
         IOFileFilter migratableFilter = FileFilterUtils.notFileFilter(FileFilterUtils.or(FileFilterUtils.nameFileFilter("indexes"), FileFilterUtils.nameFileFilter("objects"), FileFilterUtils.nameFileFilter("virtual")));

         File file;
         for(Iterator var5 = (new TreeSet(FileUtils.listFiles(sourceDir, TrueFileFilter.TRUE, migratableFilter))).iterator(); var5.hasNext(); FileUtils.deleteQuietly(file)) {
            file = (File)var5.next();
            String hash = FileUtil.getDigest(file, "SHA-1", 40);
            File destinationFile = new File(objectsDir, hash.substring(0, 2) + "/" + hash);
            if (!destinationFile.exists()) {
               this.log("Migrated old asset {} into {}", new Object[]{file, destinationFile});

               try {
                  FileUtils.copyFile(file, destinationFile);
               } catch (IOException var9) {
                  this.log("Couldn't migrate old asset", var9);
               }
            }
         }

         File[] assets = sourceDir.listFiles();
         if (assets != null) {
            File[] var8 = assets;
            int var13 = assets.length;

            for(int var12 = 0; var12 < var13; ++var12) {
               File file = var8[var12];
               if (!file.getName().equals("indexes") && !file.getName().equals("objects") && !file.getName().equals("virtual")) {
                  this.log("Cleaning up old assets directory {} after migration", new Object[]{file});
                  FileUtils.deleteQuietly(file);
               }
            }
         }

      }
   }

   private String constructClassPath(CompleteVersion version) throws MinecraftLauncherException {
      this.log("Constructing Classpath...");
      StringBuilder result = new StringBuilder();
      Collection classPath = version.getClassPath(this.os, this.gameDir);
      String separator = System.getProperty("path.separator");

      File file;
      for(Iterator var6 = classPath.iterator(); var6.hasNext(); result.append(file.getAbsolutePath())) {
         file = (File)var6.next();
         if (!file.isFile()) {
            throw new MinecraftLauncherException("Classpath is not found: " + file, "classpath", file);
         }

         if (result.length() > 0) {
            result.append(separator);
         }
      }

      return result.toString();
   }

   private String[] getMinecraftArguments() throws MinecraftLauncherException {
      this.log("Getting Minecraft arguments...");
      if (this.version.getMinecraftArguments() == null) {
         throw new MinecraftLauncherException("Can't run version, missing minecraftArguments", "noArgs");
      } else {
         Map map = new HashMap();
         StrSubstitutor substitutor = new StrSubstitutor(map);
         String[] split = this.version.getMinecraftArguments().split(" ");
         map.put("auth_username", this.username);
         map.put("auth_session", "null");
         map.put("auth_access_token", "null");
         map.put("user_properties", "{}");
         map.put("auth_player_name", this.username);
         map.put("auth_uuid", (new UUID(0L, 0L)).toString());
         map.put("profile_name", "(Default)");
         map.put("version_name", this.version.getId());
         map.put("game_directory", this.gameDir.getAbsolutePath());
         map.put("game_assets", this.assetsDir.getAbsolutePath());
         map.put("assets_root", (new File(this.gameDir, "assets")).getAbsolutePath());
         map.put("assets_index_name", this.version.getAssets() == null ? "legacy" : this.version.getAssets());

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

   void onCheck() {
      if (this.listener != null) {
         this.listener.onMinecraftCheck();
      }

   }

   void onPrepare() {
      if (this.listener != null) {
         this.listener.onMinecraftPrepare();
      }

   }

   void onLaunch() {
      if (this.listener != null) {
         this.listener.onMinecraftLaunch();
      }

   }

   void onStop() {
      this.log("Stopped :3");
      if (this.listener != null) {
         this.listener.onMinecraftLaunchStop();
      }

   }

   void showWarning(String message, String langpath, Object replace) {
      this.log("[WARNING] " + message);
      if (this.listener != null) {
         this.listener.onMinecraftWarning(langpath, replace);
      }

   }

   void showWarning(String message, String langpath) {
      this.showWarning(message, langpath, (Object)null);
   }

   void onError(MinecraftLauncherException e) {
      this.logerror(e);
      if (this.listener != null) {
         this.listener.onMinecraftError(e);
      }

   }

   void onError(Throwable e) {
      this.logerror(e);
      if (this.listener != null) {
         this.listener.onMinecraftError(e);
      }

   }

   private void log(Object... w) {
      if (this.con != null) {
         this.con.log("[MinecraftLauncher]", w);
      }

      U.log("[MinecraftLauncher]", w);
   }

   private void logerror(Throwable e) {
      e.printStackTrace();
      if (this.con != null) {
         this.con.log("[MinecraftLauncher]", "Error occurred. Logger won't vanish automatically.");
         this.con.log(e);
      }
   }

   public void onJavaProcessEnded(JavaProcess jp) {
      int exit = jp.getExitCode();
      if (this.listener != null) {
         this.listener.onMinecraftClose();
      }

      this.log("Minecraft closed with exit code: " + exit);
      if (!CrashDescriptor.parseExit(exit)) {
         if (!this.handleCrash(exit) && this.con != null) {
            this.con.killIn(5000L);
         }
      } else if (this.con != null) {
         this.con.killIn(5000L);
      }

      U.gc();
   }

   private boolean handleCrash(int exit) {
      if (this.con == null) {
         return false;
      } else {
         CrashDescriptor descriptor = new CrashDescriptor(this);
         Crash crash = descriptor.scan(exit);
         if (crash == null) {
            return false;
         } else {
            if (crash.getFile() != null) {
               this.log("Crash report found.");
            }

            if (!crash.getSignatures().isEmpty()) {
               this.log("Crash is recognized.");
            }

            this.log("Console won't vanish automatically.");
            this.con.show();
            if (this.listener == null) {
               return true;
            } else {
               this.listener.onMinecraftCrash(crash);
               return true;
            }
         }
      }
   }

   public void onJavaProcessError(JavaProcess jp, Throwable e) {
      e.printStackTrace();
      if (this.con != null) {
         this.con.log("Error has occurred:", e);
      }

      if (this.listener != null) {
         this.listener.onMinecraftError(e);
      }

   }

   public void onJavaProcessLog(JavaProcess jp, String line) {
      U.plog(">", line);
      if (this.con != null) {
         this.con.log(line);
      }

   }

   public Console getConsole() {
      return this.con;
   }
}
