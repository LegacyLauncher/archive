package ru.turikhay.tlauncher.jre;

public interface JavaRuntimeInstallerListener {
    void onJavaInstallerStarted();
    void onJavaInstallerProgress(long current, long max);
    void onJavaInstallerInterrupted();
    void onJavaInstallerFailed(Exception e);
    void onJavaInstallerSucceeded();
}
