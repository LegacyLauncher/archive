package net.minecraft.launcher.process;

public interface JavaProcessListener {
    void onJavaProcessLog(JavaProcess var1, String var2);

    void onJavaProcessEnded(JavaProcess var1);

    void onJavaProcessError(JavaProcess var1, Throwable var2);
}
