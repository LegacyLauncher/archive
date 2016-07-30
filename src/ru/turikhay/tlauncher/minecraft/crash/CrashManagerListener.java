package ru.turikhay.tlauncher.minecraft.crash;

public interface CrashManagerListener {
   void onCrashManagerProcessing(CrashManager var1);

   void onCrashManagerComplete(CrashManager var1, Crash var2);

   void onCrashManagerCancelled(CrashManager var1);

   void onCrashManagerFailed(CrashManager var1, Exception var2);
}
