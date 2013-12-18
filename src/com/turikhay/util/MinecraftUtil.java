package com.turikhay.util;

import java.io.File;
import java.net.URL;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.Downloadable;

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.versions.ReleaseType;

public class MinecraftUtil {	
	public static File getWorkingDirectory(){
		if(TLauncher.getInstance() == null) return getDefaultWorkingDirectory();
		
		String dir = TLauncher.getInstance().getSettings().get("minecraft.gamedir");
		if(dir == null) return getDefaultWorkingDirectory();
		
		return new File(dir);
	}
	
	public static File getSystemRelatedFile(String path){
		String
			userHome = System.getProperty("user.home", ".");
		File file;
	
		switch (OperatingSystem.getCurrentPlatform()) {
		case LINUX:
		case SOLARIS:
			file = new File(userHome, path);
			break;
		case WINDOWS:
			String applicationData = System.getenv("APPDATA");
			String folder = applicationData != null ? applicationData : userHome;
		
			file = new File(folder, path);
			break;
		case OSX:
			file = new File(userHome, "Library/Application Support/"+path);
			break;
		default:
			file = new File(userHome, path);
		}
		return file;
	}
	
	public static File getDefaultWorkingDirectory() {
		OperatingSystem os = OperatingSystem.getCurrentPlatform();
		String separator = File.separator, path = ".minecraft";
		if(os == OperatingSystem.OSX || os == OperatingSystem.UNKNOWN)
			path = "minecraft";
		
		return getSystemRelatedFile(path + separator);
		/*String userHome = System.getProperty("user.home", "."), separator = File.separator;
		File workingDirectory;
		
		switch (OperatingSystem.getCurrentPlatform()) {
		case LINUX:
		case SOLARIS:
			workingDirectory = new File(userHome, ".minecraft" + separator);
			break;
		case WINDOWS:
			String applicationData = System.getenv("APPDATA");
			String folder = applicationData != null ? applicationData : userHome;
			
			workingDirectory = new File(folder, ".minecraft" + separator);
			break;
		case OSX:
			workingDirectory = new File(userHome, "Library"+ separator +"Application Support"+ separator +"minecraft");
			break;
		default:
			workingDirectory = new File(userHome, "minecraft" + separator);
		}
		return workingDirectory;*/
	}
	
	public static File getOptionsFile() {
		return getFile("options.txt");
	}
	
	public static File getFile(String name){
		return new File(getWorkingDirectory(), name);
	}
	
	public static Downloadable getDownloadable(String url, boolean force){
		URL url_r = null;
		try{
			url_r = new URL(url);
			return new Downloadable(url, getFile( FileUtil.getFilename(url_r) ), force);
		}catch(Exception e){ e.printStackTrace(); return null; }
	}
	
	public static Downloadable getDownloadable(String url){
		return getDownloadable(url, false);
	}
	
	public static VersionFilter getVersionFilter(){
		TLauncher t = TLauncher.getInstance();
		
		if(t == null)
			throw new IllegalStateException("TLauncher instance is not defined!");
		
		VersionFilter r = new VersionFilter();
		boolean
			snaps = t.getSettings().getBoolean("minecraft.versions.snapshots"),
			beta = t.getSettings().getBoolean("minecraft.versions.beta"),
			alpha = t.getSettings().getBoolean("minecraft.versions.alpha"),
			cheats = t.getSettings().getBoolean("minecraft.versions.cheats");
		
		if(!snaps) r.excludeType(ReleaseType.SNAPSHOT);
		if(!beta) r.excludeType(ReleaseType.OLD_BETA);
		if(!alpha) r.excludeType(ReleaseType.OLD_ALPHA);
		if(!cheats) r.excludeType(ReleaseType.CHEAT);
		
		return r;		
	}
}
