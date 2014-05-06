package ru.turikhay.tlauncher.updater;

import ru.turikhay.util.FileUtil;

public enum PackageType {
	EXE, JAR;
	public String toLowerCase() {
		return this.name().toLowerCase();
	}

	public static PackageType getCurrent() {
		if (isWrapped())
			return EXE;
		return JAR;
	}

	public static boolean isCurrent(PackageType pt) {
		return getCurrent() == pt;
	}

	private static boolean isWrapped() {
		return FileUtil.getRunningJar().toString().endsWith(".exe");
	}
}
