package ru.turikhay.util;

import java.awt.Desktop;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;

import com.sun.management.OperatingSystemMXBean;

import ru.turikhay.tlauncher.ui.alert.Alert;

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

	public static final String
	NAME = System.getProperty("os.name"),
	VERSION = System.getProperty("os.version");

	public static final double JAVA_VERSION = getJavaVersion();
	public static final OS CURRENT = getCurrent();

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

	private static OS getCurrent() {
		String osName = NAME.toLowerCase();

		for(OS os : values())
			for(String alias : os.aliases)
				if(osName.contains(alias))
					return os;

		return UNKNOWN;
	}

	private static double getJavaVersion() {
		String version = System.getProperty("java.version");
		int pos, count = 0;

		for (pos = 0; pos < version.length() && count < 2; pos++) {
			if (version.charAt(pos) == '.') {
				count++;
			}
		}

		--pos; //EVALUATE double

		String doubleVersion = version.substring(0, pos);
		return Double.parseDouble(doubleVersion);
	}

	public static boolean is(OS...any) {
		if(any == null)
			throw new NullPointerException();

		if(any.length == 0)
			return false;

		for(OS compare : values())
			for(OS current : any)
				if(current.equals(compare))
					return true;

		return false;
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
		return NAME +" "+ VERSION +" "+ Arch.CURRENT +", Java "+ System.getProperty("java.version") +", "+ Arch.TOTAL_RAM_MB +" MB RAM";
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
		case OSX:
			String[] cmdArr = { "/usr/bin/open", absPath };

			try {
				r.exec(cmdArr);
				return;
			} catch(Throwable e) {
				t = e;

				log("Cannot open folder using:\n", cmdArr, e);
				break;
			}
		case WINDOWS:
			String cmd =
			String.format("cmd.exe /C start \"Open path\" \"%s\"", absPath);

			try {
				r.exec(cmd);
				return;
			} catch(Throwable e) {
				t = e;

				log("Cannot open folder using CMD.exe:\n", cmd, e);
				break;
			}
		default:
			log("... will use desktop");
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

		public static final long TOTAL_RAM = getTotalRam();
		public static final long TOTAL_RAM_MB = TOTAL_RAM / 1024 / 1024;

		public static final int
		MIN_MEMORY = 512,
		PREFERRED_MEMORY = getPreferredMemory(),
		MAX_MEMORY = getMaximumMemory();

		private final String asString;
		private final int asInt;

		Arch() {
			this.asString = toString().substring(1);

			int asInt_temp = 0; 
			try{ asInt_temp = Integer.parseInt(asString); }
			catch(RuntimeException e){}

			this.asInt = asInt_temp;
		}

		public String asString() {
			return this == UNKNOWN? toString() : asString;
		}

		public int asInteger() {
			return asInt;
		}

		public boolean isCurrent() {
			return this == CURRENT;
		}

		private static Arch getCurrent() {
			String curArch = System.getProperty("sun.arch.data.model");

			for(Arch arch : values())
				if(arch.asString.equals(curArch))
					return arch;

			return UNKNOWN;
		}

		private static long getTotalRam() {
			try {
				return	((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
						.getTotalPhysicalMemorySize();
			}catch(Throwable e) {
				U.log("[ERROR] Cannot allocate total physical memory size!", e);
				return 0;
			}
		}

		private static int getPreferredMemory() {
			switch(CURRENT) {
			case x32:

				if(TOTAL_RAM_MB > 4000)
					return 1024;

				return 512;
			case x64:
				return 1024;
			default:
				break;
			}
			return MIN_MEMORY;
		}

		private static int getMaximumMemory() {
			switch(CURRENT) {
			case x32:

				if(TOTAL_RAM_MB > 4000)
					return 1536; // experimental value

				return 1024;
			case x64:

				if(TOTAL_RAM_MB > 6000)
					return 2048;

				if(TOTAL_RAM_MB > 3000)
					return 1536;

				return 1024;

			default:
				break;
			}

			return MIN_MEMORY;
		}
	}

	protected static void log(Object...o) { U.log("[OS]", o); }
}
