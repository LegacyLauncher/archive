package net.minecraft.launcher.process;

public interface JavaProcessListener {
    void onJavaProcessPrint(JavaProcess process, PrintStreamType streamType, String line);

    void onJavaProcessEnded(JavaProcess var1);

    void onJavaProcessError(JavaProcess var1, Throwable var2);
}
