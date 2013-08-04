package jsmooth;

import java.io.File;

public class Wrapper {
   public static boolean isAvailable() {
      return Native.isAvailable();
   }

   public static File getExecutable() {
      return new File(Native.getExecutablePath() + Native.getExecutableName());
   }
}
