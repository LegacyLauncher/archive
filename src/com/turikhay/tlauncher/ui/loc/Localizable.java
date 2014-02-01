package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.settings.Settings;

public class Localizable {
   private static Settings lang;

   public static void setLang(Settings l) {
      lang = l;
   }

   public static Settings get() {
      return lang;
   }

   public static boolean exists() {
      return lang != null;
   }

   public static String get(String path) {
      return lang.get(path);
   }
}
