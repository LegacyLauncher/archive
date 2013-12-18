package com.turikhay.tlauncher;

import joptsimple.OptionSet;

import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.minecraft.Crash;
import com.turikhay.tlauncher.minecraft.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.Console.CloseAction;
import com.turikhay.util.U;

import net.minecraft.launcher.updater.VersionManager;

public class TLauncherNoGraphics implements MinecraftLauncherListener {
	private final TLauncher t;
	private Downloader d;
	private GlobalSettings g;
	private VersionManager vm;
	
	private OptionSet args;
	
	private MinecraftLauncher launcher;
	private boolean exit;
	
	TLauncherNoGraphics(TLauncher tlauncher){
		this.t = tlauncher;
		
		this.g = t.getSettings();
		this.d = t.getDownloader();
		this.vm = t.getVersionManager(); vm.refreshVersions(true);
		
		this.args = t.getArguments();
		this.exit = !args.has("console");
		
		this.launcher = new MinecraftLauncher(this, d, g, vm, args.has("force"), !args.has("nocheck"));
		this.launcher.getConsole().setCloseAction(CloseAction.EXIT);
		this.launcher.start();
		
		U.log("Loaded NoGraphics mode.");
	}

	public void onMinecraftCheck(){}
	public void onMinecraftPrepare(){}
	public void onMinecraftLaunch(){}
	public void onMinecraftLaunchStop() { TLauncher.kill(); }
	public void onMinecraftClose() { TLauncher.kill(); }
	public void onMinecraftError(MinecraftLauncherException knownError) {
		Alert.showError(knownError, exit);
	}
	public void onMinecraftError(Throwable unknownError) {
		Alert.showError(unknownError, exit);
	}
	public void onMinecraftWarning(String langpath, Object replace) {}
	public void onMinecraftCrash(Crash crash) {}
}
