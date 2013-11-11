package jsmooth;

import com.turikhay.tlauncher.util.FileUtil;

public class Wrapper {
   public static boolean isWrapped() {
      return FileUtil.getRunningJar().toString().endsWith(".exe");
   }
}
