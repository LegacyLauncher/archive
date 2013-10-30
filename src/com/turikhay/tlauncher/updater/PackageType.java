package com.turikhay.tlauncher.updater;

import jsmooth.Wrapper;

public enum PackageType {
   EXE,
   JAR;

   public String toLowerCase() {
      return this.name().toLowerCase();
   }

   public static PackageType getCurrent() {
      return Wrapper.isAvailable() ? EXE : JAR;
   }

   public static boolean isCurrent(PackageType pt) {
      return getCurrent() == pt;
   }
}
