package ru.turikhay.tlauncher.updater;

import ru.turikhay.util.FileUtil;

public enum PackageType {
   EXE,
   JAR;

   public static final PackageType CURRENT = FileUtil.getRunningJar().toString().endsWith(".exe") ? EXE : JAR;
}
