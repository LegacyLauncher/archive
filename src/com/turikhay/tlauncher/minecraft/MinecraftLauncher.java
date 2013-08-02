package com.turikhay.tlauncher.minecraft;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.text.StrSubstitutor;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.DownloadableHandler;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;

import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.process.JavaProcess;
import net.minecraft.launcher_.process.JavaProcessLauncher;
import net.minecraft.launcher_.process.JavaProcessListener;
import net.minecraft.launcher_.updater.VersionManager;
import net.minecraft.launcher_.updater.VersionSyncInfo;
import net.minecraft.launcher_.versions.CompleteVersion;
import net.minecraft.launcher_.versions.ExtractRules;
import net.minecraft.launcher_.versions.Library;

public class MinecraftLauncher extends Thread implements JavaProcessListener {
	public final int VERSION = 7;
	private final OperatingSystem os = OperatingSystem.getCurrentPlatform();
	
	private TLauncher t;
	private Downloader d;
	private VersionManager vm;
	
	private MinecraftLauncherListener listener;
	
	private boolean working, launching, installed, forceupdate;
	private VersionSyncInfo syncInfo;
	private CompleteVersion version;
	private String username, version_name;
	private String[] args;
	
	private DownloadableContainer jar = new DownloadableContainer(), resources = new DownloadableContainer();
	
	private JavaProcessLauncher processLauncher;
	private File nativeDir, gameDir, assetsDir;
	
	public MinecraftLauncher(TLauncher t, MinecraftLauncherListener listener, String version_name, boolean forceupdate, String username, String[] args){
		Thread.setDefaultUncaughtExceptionHandler(new MinecraftLauncherExceptionHandler(this));
		
		this.t = t; this.d = this.t.downloader; this.vm = this.t.vm;
		this.listener = listener;
		this.version_name = version_name;
		this.syncInfo = vm.getVersionSyncInfo(version_name);
		this.forceupdate = forceupdate;
		
		this.username = username;
		
		this.args = args;
	}
	
	public void run(){
		try{ check(); }catch(MinecraftLauncherException me){ onError(me); }catch(Exception e){ onError(e); }
	}
	
	private void check() throws MinecraftLauncherException {
		if(working) throw new IllegalStateException("MinecraftLauncher is already working!");
		log("Checking files for version "+version_name+"...");
		
		this.working = true;
		this.onCheck();
		
		if(syncInfo == null) throw new IllegalStateException("Cannot find version \""+version_name+"\"");
		try {
			this.version = vm.getLatestCompleteVersion(syncInfo);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		this.installed = syncInfo.isInstalled() && vm.getLocalVersionList().hasAllFiles(version, os);
		
		if(!version.appliesToCurrentEnvironment())
			showWarning("Version "+version_name+" is incompatible with your environment.", "incompatible");
		if(version.getMinimumLauncherVersion() > VERSION)
			showWarning("Current version of using launcher is incompatible with selected version "+version_name+" (version "+version.getMinimumLauncherVersion()+" required).", "incompatible.launcher");
		
		
		if(forceupdate || !installed){
			log("Downloading libraries and "+version_name+".jar");
			
			try {
				vm.downloadVersion(syncInfo, jar);
			} catch (IOException e) { throw new MinecraftLauncherException("Cannot get downloadable jar!", "download-jar", e); }
			
			jar.setHandler(new DownloadableHandler(){
				public void onStart(){}
				public void onCompleteError(){
					onError(new MinecraftLauncherException("Errors occurred, cancelling.", "download"));
					return;
				}
				public void onComplete(){
					log("Version "+version_name+" downloaded!");
					
					vm.getLocalVersionList().saveVersion(version);
					
					prepare();
				}
			});
			d.add(jar);
			d.launch();
		} else {
			prepare_();
			return;
		}
	}
	
	private void prepare(){
		try{ prepare_(); }catch(Exception e){ onError(e); }
	}
	
	private void prepare_() throws MinecraftLauncherException {
		if(launching) throw new IllegalStateException("The game is already launching!");
		
		this.launching = true;
		this.onPrepare();
		
		this.nativeDir = new File(MinecraftUtil.getWorkingDirectory(), "versions/" + this.version.getId() + "/" + this.version.getId() + "-natives");
		if (!this.nativeDir.isDirectory()) this.nativeDir.mkdirs();
		
		try {
			this.unpackNatives(forceupdate);
		} catch (IOException e){ throw new MinecraftLauncherException("Cannot unpack natives!", "unpack-natives", e); }
		
		this.gameDir = MinecraftUtil.getWorkingDirectory();
		
	    processLauncher = new JavaProcessLauncher(os.getJavaDir(), new String[0]);
	    processLauncher.directory(gameDir);
	    
	    this.assetsDir = new File(MinecraftUtil.getWorkingDirectory(), "assets");
	    boolean resourcesAreReady = vm.checkResources();
	    
	    if(os.equals(OperatingSystem.OSX))
	        processLauncher.addCommand("-Xdock:icon=" + new File(assetsDir, "icons/minecraft.icns").getAbsolutePath(), "-Xdock:name=Minecraft");
	    if(os.equals(OperatingSystem.WINDOWS))
	    	processLauncher.addCommand("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
	    
	    	processLauncher.addCommand((OperatingSystem.is32Bit()) ? "-Xmx512M" : "-Xmx1G");
	    	
	        processLauncher.addCommand("-Djava.library.path=" + this.nativeDir.getAbsolutePath()+"");
	        processLauncher.addCommand("-cp", constructClassPath(this.version));
	        processLauncher.addCommand(this.version.getMainClass());
	        
	        processLauncher.addCommands(getMinecraftArguments());
	        
	        processLauncher.addCommands(args);
	        
	        processLauncher.addCommand("--width", t.settings.get("minecraft.width"));
	        processLauncher.addCommand("--height", t.settings.get("minecraft.height"));
	        
	    if(!forceupdate && resourcesAreReady){ launch_(); return; }
	    
	    try {
			vm.downloadResources(resources, forceupdate);
		} catch (IOException e) { throw new MinecraftLauncherException("Cannot download resources!", "download-resources", e); }
	    
	    if(resources.get().isEmpty()){ launch_(); return; }
	    
	    log("Downloading resources...");
	    
	    resources.setHandler(new DownloadableHandler(){
			public void onStart() {}
			public void onCompleteError() {
				if(resources.getErrors() > 0)
					onError(new MinecraftLauncherException("Errors occurred, cancelling.", "download"));
			}
			public void onComplete() {			
				log("Resources have been downloaded!");				
				launch();
			}
	    });
	    d.add(resources);
	    d.launch();
	}
	
	public void launch(){
		try{ launch_(); }catch(Exception e){ onError(e); }
	}
	
	private void launch_() throws MinecraftLauncherException {
		log("Starting Minecraft "+version_name+"...");
		
	     try {
	    	 List<String> parts = processLauncher.getFullCommands();
	    	 StringBuilder full = new StringBuilder();
	    	 boolean first = true;
	    	 
	    	 for(String part : parts){
	    		 if(first) first = false; else full.append(" ");
	    		 full.append(part);
	    	 }
	    	 
	    	 log("Running: "+full.toString());
	    	 
	    	 t.hide(); onLaunch();
	    	 JavaProcess process = processLauncher.start();
	    	 process.safeSetExitRunnable(this);
	     }catch(Exception e){ throw new MinecraftLauncherException("Cannot start the game!", "start", e); }
	}
	
	private void removeNatives(){
		this.nativeDir.delete();
	}
	
	private void unpackNatives(boolean force) throws IOException {
		log("Unpacking natives...");
	    Collection<Library> libraries = version.getRelevantLibraries();
	    
	    ZipFile zip; BufferedOutputStream bufferedOutputStream;
	    
	    if(force) removeNatives();

	    for (Library library : libraries) {
	    	Map<OperatingSystem, String> nativesPerOs = library.getNatives();

	    	if(nativesPerOs != null && nativesPerOs.get(os) != null) {
	    		File file = new File(MinecraftUtil.getWorkingDirectory(), "libraries/" + library.getArtifactPath(nativesPerOs.get(os)));
	    		
	    		zip = new ZipFile(file);
	    		ExtractRules extractRules = library.getExtractRules();
	    		Enumeration<? extends ZipEntry> entries = zip.entries();
	    		
	    		while (entries.hasMoreElements()) {
	    			ZipEntry entry = entries.nextElement();
	    			if(extractRules == null || extractRules.shouldExtract(entry.getName() ) )
	    			{
	    				File targetFile = new File(this.nativeDir, entry.getName());
	    				if(!force && targetFile.exists()) continue;
	    				if (targetFile.getParentFile() != null) targetFile.getParentFile().mkdirs();

	    				if (!entry.isDirectory()) {
	    					BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));

	    					byte[] buffer = new byte[2048];
	    					FileOutputStream outputStream = new FileOutputStream(targetFile);
	    					bufferedOutputStream = new BufferedOutputStream(outputStream);
	    					
	    					int length;
	    					while ((length = inputStream.read(buffer, 0, buffer.length)) != -1)
	    						bufferedOutputStream.write(buffer, 0, length);
	    					
	    					inputStream.close();
	    					bufferedOutputStream.close();
	    				}
	    			}
	    		}
	    		zip.close();
	    	}
	    }
	}
	
	private String constructClassPath(CompleteVersion version) throws MinecraftLauncherException {
		log("Constructing ClassPath...");
		StringBuilder result = new StringBuilder();
	    Collection<File> classPath = version.getClassPath(os, MinecraftUtil.getWorkingDirectory());
	    String separator = System.getProperty("path.separator");

	    for (File file : classPath) {
	      if (!file.isFile()) throw new MinecraftLauncherException("Classpath is not found: " + file, "classpath", file);
	      if (result.length() > 0) result.append(separator);
	      result.append(file.getAbsolutePath());
	    }

	    return result.toString();
	}
	
	private String[] getMinecraftArguments() throws MinecraftLauncherException {
		log("Getting Minecraft Arguments...");
		if (version.getMinecraftArguments() == null)
			throw new MinecraftLauncherException("Can't run version, missing minecraftArguments", "noArgs");
		Map<String, String> map = new HashMap<String, String>();
		StrSubstitutor substitutor = new StrSubstitutor(map);
		    String[] split = version.getMinecraftArguments().split(" ");

		    map.put("auth_username", username);
		    map.put("auth_session", "-");

		    map.put("auth_player_name", username);
		    map.put("auth_uuid", new UUID(0L, 0L).toString());

		    map.put("profile_name", "(Default)");
		    map.put("version_name", version.getId());

		    map.put("game_directory", gameDir.getAbsolutePath());
		    map.put("game_assets", assetsDir.getAbsolutePath());

		    for (int i = 0; i < split.length; i++) {
		      split[i] = substitutor.replace(split[i]);
		    }

		    return split;
		  }
	
	void onCheck(){ if(listener != null) listener.onMinecraftCheck(); }
	void onPrepare(){ if(listener != null) listener.onMinecraftPrepare(); }
	void onLaunch(){ if(listener != null) listener.onMinecraftLaunch(); }
	
	void showWarning(String message, String langpath, String replace){ log("[WARNING] " + message); if(listener != null) listener.onMinecraftWarning(langpath, replace); }
	void showWarning(String message, String langpath){ this.showWarning(message, langpath, null); }
	
	void onError(MinecraftLauncherException e){ e.printStackTrace(); if(listener != null) listener.onMinecraftError(e); }
	void onError(Throwable e){ e.printStackTrace(); if(listener != null) listener.onMinecraftError(e); }
	private void log(Object w){ U.log("[MinecraftLauncher] ", w); }

	public void onJavaProcessEnded(JavaProcess paramJavaProcess) {
		t.show();
		
		if(listener != null)
			listener.onMinecraftClose();
	}

	public void onJavaProcessError(JavaProcess jp, Throwable e) {
		e.printStackTrace();
		
		t.show();
		
		if(listener != null)
			listener.onMinecraftError(e);
	}
}
