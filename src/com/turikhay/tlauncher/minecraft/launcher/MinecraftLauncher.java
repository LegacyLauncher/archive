package com.turikhay.tlauncher.minecraft.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.component.managers.*;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.ui.console.Console;
import com.turikhay.util.U;
import com.turikhay.util.logger.LinkedStringStream;
import com.turikhay.util.logger.PrintLogger;

public class MinecraftLauncher {
	public final static int OFFICIAL_VERSION = 14, ALTERNATIVE_VERSION = 6;
	public static final byte
		NONE = 0,
		COLLECTING = 1,
		DOWNLOADING = 2,
		CONSTRUCTING = 3,
		LAUNCHING = 4,
		POSTLAUNCH = 5;
	
	private final ComponentManager manager;
	private final Configuration settings;
	
	private final VersionManager vm;
	private final ProfileManager pm;
	
	private final LinkedStringStream output;
	private final PrintLogger logger;
	private final Console console;
	
	private final List<MinecraftListener> listeners;
	
	private byte step;
	
	//
	
	private String versionName;
	private VersionSyncInfo version;
	private CompleteVersion latestVersion;
	
	private String accountName;
	private Account account;
	
	private DownloadableContainer assetsContainer, execContainer;
	private boolean downloadFlag; // TODO removeFlag
	
	private File gameDir, assetsDir, nativeDir;
	private JavaProcessLauncher process;
	
	//
	
	public MinecraftLauncher(ComponentManager manager, Configuration configuration) {
		if(manager == null)
			throw new NullPointerException("Ti ohuel?");
		
		if(configuration == null)
			throw new NullPointerException("Configuration is NULL!");
		
		this.settings = configuration;
		
		this.manager = manager;
		this.vm = manager.getVersionManager();
		this.pm = manager.getProfileManager();
		
		this.output = new LinkedStringStream();
		this.logger = new PrintLogger(output);
		this.console = new Console(settings, logger, "Minecraft Logger", false);
		
		this.listeners = Collections.synchronizedList(new ArrayList<MinecraftListener>());
		
		this.step = NONE;
		
		//
		
		log("Minecraft Launcher ["+OFFICIAL_VERSION+";"+ALTERNATIVE_VERSION+"] has initialized");
		log("Running under TLauncher "+TLauncher.getVersion()+" "+TLauncher.getBrand());
		log("Current machine:", OperatingSystem.getCurrentInfo());
	}
	
	public void addListener(MinecraftListener listener){
		if(listener == null)
			throw new NullPointerException();
		
		this.listeners.add(listener);
	}
	
	public void start() {
		if(step > NONE)
			throw new IllegalStateException("MinecraftLauncher is already working!");
		
		try{ this.executeStep(COLLECTING); }
		catch(Throwable e){
			for(MinecraftListener listener : listeners)
				listener.onMinecraftError(e);
			
			this.step = NONE;
		}
	}
	
	private void executeStep(byte step) throws Throwable {
		if(step == NONE)
			throw new IllegalArgumentException("Cannot execute empty step ("+step+"). Minimal:" + COLLECTING);
		
		if(step > POSTLAUNCH)
			throw new IllegalArgumentException("Specified step is out of bounds ("+step+"). Maximal:" + POSTLAUNCH);
		
		switch(step){
		case NONE:
			throw new IllegalArgumentException("Cannot execute empty step ("+step+"). Minimal:" + COLLECTING);
		case COLLECTING:
			collectInfo();
		case DOWNLOADING:
			downloadResources();
		case CONSTRUCTING:
			constructProcess();
		case LAUNCHING:
			launchMinecraft();
			break;
		case POSTLAUNCH:
			postLaunch();
		}
	}
	
	private void nextStep() throws Throwable {		
		if(++step > POSTLAUNCH)
			step = COLLECTING;
		
		this.executeStep(step);
	}
	
	private void collectInfo() {
		this.step = COLLECTING;
		log("Collecting info:");
	}
	
	private void downloadResources() {
		this.step = DOWNLOADING;
	}
	
	private void constructProcess() {
		this.step = CONSTRUCTING;
		log("Constructing process:");
	}
	
	private void launchMinecraft() {
		this.step = LAUNCHING;
		log("Launching Minecraft:");
	}
	
	private void postLaunch() {
		this.step = POSTLAUNCH;
		log("Post-launch step:");
	}
	
	protected void log(Object...o){ U.log("["+getClass().getSimpleName()+"]", o); }
}
