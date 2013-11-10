package com.turikhay.tlauncher.updater;

import jsmooth.Wrapper;

public enum PackageType {
   EXE,
   JAR,
   AOT;

   public String toLowerCase() {
      return this.name().toLowerCase();
   }

   public static PackageType getCurrent() {
      if (Wrapper.isWrapped()) {
         return EXE;
      } else {
         return Wrapper.isAOT() ? AOT : JAR;
      }
   }

   public static boolean isCurrent(PackageType pt) {
      return getCurrent() == pt;
   }
}
