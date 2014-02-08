package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.configuration.LangConfiguration;

public class Localizable {
   public static final Object[] EMPTY_VARS = new Object[0];
   private static LangConfiguration lang;

   public static void setLang(LangConfiguration l) {
      lang = l;
   }

   public static LangConfiguration get() {
      return lang;
   }

   public static boolean exists() {
      return lang != null;
   }

   public static String get(String path) {
      return lang.get(path);
   }

   public static String[] checkVariables(Object[] check) {
      if (check == null) {
         throw new NullPointerException();
      } else {
         String[] string = new String[check.length];

         for(int i = 0; i < check.length; ++i) {
            if (check[i] == null) {
               throw new NullPointerException("Variable at index " + i + " is NULL!");
            }

            string[i] = check[i].toString();
         }

         return string;
      }
   }
}
