package net.legacylauncher.minecraft.crash;

public interface CrashManagerListener {
    void onCrashManagerProcessing(CrashManager manager);

    void onCrashManagerComplete(CrashManager manager, Crash crash);

    void onCrashManagerCancelled(CrashManager manager);

    void onCrashManagerFailed(CrashManager manager, Exception e);
}
