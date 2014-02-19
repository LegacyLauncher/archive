package com.turikhay.tlauncher;

import joptsimple.OptionSet;

import com.turikhay.tlauncher.component.managers.AssetsManager;
import com.turikhay.tlauncher.component.managers.ProfileManager;
import com.turikhay.tlauncher.component.managers.VersionManager;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.minecraft.Crash;
import com.turikhay.tlauncher.minecraft.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.console.Console;
import com.turikhay.util.U;

public class TLauncherNoGraphics
implements MinecraftLauncherListener
{
private final TLauncher t;
private Downloader d;
private Configuration g;
private VersionManager vm;
private AssetsManager am;
private ProfileManager pm;
private OptionSet args;
private MinecraftLauncher launcher;
private boolean exit;

TLauncherNoGraphics(TLauncher tlauncher)
{
  this.t = tlauncher;
  
  this.g = this.t.getSettings();
  this.d = this.t.getDownloader();
  this.vm = this.t.getVersionManager();this.vm.startRefresh(true);
  this.am = this.t.getManager().getAssetsManager();
  this.pm = this.t.getProfileManager();
  
  this.pm.refresh();
  
  this.args = this.t.getArguments();
  this.exit = (!this.args.has("console"));
  
  this.launcher = new MinecraftLauncher(this, this.d, this.g, this.vm, this.am, this.pm, this.args.has("force"), !this.args.has("nocheck"));
  if (this.launcher.getConsole() != null) {
    this.launcher.getConsole().setCloseAction(Console.CloseAction.EXIT);
  }
  this.launcher.start();
  
  U.log(new Object[] {"Loaded NoGraphics mode." });
}

public void onMinecraftCheck() {}

public void onMinecraftPrepare() {}

public void onMinecraftLaunch() {}

public void onMinecraftLaunchStop() {}

public void onMinecraftClose() {}

public void onMinecraftKnownError(MinecraftLauncherException knownError)
{
  Alert.showError(knownError, this.exit);
}

public void onMinecraftError(Throwable unknownError)
{
  Alert.showError(unknownError, this.exit);
}

public void onMinecraftWarning(String langpath, Object replace) {}

public void onMinecraftCrash(Crash crash) {}
}