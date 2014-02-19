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

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;
import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.updater.AssetIndex.AssetObject;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ExtractRules;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.json.DateTypeAdapter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.google.gson.Gson;
import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.component.managers.AssetsManager;
import com.turikhay.tlauncher.component.managers.ProfileManager;
import com.turikhay.tlauncher.component.managers.VersionManager;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.Configuration.ActionOnLaunch;
import com.turikhay.tlauncher.configuration.Configuration.ConsoleType;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.ui.console.Console;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;
import com.turikhay.util.logger.LinkedStringStream;
import com.turikhay.util.logger.PrintLogger;

// TODO rewrite
// TODO make empty javapath null
public class MinecraftLauncher extends Thread implements JavaProcessListener {
	public final static int VERSION = 14, TLAUNCHER_VERSION = 6;
	final String prefix = "[MinecraftLauncher]";
	
	final OperatingSystem os = OperatingSystem.getCurrentPlatform();
	final Gson gson = new Gson();
	final DateTypeAdapter dateAdapter = new DateTypeAdapter();
	
	//private TLauncher t;
	Configuration s; 
	Downloader d;
	
	final LinkedStringStream output;
	final PrintLogger logger;
	
	Console c;
	
	final MinecraftLauncherListener listener;
	final VersionManager vm;
	final AssetsManager am;
	final ProfileManager pm;
	
	static boolean instanceWorking;
	
	final boolean exit;
	
	int step = -1;
	boolean init, working;
	final boolean forceupdate, check, console;
	VersionSyncInfo syncInfo;
	CompleteVersion version;
	final String version_name, jargs, margs, gamedir, javadir;
	final int width, height;
	
	String account_name;
	Account account;
	
	DownloadableContainer ver = new DownloadableContainer(), res = new DownloadableContainer();
	List<AssetObject> assetsList;
	private boolean downloadVersion, downloadAssets, downloadFlag;
	
	JavaProcessLauncher processLauncher;
	File nativeDir, gameDir, assetsDir;
	
	private MinecraftLauncher (
			MinecraftLauncherListener listener, VersionManager vm, AssetsManager am, ProfileManager pm,
			String version_name, String account_name, String token, String gamedir, String javadir, String jargs, String margs,
			int[] sizes,
			boolean force, boolean check, boolean exit, boolean console
		){
		Thread.setDefaultUncaughtExceptionHandler(new MinecraftLauncherExceptionHandler(this));
		
		this.listener = listener;
		
		this.vm = vm;
		this.am = am;
		this.pm = pm;
		
		this.version_name = version_name;
		this.forceupdate = force;
		
		this.account_name = account_name;
		
		this.gamedir = gamedir;
		this.javadir = (javadir == null)? os.getJavaDir() : javadir;
		this.jargs = (jargs == null)? "" : jargs;
		this.margs = (margs == null)? "" : margs;
		
		this.console = console;
		this.check = check;
		this.exit = exit;
		
		this.width = sizes[0];
		this.height = sizes[1];
		
		this.output = new LinkedStringStream();
		this.logger = new PrintLogger(output);
	}
	
	public MinecraftLauncher(MinecraftLauncherListener listener, Downloader d, Configuration s, VersionManager vm, AssetsManager am, ProfileManager pm, boolean force, boolean check){
		this(
				listener, vm, am, pm,
				s.get("login.version"), s.get("login.account"), s.get("login.token"), s.get("minecraft.gamedir"), s.get("minecraft.javadir"), s.get("minecraft.javaargs"), s.get("minecraft.args"),
				s.getWindowSize(),
				force, check, s.getActionOnLaunch() == ActionOnLaunch.EXIT, s.getConsoleType() == ConsoleType.MINECRAFT
		);
		
		this.s = s;
		this.d = d;
		
		init();
	}
	
	public MinecraftLauncher(TLauncher t, MinecraftLauncherListener listener, boolean force, boolean check){
		this(listener, t.getDownloader(), t.getSettings(), t.getManager().getVersionManager(), t.getManager().getAssetsManager(), t.getManager().getProfileManager(), force, check);
		
		init();
	}
	
	public MinecraftLauncher(TLauncher t, MinecraftLauncherListener listener, String username, boolean force, boolean check){
		this(listener, t.getDownloader(), t.getSettings(), t.getVersionManager(), t.getManager().getAssetsManager(), t.getProfileManager(), force, check);
		
		this.account_name = username;
		this.account = new Account(username);
		
		init();
	}
	
	public void init(){
		if(init) return; init = true;
		
		if(!exit && s != null && s.getConsoleType() != ConsoleType.GLOBAL)
			c = new Console(s, logger, "Minecraft Logger", console);
		
		log("Minecraft Launcher ["+VERSION+";"+TLAUNCHER_VERSION+"] is started!");
		log("Running under TLauncher "+TLauncher.getVersion()+" "+TLauncher.getBrand());
		log("Current machine:", OperatingSystem.getCurrentInfo());
		log("Launching version:", version_name);
	}
	
	public void run(){
		try{ check(); }catch(MinecraftLauncherException me){ onError(me); }catch(Exception e){ onError(e); }
	}
	
	private void check() throws MinecraftLauncherException {
		if(step > -1) throw new IllegalStateException("MinecraftLauncher is already working!");
		
		if(account == null){
			if(account_name == null || account_name.isEmpty())
				throw new MinecraftLauncherException("Account is NULL!", "account-invalid", account_name);
			
			this.account = pm.getAuthDatabase().getByUsername(account_name);
		}
		
		if(account == null)
			throw new MinecraftLauncherException("Account is not found", "account-not-found", account_name);
		
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
		++this.step;
		this.onCheck();
		
		if(version.getMinimumCustomLauncherVersion() != 0){
			if(version.getMinimumCustomLauncherVersion() > TLAUNCHER_VERSION)
				throw new MinecraftLauncherException("TLauncher is incompatible with this extra version (needed "+version.getMinimumCustomLauncherVersion()+").", "incompatible");
		} else {
			if(!version.appliesToCurrentEnvironment())
				showWarning("Version "+version_name+" is incompatible with your environment.", "incompatible");
			if(version.getMinimumLauncherVersion() > VERSION)
				showWarning("Current launcher version is incompatible with selected version "+version_name+" (version "+version.getMinimumLauncherVersion()+" required).", "incompatible.launcher");
		}
		
		
		assetsList = (check)? this.compareAssets() : null;
		
		downloadAssets = assetsList != null && !assetsList.isEmpty();
		if(forceupdate) downloadVersion = true;
		else downloadVersion = !syncInfo.isInstalled() || !vm.getLocalList().hasAllFiles(version, os);
		
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
	    		am.downloadResources(res, version, assetsList, forceupdate);
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
				
				try {
					vm.getLocalList().saveVersion(version);
				} catch (IOException e) {
					U.log("Cannot save version :(");
				}
				
				if(downloadFlag) prepare();
				else downloadFlag = true;
			}
		});
		ver.setConsole(c);
		
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
		res.setConsole(c);
		
		if(!downloadVersion || !downloadAssets)
			downloadFlag = true;
		
		if(downloadVersion) log("Downloading version "+version_name+"...");
		if(downloadAssets) log("Downloading assets...");
		
		d.add(ver); d.add(res);
		d.startLaunch();
	}
	
	private void prepare_() throws MinecraftLauncherException {
		if(step != 0) throw new IllegalStateException("The game is not checked or is already launching!");
		
		++this.step;
		this.onPrepare();
		
		this.gameDir = new File(gamedir);
		this.nativeDir = new File(gameDir, "versions/" + this.version.getID() + "/" + "natives");
		if (!this.nativeDir.isDirectory()) this.nativeDir.mkdirs();
		
		try {
			this.unpackNatives(forceupdate);
		} catch (IOException e){ throw new MinecraftLauncherException("Cannot unpack natives!", "unpack-natives", e); }
		
		try {
			this.deleteEntries();
		} catch (IOException e){ throw new MinecraftLauncherException("Cannot delete entries!", "delete-entries", e); }
		
		if(TLauncher.getInstance().getManager().getServerListManager() != null)
			try {
				TLauncher.getInstance().getManager().getServerListManager().reconstructList(version_name, new File(gameDir, "servers.dat"));
			}catch(Throwable e){ U.log("Cannot reconstruct server list!", e); }
		
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
	        
	        if(jargs.length() > 0) processLauncher.addSplitCommands(jargs);
	        
	        processLauncher.addCommands(getJVMArguments());
	        
	        processLauncher.addCommand(this.version.getMainClass());
	        
	        log("Half command (characters are not escaped, without Minecraft arguments):");
	   	 	log(processLauncher.getCommandsAsString());
	        
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
		if(step != 1) throw new IllegalStateException("The game is not prepared or is already launched!");
		
		++this.step;
		
		U.gc();
		log("Starting Minecraft "+version_name+"...");
		log("Launching in:", gameDir.getAbsolutePath());
   	 
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
		
		List<AssetObject> result = am.checkResources(version, !forceupdate);
		
		long end = System.nanoTime(), delta = end - start;
		log("Delta time to compare assets: " + delta / 1000000L + " ms.");
		
		return result;
	}
	
	private void deleteEntries() throws IOException {
		List<String> entries = this.version.getRemovableEntries();
		if(entries == null || entries.size() == 0) return;
		log("Removing entries...");
	    
	    File file = version.getFile(gameDir);
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
			    
		String hash = ((AssetObject) index.getFileMap().get(name)).getHash();
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
				
				if(!original.isFile())
					log("Skipped reconstructing:", original);
				else				
					if(forceupdate || !target.isFile())
						FileUtils.copyFile(original, target, false);
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
		
			String assets = version.getAssets();
		    String[] split = version.getMinecraftArguments().split(" ");

		    map.put("auth_username", account_name);
		    
		    if(account != null && account.hasLicense()) {
		    	map.put("auth_session", String.format("token:%s:%s", new Object[] { account.getAccessToken(), account.getProfile().getId() }));
			    map.put("auth_access_token", account.getAccessToken());
			    map.put("user_properties", gson.toJson(account.getProperties()));
			    
			    map.put("auth_player_name", account.getDisplayName());
			    map.put("auth_uuid", account.getUUID());
			    map.put("user_type", "mojang");
			    map.put("profile_name", account.getProfile().getName());
		    } else {
			    map.put("auth_session", "null");
			    map.put("auth_access_token", "null");
			    map.put("user_properties", "[]");
			    
			    map.put("auth_player_name", account_name);
			    map.put("auth_uuid", new UUID(0L, 0L).toString());
			    map.put("user_type", "legacy");
			    map.put("profile_name", "(Default)");
		    }
		    map.put("version_name", version.getID());

		    map.put("game_directory", gameDir.getAbsolutePath());
		    map.put("game_assets", assetsDir.getAbsolutePath());
		    
		    map.put("assets_root", new File(gameDir, "assets").getAbsolutePath());
		    map.put("assets_index_name", assets == null ? "legacy" : assets);

		    for (int i = 0; i < split.length; i++) {
		      split[i] = substitutor.replace(split[i]);
		    }

		    return split;
	}
	
	private String[] getJVMArguments() {
		String jvmargs = version.getJVMArguments();
		return (jvmargs != null)? jvmargs.split(" ") : new String[0];
	}
	
	void onCheck(){
		this.working = true;
		
		if(listener != null) listener.onMinecraftCheck();
	}
	void onPrepare(){ if(listener != null) listener.onMinecraftPrepare(); }
	void onLaunch(){ if(listener != null) listener.onMinecraftLaunch(); }
	void onStop(){
		this.working = false;
		
		log("Launcher stopped.");
		if(listener != null) listener.onMinecraftLaunchStop();
	}
	
	void showWarning(String message, String langpath, Object replace){ log("[WARNING] " + message); if(listener != null) listener.onMinecraftWarning(langpath, replace); }
	void showWarning(String message, String langpath){ this.showWarning(message, langpath, null); }
	
	void onError(Throwable e){
		this.working = false;
		
		logerror(e);
		if(listener == null) return;
		
		if(e instanceof MinecraftLauncherException)
			listener.onMinecraftKnownError((MinecraftLauncherException) e);
		else
			listener.onMinecraftError(e);
	}
	private void log(Object... w){ logger.log(prefix, w); U.log(prefix, w); }
	private void logerror(Throwable e){ e.printStackTrace(); if(c == null) return; c.log(prefix, "Error occurred. Logger won't vanish automatically."); c.log(e); }

	public void onJavaProcessEnded(JavaProcess jp) {
		int exit = jp.getExitCode();
		
		this.working = false;
		
		if(listener != null)
			listener.onMinecraftClose();
		
		log("Minecraft closed with exit code: "+exit);	
		if(!CrashDescriptor.parseExit(exit)) {
			if(!handleCrash(exit)) if(c != null) c.killIn(5000);
		} else if(c != null) c.killIn(5000);
		
		U.gc();
	}
	
	private boolean handleCrash(int exit){
		CrashDescriptor descriptor = new CrashDescriptor(this);
		Crash crash = descriptor.scan(exit);
		
		if(crash == null) return false;
		
		if(crash.getFile() != null)
			log("Crash report found.");
		
		if(!crash.getSignatures().isEmpty())
			log("Crash has been recognized.");
		
		if(c != null){
			log("Console won't vanish automatically.");	
			c.show();
		}
		
		if(listener == null) return true;
		listener.onMinecraftCrash(crash);
		return true;
	}

	public void onJavaProcessError(JavaProcess jp, Throwable e) {
		e.printStackTrace();		
		if(c != null) c.log("Error has occurred:", e);
		
		if(listener != null)
			listener.onMinecraftError(e);
	}

	public void onJavaProcessLog(JavaProcess jp, String line) {
		U.plog(">", line);
		
		logger.log(line);
	}
	
	public Console getConsole(){
		return c;
	}
	
	public PrintLogger getLogger(){
		return logger;
	}
	
	public LinkedStringStream getStream(){
		return output;
	}
	
	public boolean isWorking(){
		return working;
	}
}
