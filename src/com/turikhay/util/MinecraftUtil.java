package com.turikhay.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.downloader.Downloadable;

public class MinecraftUtil {	
	public static File getWorkingDirectory(){
		if(TLauncher.getInstance() == null) return getDefaultWorkingDirectory();
		
		Configuration settings = TLauncher.getInstance().getSettings();
		String sdir = settings.get("minecraft.gamedir");
		
		if(sdir == null) return getDefaultWorkingDirectory();
		
		File dir = new File(sdir);
		
		try{
			FileUtil.createFolder(dir);
		}catch(IOException e){
			U.log("Cannot create specified Minecraft folder:", dir.getAbsolutePath());
			return getDefaultWorkingDirectory();
		}
		
		return dir;
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
		String path = ".minecraft";
		
		if(os == OperatingSystem.OSX || os == OperatingSystem.UNKNOWN)
			path = "minecraft";
		
		return getSystemRelatedFile(path + File.separator);
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
}
