package net.minecraft.launcher;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;

public enum OperatingSystem {
	LINUX("linux", new String[] { "linux", "unix" }), WINDOWS("windows",
			new String[] { "win" }), OSX("osx", new String[] { "mac" }), SOLARIS(
			"solaris", new String[] { "solaris", "sunos" }), UNKNOWN("unknown",
			new String[0]);

	private final String name;
	private final String[] aliases;
	private final String arch = System.getProperty("sun.arch.data.model");

	private OperatingSystem(String name, String[] aliases) {
		this.name = name;
		this.aliases = (aliases == null ? new String[0] : aliases);
	}

	public String getName() {
		return this.name;
	}

	String[] getAliases() {
		return this.aliases;
	}

	public boolean isSupported() {
		return this != UNKNOWN;
	}

	String getJavaDir(boolean appendFile) {
		String separator = System.getProperty("file.separator");
		String path = System.getProperty("java.home") + separator;

		if (appendFile) {
			path += "bin" + separator;

			if (getCurrentPlatform() == WINDOWS)
				return path + "javaw.exe";
			else
				return path + "java";
		}

		return path;
	}

	public String getJavaDir() {
		return getJavaDir(true);
	}

	public boolean doesJavaExist() {
		return (this == OperatingSystem.WINDOWS) ? FileUtil
				.fileExists(getJavaDir()) : FileUtil.folderExists(getJavaDir());
	}

	public static OperatingSystem getCurrentPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();

		for (OperatingSystem os : values()) {
			for (String alias : os.getAliases()) {
				if (osName.contains(alias))
					return os;
			}
		}

		return UNKNOWN;
	}

	public boolean is32Bit() {
		return arch.equals("32");
	}

	public boolean is64Bit() {
		return arch.equals("64");
	}

	public String getArch() {
		return arch;
	}

	private static void rawOpenLink(URI uri) throws Throwable {
		Desktop desktop = Desktop.getDesktop();
		desktop.browse(uri);
	}

	private static boolean openLink(URI uri, boolean showError) {
		try {
			rawOpenLink(uri);
		} catch (Throwable e) {
			U.log("Cannot browser link:", uri);

			if (showError)
				Alert.showError(Localizable.get("ui.error.openlink.title"),
						Localizable.get("ui.error.openlink", uri), e);

			return false;
		}

		return true;
	}

	public static boolean openLink(URI uri) {
		return openLink(uri, true);
	}

	private static void rawOpenFile(File file) throws Throwable {
		Desktop desktop = Desktop.getDesktop();
		desktop.open(file);
	}

	private static boolean openFile(File file, boolean showError) {
		try {
			rawOpenFile(file);
		} catch (Throwable e) {
			U.log("Cannot open file:", file);

			if (showError)
				Alert.showError(Localizable.get("ui.error.openfile.title"),
						Localizable.get("ui.error.openfile", file), e);

			return false;
		}

		return true;
	}

	public static boolean openFile(File file) {
		return openFile(file, true);
	}

	public int getRecommendedMemory() {
		return is32Bit() ? 512 : 1024;
	}

	public static String getCurrentInfo() {
		return System.getProperty("os.name") + " "
				+ System.getProperty("os.version") + ", " + "Java "
				+ System.getProperty("java.version");
	}
}