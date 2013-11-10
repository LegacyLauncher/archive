package jsmooth;

import java.io.File;

public class Wrapper {
   public static boolean isWrapped() {
      return getProp("launch4j.wrapper");
   }

   public static boolean isAOT() {
      return getProp("excelsiorjet.aot");
   }

   private static boolean getProp(String key) {
      String prop = System.getProperty(key);
      return prop != null && prop.equals("true");
   }

   public static File getWrapperExecutable() {
      String exe = System.getProperty("launch4j.exefile");
      return new File(exe);
   }
}
