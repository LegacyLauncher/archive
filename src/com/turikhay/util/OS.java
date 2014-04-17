package com.turikhay.util;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;

import com.turikhay.tlauncher.ui.alert.Alert;

/**
 * <code>OS</code> enum-class is used for getting OS-dependant information. <br/>
 * Based on <code>OperatingSystem</code> enum-class in official Minecraft launcher.
 * @author turikhay
 */
public enum OS {
	LINUX("linux", "unix"),
	WINDOWS("win"),
	OSX("mac"),
	SOLARIS("solaris", "sunos"),
	UNKNOWN("unknown");
	
	private final String name;
	private final String[] aliases;
	
	OS(String... aliases) {
		if(aliases == null)
			throw new NullPointerException();
		
		this.name = toString().toLowerCase();
		this.aliases = aliases;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isUnsupported() {
		return this == UNKNOWN;
	}
	
	public boolean isCurrent() {
		return this == CURRENT;
	}
	
	public static OS getCurrent() {
		String osName = System.getProperty("os.name").toLowerCase();

		for(OS os : values())
			for(String alias : os.aliases)
				if(osName.contains(alias))
					return os;

		return UNKNOWN;
	}
	
	public static Arch getArch() {
		return Arch.getCurrent();
	}
	
	public static String getJavaPath(boolean appendBinFolder) {
		char separator = File.separatorChar;
		String path = System.getProperty("java.home") + separator;
		
		if(appendBinFolder) {
			path += "bin" + separator + "java";
			
			if(CURRENT == WINDOWS)
				path += "w.exe"; // javaw.exe
		}
		
		return path;
	}
	
	public static String getJavaPath() {
		return getJavaPath(true);
	}
	
	public static String getSummary() {
		return System.getProperty("os.name") + " " + System.getProperty("os.version") + ", " + "Java " + System.getProperty("java.version");
	}
	
	private static void rawOpenLink(URI uri) throws Throwable {
		Desktop.getDesktop().browse(uri);
	}
	
	public static boolean openLink(URI uri, boolean alertError) {
		log("Trying to open link with default browser:", uri);
		
		try { Desktop.getDesktop().browse(uri); }
		catch(Throwable e) {
			log("Failed to open link with default browser:", uri, e);
			
			if(alertError)
				Alert.showLocError("ui.error.openlink", uri);
			
			return false;
		}
		return true;
	}
	
	public static boolean openLink(URI uri) {
		return openLink(uri, true);
	}
	
	public static boolean openLink(URL url, boolean alertError) {
		log("Trying to open URL with default browser:", url);
		
		URI uri = null;
		
		try { uri = url.toURI(); }
		catch(Exception e){}
		
		return openLink(uri, alertError);
	}
	
	public static boolean openLink(URL url) {
		return openLink(url, true);
	}
	
	private static void openPath(File path, boolean appendSeparator) throws Throwable {
		String absPath = path.getAbsolutePath() + File.separatorChar;
		Runtime r = Runtime.getRuntime();
		Throwable t = null;
		
		switch(CURRENT) {
		case LINUX:
		case OSX:
			String[] cmdArr = { "/usr/bin/open", absPath };
			
			try { r.exec(cmdArr); }
			catch(Throwable e) {
				t = e;
				
				log("Cannot open folder using:\n", cmdArr, e);
				break;
			}
			return;
		case WINDOWS:
			String cmd =
				String.format("cmd.exe /C start \"Open path\" \"%s\"", absPath);
			
			try { r.exec(cmd); }
			catch(Throwable e) {
				t = e;
				
				log("Cannot open folder using CMD.exe:\n", cmd, e);
				break;
			}
			return;
		default:
			log("Unknown system: will use desktop");
			break;
		}
		
		try { rawOpenLink(path.toURI()); }
		catch(Throwable e) {
			t = e;
		}
		
		if(t == null) return; // Path opened successfully.
		throw t;
	}
	
	public static boolean openFolder(File folder, boolean alertError) {
		log("Trying to open folder:", folder);
		
		if(!folder.isDirectory()) {
			log("This path is not a directory, sorry.");
			return false;
		}
		
		try { openPath(folder, true); }catch(Throwable e) {
			log("Failed to open folder:", e);
			
			if(alertError)
				Alert.showLocError("ui.error.openfolder", folder);
			
			return false;
		}
		
		return true;
	}
	
	public static boolean openFolder(File folder) {
		return openFolder(folder, true);
	}
	
	public static boolean openFile(File file, boolean alertError) {
		log("Trying to open file:", file);
		
		if(!file.isFile()) {
			log("This path is not a file, sorry.");
			return false;
		}
		
		try { openPath(file, false); }catch(Throwable e) {
			log("Failed to open file:", e);
			
			if(alertError)
				Alert.showLocError("ui.error.openfolder", file);
			
			return false;
		}
		
		return true;
	}
	
	public static boolean openFile(File file) {
		return openFile(file, true);
	}
	
	public enum Arch {
		x32, x64, UNKNOWN;
		
		public static final Arch CURRENT = getCurrent();
		
		private final String asString;
		private final byte asByte;
		
		Arch() {
			this.asString = toString().substring(1);
			
			byte asByte_temp = 0; 
			
			try{ asByte_temp = Byte.parseByte(asString); }
			catch(RuntimeException e){}
			
			this.asByte = asByte_temp;
		}
		
		public String asString() {
			return asString;
		}
		
		public byte asByte() {
			return asByte;
		}
		
		public boolean isCurrent() {
			return this == CURRENT;
		}
		
		public static Arch getCurrent() {
			String curArch = System.getProperty("sun.arch.data.model");
			
			for(Arch arch : values())
				if(arch.asString.equals(curArch))
					return arch;
			
			return UNKNOWN;
		}
		
		public static int getRecommendedMemory() {
			return CURRENT == x32 || CURRENT == UNKNOWN? 512 : 1024;
		}
	}
	
	public static final OS CURRENT = getCurrent();
	public static final Arch ARCH = Arch.getCurrent();
	
	protected static void log(Object...o) { U.log("[OS]", o); }
}
