package com.turikhay.tlauncher;

import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.minecraft.Crash;
import com.turikhay.tlauncher.minecraft.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.Console;
import com.turikhay.util.U;
import joptsimple.OptionSet;
import net.minecraft.launcher.updater.VersionManager;

public class TLauncherNoGraphics implements MinecraftLauncherListener {
   private final TLauncher t;
   private Downloader d;
   private GlobalSettings g;
   private VersionManager vm;
   private OptionSet args;
   private MinecraftLauncher launcher;
   private boolean exit;

   TLauncherNoGraphics(TLauncher tlauncher) {
      this.t = tlauncher;
      this.g = this.t.getSettings();
      this.d = this.t.getDownloader();
      this.vm = this.t.getVersionManager();
      this.vm.refreshVersions(true);
      this.args = this.t.getArguments();
      this.exit = !this.args.has("console");
      this.launcher = new MinecraftLauncher(this, this.d, this.g, this.vm, this.args.has("force"), !this.args.has("nocheck"));
      this.launcher.getConsole().setCloseAction(Console.CloseAction.EXIT);
      this.launcher.start();
      U.log("Loaded NoGraphics mode.");
   }

   public void onMinecraftCheck() {
   }

   public void onMinecraftPrepare() {
   }

   public void onMinecraftLaunch() {
   }

   public void onMinecraftLaunchStop() {
      TLauncher.kill();
   }

   public void onMinecraftClose() {
      TLauncher.kill();
   }

   public void onMinecraftError(MinecraftLauncherException knownError) {
      Alert.showError(knownError, this.exit);
   }

   public void onMinecraftError(Throwable unknownError) {
      Alert.showError(unknownError, this.exit);
   }

   public void onMinecraftWarning(String langpath, Object replace) {
   }

   public void onMinecraftCrash(Crash crash) {
   }
}
