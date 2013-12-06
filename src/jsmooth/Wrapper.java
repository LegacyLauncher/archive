package jsmooth;

import com.turikhay.util.FileUtil;

public class Wrapper {
   public static boolean isWrapped() {
      return FileUtil.getRunningJar().toString().endsWith(".exe");
   }
}
