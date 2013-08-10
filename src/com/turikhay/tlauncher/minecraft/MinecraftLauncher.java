package com.turikhay.tlauncher.minecraft;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.util.Console;
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
import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.process.JavaProcess;
import net.minecraft.launcher_.process.JavaProcessLauncher;
import net.minecraft.launcher_.process.JavaProcessListener;
import net.minecraft.launcher_.updater.VersionManager;
import net.minecraft.launcher_.updater.VersionSyncInfo;
import net.minecraft.launcher_.versions.CompleteVersion;
import net.minecraft.launcher_.versions.ExtractRules;
import net.minecraft.launcher_.versions.Library;
import org.apache.commons.lang3.text.StrSubstitutor;

public class MinecraftLauncher extends Thread implements JavaProcessListener {
   public final int VERSION = 7;
   private final String prefix = "[MinecraftLauncher]";
   private final OperatingSystem os = OperatingSystem.getCurrentPlatform();
   private TLauncher t;
   private Downloader d;
   private VersionManager vm;
   private Console con;
   private MinecraftLauncherListener listener;
   private boolean working;
   private boolean launching;
   private boolean installed;
   private boolean forceupdate;
   private VersionSyncInfo syncInfo;
   private CompleteVersion version;
   private String username;
   private String version_name;
   private String[] args;
   private DownloadableContainer jar = new DownloadableContainer();
   private DownloadableContainer resources = new DownloadableContainer();
   private JavaProcessLauncher processLauncher;
   private File nativeDir;
   private File gameDir;
   private File assetsDir;

   public MinecraftLauncher(TLauncher t, MinecraftLauncherListener listener, String version_name, boolean forceupdate, String username, String[] args, boolean console) {
      Thread.setDefaultUncaughtExceptionHandler(new MinecraftLauncherExceptionHandler(this));
      this.t = t;
      this.d = this.t.downloader;
      this.vm = this.t.vm;
      this.listener = listener;
      this.version_name = version_name;
      this.syncInfo = this.vm.getVersionSyncInfo(version_name);
      this.forceupdate = forceupdate;
      this.username = username;
      this.args = args;
      if (console) {
         this.con = new Console("Minecraft Logger", true);
      }

      this.log("Minecraft Launcher v7 is started!");
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
      } else {
         this.log("Checking files for version " + this.version_name + "...");
         this.working = true;
         this.onCheck();
         if (this.syncInfo == null) {
            throw new IllegalStateException("Cannot find version \"" + this.version_name + "\"");
         } else {
            try {
               this.version = this.vm.getLatestCompleteVersion(this.syncInfo);
            } catch (IOException var3) {
               var3.printStackTrace();
               return;
            }

            this.installed = true;
            if (!this.version.appliesToCurrentEnvironment()) {
               this.showWarning("Version " + this.version_name + " is incompatible with your environment.", "incompatible");
            }

            if (this.version.getMinimumLauncherVersion() > 7) {
               this.showWarning("Current version of using launcher is incompatible with selected version " + this.version_name + " (version " + this.version.getMinimumLauncherVersion() + " required).", "incompatible.launcher");
            }

            if (!this.forceupdate && this.installed) {
               this.prepare_();
            } else {
               this.log("Downloading libraries and " + this.version_name + ".jar");

               try {
                  this.vm.downloadVersion(this.syncInfo, this.jar);
               } catch (IOException var2) {
                  throw new MinecraftLauncherException("Cannot get downloadable jar!", "download-jar", var2);
               }

               this.jar.setHandler(new DownloadableHandler() {
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
               this.d.add(this.jar);
               this.d.launch();
            }
         }
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
         this.nativeDir = new File(MinecraftUtil.getWorkingDirectory(), "versions/" + this.version.getId() + "/" + this.version.getId() + "-natives");
         if (!this.nativeDir.isDirectory()) {
            this.nativeDir.mkdirs();
         }

         try {
            this.unpackNatives(this.forceupdate);
         } catch (IOException var4) {
            throw new MinecraftLauncherException("Cannot unpack natives!", "unpack-natives", var4);
         }

         this.gameDir = MinecraftUtil.getWorkingDirectory();
         this.processLauncher = new JavaProcessLauncher(this.os.getJavaDir(), new String[0]);
         this.processLauncher.directory(this.gameDir);
         this.assetsDir = new File(MinecraftUtil.getWorkingDirectory(), "assets");
         boolean resourcesAreReady = this.vm.checkResources();
         if (this.os.equals(OperatingSystem.OSX)) {
            this.processLauncher.addCommand("-Xdock:icon=" + (new File(this.assetsDir, "icons/minecraft.icns")).getAbsolutePath(), "-Xdock:name=Minecraft");
         }

         if (this.os.equals(OperatingSystem.WINDOWS)) {
            this.processLauncher.addCommand("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
         }

         this.processLauncher.addCommand(OperatingSystem.is32Bit() ? "-Xmx512M" : "-Xmx1G");
         this.processLauncher.addCommand("-Djava.library.path=" + this.nativeDir.getAbsolutePath());
         this.processLauncher.addCommand("-cp", this.constructClassPath(this.version));
         this.processLauncher.addCommand(this.version.getMainClass());
         this.processLauncher.addCommands(this.getMinecraftArguments());
         this.processLauncher.addCommands(this.args);
         this.processLauncher.addCommand("--width", this.t.settings.get("minecraft.width"));
         this.processLauncher.addCommand("--height", this.t.settings.get("minecraft.height"));
         if (!this.forceupdate && resourcesAreReady) {
            this.launch_();
         } else {
            try {
               this.vm.downloadResources(this.resources, this.forceupdate);
            } catch (IOException var3) {
               throw new MinecraftLauncherException("Cannot download resources!", "download-resources", var3);
            }

            if (this.resources.get().isEmpty()) {
               this.launch_();
            } else {
               this.log("Downloading resources...");
               this.resources.setHandler(new DownloadableHandler() {
                  public void onStart() {
                  }

                  public void onCompleteError() {
                     if (MinecraftLauncher.this.resources.getErrors() > 0) {
                        MinecraftLauncher.this.onError(new MinecraftLauncherException("Errors occurred, cancelling.", "download"));
                     }

                  }

                  public void onComplete() {
                     MinecraftLauncher.this.log("Resources have been downloaded!");
                     MinecraftLauncher.this.launch();
                  }
               });
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

   }

   private void launch_() throws MinecraftLauncherException {
      this.log("Starting Minecraft " + this.version_name + "...");

      try {
         List parts = this.processLauncher.getFullCommands();
         StringBuilder full = new StringBuilder();
         boolean first = true;

         String part;
         for(Iterator var5 = parts.iterator(); var5.hasNext(); full.append(part)) {
            part = (String)var5.next();
            if (first) {
               first = false;
            } else {
               full.append(" ");
            }
         }

         this.log("Running: " + full.toString());
         this.t.hide();
         this.onLaunch();
         JavaProcess process = this.processLauncher.start();
         process.safeSetExitRunnable(this);
      } catch (Exception var6) {
         throw new MinecraftLauncherException("Cannot start the game!", "start", var6);
      }
   }

   private void removeNatives() {
      this.nativeDir.delete();
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
         map.put("auth_session", "-");
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

   void showWarning(String message, String langpath, String replace) {
      this.log("[WARNING] " + message);
      if (this.listener != null) {
         this.listener.onMinecraftWarning(langpath, replace);
      }

   }

   void showWarning(String message, String langpath) {
      this.showWarning(message, langpath, (String)null);
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
         this.con.log("[MinecraftLauncher]", (Object)w);
      }

      U.log("[MinecraftLauncher]", (Object)w);
   }

   private void logerror(Throwable e) {
      e.printStackTrace();
      if (this.con != null) {
         this.con.log("[MinecraftLauncher]", (Object)"Error occurred. Logger won't vanish automatically.");
         this.con.log(e);
      }
   }

   public void onJavaProcessEnded(JavaProcess paramJavaProcess) {
      this.t.show();
      if (this.listener != null) {
         this.listener.onMinecraftClose();
      }

      if (this.con != null) {
         this.con.plog("Minecraft closed successfully. Logger will vanish in 5 seconds.");
         this.con.killIn(5000L);
      }
   }

   public void onJavaProcessError(JavaProcess jp, Throwable e) {
      e.printStackTrace();
      this.t.show();
      if (this.con != null) {
         this.con.log("Error has occurred:", (Throwable)e);
      }

      if (this.listener != null) {
         this.listener.onMinecraftError(e);
      }

   }

   public void onJavaProcessLog(JavaProcess jp, String line) {
      U.log(">", (Object)line);
      if (this.con != null) {
         this.con.log((Object)line);
      }

   }

   public Console getConsole() {
      return this.con;
   }
}
