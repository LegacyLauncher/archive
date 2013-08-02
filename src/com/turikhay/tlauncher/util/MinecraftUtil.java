package com.turikhay.tlauncher.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.TLauncherException;
import com.turikhay.tlauncher.downloader.Downloadable;

import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.updater.VersionFilter;
import net.minecraft.launcher_.versions.ReleaseType;

public class MinecraftUtil {
	private static TLauncher t;
	private static File g_workingDirectory;
	
	static void setWorkingTo(TLauncher to){ if(t == null) t = to; }
	
	public static void setCustomWorkingDirectory(File folder){
		if(!folder.isDirectory()) folder.mkdirs();
		g_workingDirectory = folder;
	}
	
	public static File getWorkingDirectory() {
		if(g_workingDirectory != null) return g_workingDirectory;
		
		String userHome = System.getProperty("user.home", "."), separator = File.separator;
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
		return (g_workingDirectory = workingDirectory);
	}
	
	public static File getOptionsFile() {
		return getFile("options.txt");
	}
	
	public static File getNativeOptionsFile(){
		return getFile("toptions.ini");
	}
	
	public static File getFile(String name){
		return new File(getWorkingDirectory(), name);
	}
	
	public static Downloadable getDownloadable(String url, boolean force){
		URL url_r = null;
		try{ url_r = new URL(url); }catch(Exception e){ e.printStackTrace(); return null; }
		
		return new Downloadable(url_r, getFile( FileUtil.getFilename(url_r) ), force);
	}
	
	public static Downloadable getDownloadable(String url){
		return getDownloadable(url, false);
	}
	
	@SuppressWarnings("resource")
	public static void startLauncher(File launcherJar, Class<?>[] construct, Object[] obj){
		U.log("Starting launcher...");
		try{
			Class<?> aClass = new URLClassLoader(new URL[] { launcherJar.toURI().toURL() }).loadClass("net.minecraft.launcher.Launcher");
			Constructor<?> constructor = aClass.getConstructor(construct);
			constructor.newInstance(obj);
		}catch(Exception e){
			throw new TLauncherException("Cannot start launcher", e);
		}
	}
	
	public static VersionFilter getVersionFilter(){
		VersionFilter r = new VersionFilter();
		boolean
			snaps = t.settings.getBoolean("minecraft.versions.snapshots"),
			beta = t.settings.getBoolean("minecraft.versions.beta"),
			alpha = t.settings.getBoolean("minecraft.versions.alpha");
		
		if(!snaps) r.excludeType(ReleaseType.SNAPSHOT);
		if(!beta) r.excludeType(ReleaseType.OLD_BETA);
		if(!alpha) r.excludeType(ReleaseType.OLD_ALPHA);
		
		return r;		
	}
}
