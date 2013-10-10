package wrapper;

import java.io.File;

public class Wrapper {
   public static boolean isAvailable() {
      String wrapped = System.getProperty("launch4j.wrapper");
      return wrapped != null && wrapped.equals("true");
   }

   public static File getExecutable() {
      String exe = System.getProperty("launch4j.exefile");
      return new File(exe);
   }
}
