package com.turikhay.tlauncher.minecraft;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.google.gson.Gson;
import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.GlobalSettings.ActionOnLaunch;
import com.turikhay.tlauncher.ui.Console;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;
import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.updater.AssetIndex.AssetObject;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.updater.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ExtractRules;
import net.minecraft.launcher.versions.Library;

public class MinecraftLauncher extends Thread implements JavaProcessListener {
	public final static int VERSION = 13, TLAUNCHER_VERSION = 6;
	final String prefix = "[MinecraftLauncher]";
	
	final OperatingSystem os = OperatingSystem.getCurrentPlatform();
	final Gson gson = new Gson();
	final DateTypeAdapter dateAdapter = new DateTypeAdapter();
	
	//private TLauncher t;
	GlobalSettings s; 
	Downloader d;
	Console con;
	
	final MinecraftLauncherListener listener;
	final VersionManager vm;
	
	final boolean exit;
	
	boolean init, working, launching;
	final boolean forceupdate, check, console;
	VersionSyncInfo syncInfo;
	CompleteVersion version;
	final String username, version_name, jargs, margs, gamedir, javadir;
	final int width, height;
	
	DownloadableContainer ver = new DownloadableContainer(), res = new DownloadableContainer();
	List<AssetObject> assetsList;
	private boolean downloadVersion, downloadAssets, downloadFlag;
	
	JavaProcessLauncher processLauncher;
	File nativeDir, gameDir, assetsDir;
	
	private MinecraftLauncher (
			MinecraftLauncherListener listener, VersionManager vm,
			String version_name, String username, String token, String gamedir, String javadir, String jargs, String margs,
			int[] sizes,
			boolean force, boolean check, boolean exit, boolean console
		){
		Thread.setDefaultUncaughtExceptionHandler(new MinecraftLauncherExceptionHandler(this));
		
		this.listener = listener; this.vm = vm;
		
		this.version_name = version_name;
		this.forceupdate = force;
		
		this.username = username;
		
		this.gamedir = gamedir;
		this.javadir = (javadir == null)? os.getJavaDir() : javadir;
		this.jargs = (jargs == null)? "" : jargs;
		this.margs = (margs == null)? "" : margs;
		
		this.console = console;
		this.check = check;
		this.exit = exit;
		
		this.width = sizes[0];
		this.height = sizes[1];
	}
	
	public MinecraftLauncher(MinecraftLauncherListener listener, Downloader d, GlobalSettings s, VersionManager vm, boolean force, boolean check){
		this(
				listener, vm,
				s.get("login.version"), s.get("login.username"), s.get("login.token"), s.get("minecraft.gamedir"), s.get("minecraft.javadir"), s.get("minecraft.javaargs"), s.get("minecraft.args"),
				s.getWindowSize(),
				force, check, s.getActionOnLaunch() == ActionOnLaunch.EXIT, s.getBoolean("gui.console")
		);
		
		this.s = s;
		this.d = d;
		
		init();
	}
	
	public MinecraftLauncher(TLauncher t, MinecraftLauncherListener listener, boolean force, boolean check){
		this(listener, t.getDownloader(), t.getSettings(), t.getVersionManager(), force, check);
		
		init();
	}
	
	public void init(){
		if(init) return; init = true;
		
		if(!exit && s != null)
			con = new Console(s, "Minecraft Logger", console);
		
		log("Minecraft Launcher ["+VERSION+";"+TLAUNCHER_VERSION+"] is started!");
		log("Running under TLauncher "+TLauncher.getVersion()+" "+TLauncher.getBrand());
	}
	
	public void run(){
		try{ check(); }catch(MinecraftLauncherException me){ onError(me); }catch(Exception e){ onError(e); }
	}
	
	private void check() throws MinecraftLauncherException {
		if(working) throw new IllegalStateException("MinecraftLauncher is already working!");
		
		if(version_name == null || version_name.length() == 0)
			throw new MinecraftLauncherException("Version name is invalid: \""+version_name+"\"", "version-invalid", version_name);
		
		try{
			FileUtil.createFolder(gamedir);
		}catch(Exception e){
			throw new MinecraftLauncherException("Cannot find folder: "+gamedir, "folder-not-found", gamedir);
		}
		
		this.syncInfo = vm.getVersionSyncInfo(version_name);
		if(syncInfo == null)
			throw new MinecraftLauncherException("SyncInfo is NULL!", "version-not-found", version_name + "\n" + gamedir);
		
		try {
			this.version = vm.getLatestCompleteVersion(syncInfo);
		} catch (Exception e) {
			throw new MinecraftLauncherException("Cannot get version info!", "version-info", e);
		}
		
		if(check) log("Checking files for version "+version_name+"...");
		else {
			log("Checking files for version "+version_name+" skipped.");
			prepare_();
			return;
		}
		
		this.working = true;
		this.onCheck();
		
		if(version.getTLauncherVersion() != 0){
			if(version.getTLauncherVersion() > TLAUNCHER_VERSION)
				throw new MinecraftLauncherException("TLauncher is incompatible with this extra version (needed "+version.getTLauncherVersion()+").", "incompatible");
		} else {
			if(!version.appliesToCurrentEnvironment())
				showWarning("Version "+version_name+" is incompatible with your environment.", "incompatible");
			if(version.getMinimumLauncherVersion() > VERSION)
				showWarning("Current launcher version is incompatible with selected version "+version_name+" (version "+version.getMinimumLauncherVersion()+" required).", "incompatible.launcher");
		}
		
		
		assetsList = (check)? this.compareAssets() : null;
		
		downloadAssets = assetsList != null && !assetsList.isEmpty();
		if(forceupdate) downloadVersion = true;
		else downloadVersion = !syncInfo.isInstalled() || !vm.getLocalVersionList().hasAllFiles(version, os);
		
		if(!forceupdate && !downloadVersion && !downloadAssets){
			prepare_();
			return;
		}
		
		this.downloadResources();
	}
	
	private void prepare(){
		try{ prepare_(); }catch(Exception e){ onError(e); }
	}
	
	private void downloadResources() throws MinecraftLauncherException {
		if(d == null)
			throw new MinecraftLauncherException("Downloader is NULL. Cannot download version!");
		
		if(downloadVersion)
			try {
				vm.downloadVersion(ver, syncInfo, forceupdate);
			} catch (IOException e) { throw new MinecraftLauncherException("Cannot get downloadable jar!", "download-jar", e); }
		
	    if(downloadAssets)
	    	try {
	    		vm.downloadResources(res, version, assetsList, forceupdate);
	    	} catch(IOException e){ throw new MinecraftLauncherException("Cannot download resources!", "download-resources", e); }
		
		ver.addHandler(new DownloadableHandler(){
			public void onStart(){}
			public void onCompleteError(){
				onError(new MinecraftLauncherException("Errors occurred, cancelling.", "download"));
			}
			public void onAbort(){
				onStop();
			}
			public void onComplete(){
				log("Version "+version_name+" downloaded!");
				
				vm.getLocalVersionList().saveVersion(version);
				
				if(downloadFlag) prepare();
				else downloadFlag = true;
			}
		});
		ver.setConsole(con);
		
		res.addHandler(new DownloadableHandler(){
			public void onStart() {}
			public void onCompleteError() {
				log("Error occurred while downloading the assets. Minecraft will be be launched, though");
				onContinue();
			}
			public void onComplete() {			
				log("Assets have been downloaded!");
				onContinue();
			}
			public void onAbort(){
				onStop();
			}
			private void onContinue(){				
				if(downloadFlag) prepare();
				else downloadFlag = true;
			}
		});
		res.setConsole(con);
		
		if(!downloadVersion || !downloadAssets)
			downloadFlag = true;
		
		if(downloadVersion) log("Downloading version "+version_name+"...");
		if(downloadAssets) log("Downloading assets...");
		
		d.add(ver); d.add(res);
		d.startLaunch();
	}
	
	private void prepare_() throws MinecraftLauncherException {
		if(launching) throw new IllegalStateException("The game is already launching!");
		
		this.launching = true;
		this.onPrepare();
		
		this.gameDir = new File(gamedir);
		this.nativeDir = new File(gameDir, "versions/" + this.version.getId() + "/" + "natives");
		if (!this.nativeDir.isDirectory()) this.nativeDir.mkdirs();
		
		try {
			this.unpackNatives(forceupdate);
		} catch (IOException e){ throw new MinecraftLauncherException("Cannot unpack natives!", "unpack-natives", e); }
		
		try {
			this.deleteEntries();
		} catch (IOException e){ throw new MinecraftLauncherException("Cannot delete entries!", "delete-entries", e); }
		
	    processLauncher = new JavaProcessLauncher(javadir, new String[0]);
	    processLauncher.directory(gameDir);
	    
	    try {
			this.assetsDir = this.reconstructAssets();
		} catch (IOException e) { throw new MinecraftLauncherException("Cannot reconstruct assets!", "reconstruct-assets", e); }
	    
	    if(os.equals(OperatingSystem.OSX)){
	    	File icon = null;
	    	try{ icon = getAssetObject("icons/minecraft.icns"); }
	    	catch(IOException e){ log("Cannot get icon file from assets.", e); }
	        
	    	if(icon != null)
	    		processLauncher.addCommand("-Xdock:icon=\"" + icon.getAbsolutePath() + "\"", "-Xdock:name=Minecraft");
	    }
	    if(os.equals(OperatingSystem.WINDOWS))
	    	processLauncher.addCommand("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
	    
	    	processLauncher.addCommand((os.is32Bit()) ? "-Xmx512M" : "-Xmx1G");
	    	
	        processLauncher.addCommand("-Djava.library.path=" + this.nativeDir.getAbsolutePath());
	        processLauncher.addCommand("-cp", constructClassPath(this.version));
	        
	        processLauncher.addCommands(getJVMArguments());
	        if(jargs.length() > 0) processLauncher.addSplitCommands(jargs);
	        
	        processLauncher.addCommand(this.version.getMainClass());
	        
	        processLauncher.addCommands(getMinecraftArguments());
	        
	        processLauncher.addCommand("--width", width);
	        processLauncher.addCommand("--height", height);
	        
	        if(margs.length() > 0) processLauncher.addSplitCommands(margs);
	        
	        launch();
	}
	
	public void launch(){
		try{ launch_(); }catch(Exception e){ onError(e); }
		U.gc();
	}
	
	private void launch_() throws MinecraftLauncherException {
		U.gc();
		log("Starting Minecraft "+version_name+"...");
		
		log("Launching in:", this.gameDir.getAbsolutePath());
		
   	 	log("Running (characters are not escaped):");
   	 	log(processLauncher.getCommandsAsString());
   	 
   	 	if(!exit){ onLaunch(); }
		
   	 	try {
   	 		JavaProcess process = processLauncher.start();
   	 		if(exit) TLauncher.kill(); else process.safeSetExitRunnable(this);
   	 	}catch(Exception e){ throw new MinecraftLauncherException("Cannot start the game!", "start", e); }
	}
	
	private void removeNatives(){
		this.nativeDir.delete();
	}
	
	private List<AssetObject> compareAssets(){
		this.migrateOldAssets();
		
		log("Comparing assets...");
		long start = System.nanoTime();
		
		List<AssetObject> result = vm.checkResources(version);
		
		long end = System.nanoTime(), delta = end - start;
		log("Delta time to compare assets: " + delta / 1000000L + " ms.");
		
		return result;
	}
	
	private void deleteEntries() throws IOException {
		List<String> entries = this.version.getUnnecessaryEntries();
		if(entries == null || entries.size() == 0) return;
		log("Removing entries...");
	    
	    File file = version.getJARFile(gameDir);
	    FileUtil.removeFromZip(file, entries);
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
	
	private File getAssetObject(String name) throws IOException {
		File assetsDir = new File(gamedir, "assets");
		File indexDir = new File(assetsDir, "indexes");
		File objectsDir = new File(assetsDir, "objects");
		String assetVersion = this.version.getAssets() == null ? "legacy" : this.version.getAssets();
		File indexFile = new File(indexDir, assetVersion + ".json");
		AssetIndex index = (AssetIndex) this.gson.fromJson(FileUtil.readFile(indexFile), AssetIndex.class);
			    
		String hash = ((AssetIndex.AssetObject)index.getFileMap().get(name)).getHash();
		return new File(objectsDir, hash.substring(0, 2) + "/" + hash);
	}
			  
	private File reconstructAssets() throws IOException {
		File assetsDir = new File(gameDir, "assets");
		File indexDir = new File(assetsDir, "indexes");
		File objectDir = new File(assetsDir, "objects");
		String assetVersion = this.version.getAssets() == null ? "legacy" : this.version.getAssets();
		File indexFile = new File(indexDir, assetVersion + ".json");
		File virtualRoot = new File(new File(assetsDir, "virtual"), assetVersion);
		if (!indexFile.isFile()) {
			log("No assets index file " + virtualRoot + "; can't reconstruct assets");
			return virtualRoot;
		}
		
		AssetIndex index = this.gson.fromJson(FileUtil.readFile(indexFile), AssetIndex.class);
		if (index.isVirtual())
		{
			log("Reconstructing virtual assets folder at " + virtualRoot);
			for (Map.Entry<String, AssetIndex.AssetObject> entry : index.getFileMap().entrySet()) {
				File target = new File(virtualRoot, entry.getKey());
				File original = new File(new File(objectDir, entry.getValue().getHash().substring(0, 2)), entry.getValue().getHash());
				if (!target.isFile()) {
					FileUtils.copyFile(original, target, false);
				}
			}
			FileUtil.writeFile(new File(virtualRoot, ".lastused"), this.dateAdapter.serializeToString(new Date()));
		}
		return virtualRoot;
	}
	
	private void migrateOldAssets() {
		File sourceDir = new File(gamedir, "assets");
		File objectsDir = new File(sourceDir, "objects");
		
		if (!sourceDir.isDirectory())
			return;
		
		IOFileFilter migratableFilter = FileFilterUtils.notFileFilter(FileFilterUtils.or(new IOFileFilter[] { FileFilterUtils.nameFileFilter("indexes"), FileFilterUtils.nameFileFilter("objects"), FileFilterUtils.nameFileFilter("virtual") }));
		for (File file : new TreeSet<File>(FileUtils.listFiles(sourceDir, TrueFileFilter.TRUE, migratableFilter))) {
			String hash = FileUtil.getDigest(file, "SHA-1", 40);
			File destinationFile = new File(objectsDir, hash.substring(0, 2) + "/" + hash);
			
			if (!destinationFile.exists()) {
				log("Migrated old asset", file, "into", destinationFile);
				try {
					FileUtils.copyFile(file, destinationFile);
				}
				catch (IOException e) {
					log("Couldn't migrate old asset", e);
				}
			}
			FileUtils.deleteQuietly(file);
		}
		File[] assets = sourceDir.listFiles();
		if (assets != null) {
			for (File file : assets) {
				if ((!file.getName().equals("indexes")) && (!file.getName().equals("objects")) && (!file.getName().equals("virtual"))) {
					log("Cleaning up old assets directory",file,"after migration");
					FileUtils.deleteQuietly(file);
				}
			}
		}
	}
	
	private String constructClassPath(CompleteVersion version) throws MinecraftLauncherException {
		log("Constructing Classpath...");
		StringBuilder result = new StringBuilder();
	    Collection<File> classPath = version.getClassPath(os, gameDir);
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
		    map.put("auth_session", "null");
		    map.put("auth_access_token", "null");
		    map.put("user_properties", "{}");

		    map.put("auth_player_name", username);
		    map.put("auth_uuid", new UUID(0L, 0L).toString());
		    map.put("user_type", "legacy");

		    map.put("profile_name", "(Default)");
		    map.put("version_name", version.getId());

		    map.put("game_directory", gameDir.getAbsolutePath());
		    map.put("game_assets", assetsDir.getAbsolutePath());
		    
		    map.put("assets_root", new File(gameDir, "assets").getAbsolutePath());
		    map.put("assets_index_name", version.getAssets() == null ? "legacy" : version.getAssets());

		    for (int i = 0; i < split.length; i++) {
		      split[i] = substitutor.replace(split[i]);
		    }

		    return split;
	}
	
	private String[] getJVMArguments() {
		String jvmargs = version.getJVMArguments();
		return (jvmargs != null)? jvmargs.split(" ") : new String[0];
	}
	
	void onCheck(){ if(listener != null) listener.onMinecraftCheck(); }
	void onPrepare(){ if(listener != null) listener.onMinecraftPrepare(); }
	void onLaunch(){ if(listener != null) listener.onMinecraftLaunch(); }
	void onStop(){ log("Launcher stopped."); if(listener != null) listener.onMinecraftLaunchStop(); }
	
	void showWarning(String message, String langpath, Object replace){ log("[WARNING] " + message); if(listener != null) listener.onMinecraftWarning(langpath, replace); }
	void showWarning(String message, String langpath){ this.showWarning(message, langpath, null); }
	
	void onError(MinecraftLauncherException e){ logerror(e); if(listener != null) listener.onMinecraftError(e); }
	void onError(Throwable e){ logerror(e); if(listener != null) listener.onMinecraftError(e); }
	private void log(Object... w){ if(con != null) con.log(prefix, w); U.log(prefix, w); }
	private void logerror(Throwable e){ e.printStackTrace(); if(con == null) return; con.log(prefix, "Error occurred. Logger won't vanish automatically."); con.log(e); }

	public void onJavaProcessEnded(JavaProcess jp) {
		int exit = jp.getExitCode();
		
		if(listener != null)
			listener.onMinecraftClose();
		
		log("Minecraft closed with exit code: "+exit);		
		if(!CrashDescriptor.parseExit(exit)) {
			if(!handleCrash(exit)) if(con != null) con.killIn(5000);
		} else if(con != null) con.killIn(5000);
		
		U.gc();
	}
	
	private boolean handleCrash(int exit){
		if(con == null) return false;
		
		CrashDescriptor descriptor = new CrashDescriptor(this);
		Crash crash = descriptor.scan(exit);
		
		if(crash == null) return false;
		
		if(crash.getFile() != null)
			log("Crash report found.");
		
		if(!crash.getSignatures().isEmpty())
			log("Crash has been recognized.");
		
		log("Console won't vanish automatically.");	
		con.show();
		
		if(listener == null) return true;
		listener.onMinecraftCrash(crash);
		return true;
	}

	public void onJavaProcessError(JavaProcess jp, Throwable e) {
		e.printStackTrace();		
		if(con != null) con.log("Error has occurred:", e);
		
		if(listener != null)
			listener.onMinecraftError(e);
	}

	public void onJavaProcessLog(JavaProcess jp, String line) {
		U.plog(">", line);
		
		if(con != null) con.log(line);
	}
	
	public Console getConsole(){
		return con;
	}
}
