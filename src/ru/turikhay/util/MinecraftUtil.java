package ru.turikhay.util;

import java.io.File;
import java.io.IOException;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;

public class MinecraftUtil {
	public static File getWorkingDirectory() {
		if (TLauncher.getInstance() == null)
			return getDefaultWorkingDirectory();

		Configuration settings = TLauncher.getInstance().getSettings();
		String sdir = settings.get("minecraft.gamedir");

		if (sdir == null)
			return getDefaultWorkingDirectory();

		File dir = new File(sdir);

		try {
			FileUtil.createFolder(dir);
		} catch (IOException e) {
			U.log("Cannot create specified Minecraft folder:",
					dir.getAbsolutePath());
			return getDefaultWorkingDirectory();
		}

		return dir;
	}

	public static File getSystemRelatedFile(String path) {
		String userHome = System.getProperty("user.home", ".");
		File file;

		switch (OS.CURRENT) {
		case LINUX:
		case SOLARIS:
			file = new File(userHome, path);
			break;
		case WINDOWS:
			String applicationData = System.getenv("APPDATA");
			String folder = applicationData != null ? applicationData
					: userHome;

			file = new File(folder, path);
			break;
		case OSX:
			file = new File(userHome, "Library/Application Support/" + path);
			break;
		default:
			file = new File(userHome, path);
		}
		return file;
	}

	public static File getDefaultWorkingDirectory() {
		OS os = OS.CURRENT;
		String path = "." + TLauncher.getFolder();

		if (os == OS.OSX || os == OS.UNKNOWN)
			path = TLauncher.getFolder();

		return getSystemRelatedFile(path + File.separator);
	}

	public static File getOptionsFile() {
		return getFile("options.txt");
	}

	private static File getFile(String name) {
		return new File(getWorkingDirectory(), name);
	}
}
