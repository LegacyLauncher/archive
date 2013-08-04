package net.minecraft.launcher_.process;

public interface JavaProcessListener {
   void onJavaProcessEnded(JavaProcess var1);

   void onJavaProcessError(JavaProcess var1, Throwable var2);
}
