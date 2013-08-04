package jsmooth;

import java.io.File;

public class Native {
   private static boolean s_bound = false;
   public static final int EXITWINDOWS_FORCE = 4;
   public static final int EXITWINDOWS_LOGOFF = 0;
   public static final int EXITWINDOWS_POWEROFF = 8;
   public static final int EXITWINDOWS_REBOOT = 2;
   public static final int EXITWINDOWS_SHUTDOWN = 1;
   public static final String SHELLEXECUTE_OPEN = "open";
   public static final String SHELLEXECUTE_PRINT = "print";
   public static final String SHELLEXECUTE_EXPLORE = "explore";
   public static final String SHELLEXECUTE_FIND = "find";
   public static final String SHELLEXECUTE_EDIT = "edit";
   public static final int SW_HIDE = 0;
   public static final int SW_NORMAL = 1;
   public static final int SW_SHOWNORMAL = 1;
   public static final int SW_SHOWMINIMIZED = 2;
   public static final int SW_MAXIMIZE = 3;
   public static final int SW_SHOWMAXIMIZED = 3;
   public static final int SW_SHOWNOACTIVATE = 4;
   public static final int SW_SHOW = 5;
   public static final int SW_MINIMIZE = 6;
   public static final int SW_SHOWMINNOACTIVE = 7;
   public static final int SW_SHOWNA = 8;
   public static final int SW_RESTORE = 9;
   public static final int SW_SHOWDEFAULT = 10;
   public static final int SW_FORCEMINIMIZE = 11;
   public static final int SW_MAX = 11;

   public static boolean isAvailable() {
      return s_bound;
   }

   public static native String getExecutablePath();

   public static native String getExecutableName();

   public static native boolean deleteFileOnReboot(String var0);

   public static native boolean exitWindows(int var0);

   public static native boolean shellExecute(String var0, String var1, String var2, String var3, int var4);

   public static native DriveInfo getDriveInfo(File var0);
}
