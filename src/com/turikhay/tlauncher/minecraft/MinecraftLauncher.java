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
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.Console;
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
	private final String prefix = "[MinecraftLauncher]";
	private final OperatingSystem os = OperatingSystem.getCurrentPlatform();
	
	private TLauncher t;
	private Settings s;
	private Downloader d;
	private VersionManager vm;
	private Console con;
	
	private MinecraftLauncherListener listener;
	
	private boolean working, launching, installed, forceupdate;
	private VersionSyncInfo syncInfo;
	private CompleteVersion version;
	private String username, version_name, margs, jargs;
	private String[] args;
	private int width, height;
	
	private DownloadableContainer jar = new DownloadableContainer(), resources = new DownloadableContainer();
	
	private JavaProcessLauncher processLauncher;
	private File nativeDir, gameDir, assetsDir;
	
	public MinecraftLauncher(TLauncher t, MinecraftLauncherListener listener, String[] args, boolean forceupdate){
		Thread.setDefaultUncaughtExceptionHandler(new MinecraftLauncherExceptionHandler(this));
		
		this.t = t; this.s = t.getSettings(); this.d = this.t.getDownloader(); this.vm = this.t.getVersionManager();
		this.listener = listener;
		this.version_name = s.get("login.version");
		this.syncInfo = vm.getVersionSyncInfo(version_name);
		this.forceupdate = forceupdate;
		
		this.username = s.get("login.username");
		
		this.jargs = s.get("minecraft.javaargs"); if(jargs == null) jargs = "";
		this.margs = s.get("minecraft.args"); if(margs == null) margs = "";
		this.args = args;
		this.width = s.getInteger("minecraft.size.width");
		this.height = s.getInteger("minecraft.size.height");
		
		this.con = new Console("Minecraft Logger", s.getBoolean("gui.console"));
		
		log("Minecraft Launcher v"+VERSION+" is started!");
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
			showWarning("Current launcher version is incompatible with selected version "+version_name+" (version "+version.getMinimumLauncherVersion()+" required).", "incompatible.launcher");
		
		if(!forceupdate && installed){
			prepare_();
			return;
		}
		
		log("Downloading version "+version_name+"...");
			
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
	}
	
	private void prepare(){
		try{ prepare_(); }catch(Exception e){ onError(e); }
	}
	
	private void prepare_() throws MinecraftLauncherException {
		if(launching) throw new IllegalStateException("The game is already launching!");
		
		this.launching = true;
		this.onPrepare();
		
		this.gameDir = new File(s.get("minecraft.gamedir"));
		this.nativeDir = new File(gameDir, "versions/" + this.version.getId() + "/" + "natives");
		if (!this.nativeDir.isDirectory()) this.nativeDir.mkdirs();
		
		try {
			this.unpackNatives(forceupdate);
		} catch (IOException e){ throw new MinecraftLauncherException("Cannot unpack natives!", "unpack-natives", e); }
		
	    processLauncher = new JavaProcessLauncher(os.getJavaDir(), new String[0]);
	    processLauncher.directory(gameDir);
	    
	    this.assetsDir = new File(gameDir, "assets");
	    boolean resourcesAreReady = this.compareResources();
	    
	    if(os.equals(OperatingSystem.OSX))
	        processLauncher.addCommand("-Xdock:icon=" + new File(assetsDir, "icons/minecraft.icns").getAbsolutePath(), "-Xdock:name=Minecraft");
	    if(os.equals(OperatingSystem.WINDOWS))
	    	processLauncher.addCommand("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
	    
	    	processLauncher.addCommand((OperatingSystem.is32Bit()) ? "-Xmx512M" : "-Xmx1G");
	    	
	        processLauncher.addCommand("-Djava.library.path=" + this.nativeDir.getAbsolutePath()+"");
	        processLauncher.addCommand("-cp", constructClassPath(this.version));
	        
	        processLauncher.addCommand(jargs);
	        
	        processLauncher.addCommand(this.version.getMainClass());
	        
	        processLauncher.addCommands(getMinecraftArguments());
	        
	        processLauncher.addCommand("--width", width);
	        processLauncher.addCommand("--height", height);
	        
	    	processLauncher.addCommands(args);
	        processLauncher.addCommands(margs.split(" "));
	        
	    if(resourcesAreReady){ launch_(); return; }
	    
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
		U.gc();		
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
	
	private boolean compareResources(){
		log("Comparing resources...");
		long start = System.nanoTime();
		
		boolean result = vm.checkResources(true);
		
		long end = System.nanoTime(), delta = end - start;
		log("Delta time to compare resources: " + delta / 1000000L + " ms.");
		
		return result;
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
		log("Constructing Classpath...");
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
		log("Getting Minecraft arguments...");
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
	
	void showWarning(String message, String langpath, Object replace){ log("[WARNING] " + message); if(listener != null) listener.onMinecraftWarning(langpath, replace); }
	void showWarning(String message, String langpath){ this.showWarning(message, langpath, null); }
	
	void onError(MinecraftLauncherException e){ logerror(e); if(listener != null) listener.onMinecraftError(e); }
	void onError(Throwable e){ logerror(e); if(listener != null) listener.onMinecraftError(e); }
	private void log(Object w){ con.log(prefix, w); U.log(prefix, w); }
	private void logerror(Throwable e){ e.printStackTrace(); con.log(prefix, "Error occurred. Logger won't vanish automatically."); con.log(e); }

	public void onJavaProcessEnded(JavaProcess jp) {
		t.show();
		int exit = jp.getExitCode();
		
		if(listener != null)
			listener.onMinecraftClose();
		
		log("Minecraft closed with exit code: "+exit);
		if(exit != 0)
			handleCrash();
		else
			con.killIn(2000);
		
		U.gc();
	}
	
	private void handleCrash(){
		
		String crash_report = null;
		String output = con.getOutput();
		for(String line : output.split("\n"))
			if(line.startsWith("#@!@#")){
				String[] line_split = line.split("#@!@#");
				if(line_split.length != 3) crash_report = "/* MISSING_PATH */";
				else crash_report = line_split[2];
				break;
			}
		if(crash_report == null) return;
		log("Crash report found. Console won't vanish automatically.");
		
		con.show();
		
		if(listener == null) return;
		MinecraftLauncherException ex = new MinecraftLauncherException("Minecraft exited with illegal code.", "exit-code", crash_report);
		listener.onMinecraftError(ex);
	}

	public void onJavaProcessError(JavaProcess jp, Throwable e) {
		e.printStackTrace();
		
		t.show();
		
		con.log("Error has occurred:", e);
		
		if(listener != null)
			listener.onMinecraftError(e);
	}

	public void onJavaProcessLog(JavaProcess jp, String line) {
		U.plog(">", line);
		
		con.log(line);
	}
	
	public Console getConsole(){
		return con;
	}
}
