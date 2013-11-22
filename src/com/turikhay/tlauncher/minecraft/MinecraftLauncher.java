package com.turikhay.tlauncher.minecraft;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.util.Console;
import com.turikhay.tlauncher.util.FileUtil;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ExtractRules;
import net.minecraft.launcher.versions.Library;
import org.apache.commons.lang3.text.StrSubstitutor;

public class MinecraftLauncher extends Thread implements JavaProcessListener {
   public static final int VERSION = 10;
   final String prefix;
   final OperatingSystem os;
   GlobalSettings s;
   Downloader d;
   Console con;
   final MinecraftLauncherListener listener;
   final VersionManager vm;
   final boolean exit;
   boolean init;
   boolean working;
   boolean launching;
   boolean installed;
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
   DownloadableContainer jar;
   DownloadableContainer resources;
   JavaProcessLauncher processLauncher;
   File nativeDir;
   File gameDir;
   File assetsDir;

   private MinecraftLauncher(MinecraftLauncherListener listener, VersionManager vm, String version_name, String username, String token, String gamedir, String javadir, String jargs, String margs, int[] sizes, boolean force, boolean check, boolean exit, boolean console) {
      this.prefix = "[MinecraftLauncher]";
      this.os = OperatingSystem.getCurrentPlatform();
      this.jar = new DownloadableContainer();
      this.resources = new DownloadableContainer();
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
      this.log("Minecraft Launcher v10 is started!");
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

   private void init() {
      if (!this.init) {
         this.init = true;
         if (!this.exit) {
            this.con = new Console(this.s, "Minecraft Logger", this.console);
         }

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
         } catch (Exception var4) {
            throw new MinecraftLauncherException("Cannot find folder: " + this.gamedir, "folder-not-found", this.gamedir);
         }

         this.syncInfo = this.vm.getVersionSyncInfo(this.version_name);
         if (this.syncInfo == null) {
            throw new MinecraftLauncherException("SyncInfo is NULL!", "version-not-found", this.version_name + "\n" + this.gamedir);
         } else {
            try {
               this.version = this.vm.getLatestCompleteVersion(this.syncInfo);
            } catch (Exception var3) {
               throw new MinecraftLauncherException("Cannot get version info!", "version-info", var3);
            }

            if (!this.check) {
               this.log("Checking files for version " + this.version_name + " skipped.");
               this.prepare_();
            } else {
               this.log("Checking files for version " + this.version_name + "...");
               this.working = true;
               this.onCheck();
               this.installed = this.syncInfo.isInstalled() && this.vm.getLocalVersionList().hasAllFiles(this.version, this.os);
               if (!this.version.appliesToCurrentEnvironment()) {
                  this.showWarning("Version " + this.version_name + " is incompatible with your environment.", "incompatible");
               }

               if (this.version.getMinimumLauncherVersion() > 10) {
                  this.showWarning("Current launcher version is incompatible with selected version " + this.version_name + " (version " + this.version.getMinimumLauncherVersion() + " required).", "incompatible.launcher");
               }

               if (!this.forceupdate && this.installed) {
                  this.prepare_();
               } else if (this.d == null) {
                  throw new MinecraftLauncherException("Downloader is NULL. Cannot download version!");
               } else {
                  this.log("Downloading version " + this.version_name + "...");

                  try {
                     this.vm.downloadVersion(this.syncInfo, this.jar, this.forceupdate);
                  } catch (IOException var2) {
                     throw new MinecraftLauncherException("Cannot get downloadable jar!", "download-jar", var2);
                  }

                  this.jar.addHandler(new DownloadableHandler() {
                     public void onStart() {
                     }

                     public void onCompleteError() {
                        MinecraftLauncher.this.onError(new MinecraftLauncherException("Errors occurred, cancelling.", "download"));
                     }

                     public void onComplete() {
                        MinecraftLauncher.this.log("Version " + MinecraftLauncher.this.version_name + " downloaded!");
                        MinecraftLauncher.this.vm.getLocalVersionList().saveVersion(MinecraftLauncher.this.version);
                        MinecraftLauncher.this.prepare();
                     }
                  });
                  this.jar.setConsole(this.con);
                  this.d.add(this.jar);
                  this.d.launch();
               }
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
         } catch (IOException var5) {
            throw new MinecraftLauncherException("Cannot unpack natives!", "unpack-natives", var5);
         }

         this.processLauncher = new JavaProcessLauncher(this.javadir, new String[0]);
         this.processLauncher.directory(this.gameDir);
         this.assetsDir = new File(this.gameDir, "assets");
         List resourcesList = this.check ? this.compareResources() : null;
         boolean resourcesAreReady = resourcesList == null || resourcesList.size() == 0;
         if (this.os.equals(OperatingSystem.OSX)) {
            this.processLauncher.addCommand("-Xdock:icon=\"" + (new File(this.assetsDir, "icons/minecraft.icns")).getAbsolutePath() + "\"", "-Xdock:name=Minecraft");
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

         if (resourcesAreReady) {
            this.launch_();
         } else if (this.d == null) {
            throw new MinecraftLauncherException("Downloader is NULL. Cannot download resources!");
         } else {
            try {
               this.resources = this.vm.downloadResources(resourcesList, this.forceupdate);
            } catch (IOException var4) {
               throw new MinecraftLauncherException("Cannot download resources!", "download-resources", var4);
            }

            if (this.resources.get().isEmpty()) {
               this.launch_();
            } else {
               this.log("Downloading resources...");
               this.resources.addHandler(new DownloadableHandler() {
                  public void onStart() {
                  }

                  public void onCompleteError() {
                     MinecraftLauncher.this.log("Error occurred while downloading the resources. Minecraft will be be launched, though");
                     MinecraftLauncher.this.launch();
                  }

                  public void onComplete() {
                     MinecraftLauncher.this.log("Resources have been downloaded!");
                     MinecraftLauncher.this.launch();
                  }
               });
               this.resources.setConsole(this.con);
               this.d.add(this.resources);
               this.d.launch();
            }
         }
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
         this.log("Running: " + this.processLauncher.getCommandsAsString());
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

   private List compareResources() {
      this.log("Comparing resources...");
      long start = System.nanoTime();
      if (this.forcecompare) {
         this.log("Resources will be compared from the server.");
      }

      List result = this.vm.checkResources(!this.forcecompare, true);
      long end = System.nanoTime();
      long delta = end - start;
      this.log("Delta time to compare resources: " + delta / 1000000L + " ms.");
      return result;
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

   private String constructClassPath(CompleteVersion version) throws MinecraftLauncherException {
      this.log("Constructing Classpath...");
      StringBuilder result = new StringBuilder();
      Collection classPath = version.getClassPath(this.os, MinecraftUtil.getWorkingDirectory());
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

   private void log(Object w) {
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
