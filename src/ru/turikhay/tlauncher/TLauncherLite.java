package ru.turikhay.tlauncher;

import joptsimple.OptionSet;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.console.Console;

public class TLauncherLite implements MinecraftListener {
   private final TLauncher tlauncher;
   private final OptionSet args;

   TLauncherLite(TLauncher tlauncher) {
      if (tlauncher == null) {
         throw new NullPointerException();
      } else {
         this.tlauncher = tlauncher;
         tlauncher.getVersionManager().startRefresh(true);
         tlauncher.getProfileManager().refreshComponent();
         this.args = tlauncher.getArguments();
         MinecraftLauncher launcher = new MinecraftLauncher(this, this.args);
         launcher.addListener(tlauncher.getMinecraftListener());
         launcher.addListener(this);
         if (launcher.getConsole() != null) {
            launcher.getConsole().setCloseAction(Console.CloseAction.EXIT);
         }

         launcher.start();
      }
   }

   public TLauncher getLauncher() {
      return this.tlauncher;
   }

   public void onMinecraftPrepare() {
   }

   public void onMinecraftAbort() {
   }

   public void onMinecraftLaunch() {
   }

   public void onMinecraftClose() {
      if (!this.args.has("console") && this.tlauncher.getSettings().getConsoleType().equals(Configuration.ConsoleType.NONE)) {
         TLauncher.kill();
      }

   }

   public void onMinecraftError(Throwable e) {
   }

   public void onMinecraftKnownError(MinecraftException e) {
   }

   public void onMinecraftCrash(Crash crash) {
   }
}
