package net.legacylauncher.ui.progress;

import net.legacylauncher.minecraft.crash.CrashManager;
import net.legacylauncher.minecraft.launcher.MinecraftException;
import net.legacylauncher.minecraft.launcher.MinecraftExtendedListener;
import net.legacylauncher.ui.login.LoginException;
import net.legacylauncher.ui.login.LoginForm;

import java.awt.*;

public class LaunchProgress extends DownloaderProgress implements MinecraftExtendedListener, LoginForm.LoginProcessListener {
    private static final long serialVersionUID = -1003141285749311799L;

    public LaunchProgress(Component parentComp) {
        super(parentComp);
    }

    public void clearProgress() {
        setIndeterminate(false);
        setValue(0);
        setCenterString(null);
        setEastString(null);
    }

    private void setupBar() {
        startProgress();
        setIndeterminate(true);
    }

    public void onMinecraftPrepare() {
    }

    public void onMinecraftCollecting() {
        setWestString("launcher.step.collecting");
    }

    public void onMinecraftComparingAssets(boolean fastCompare) {
        setWestString("launcher.step.comparing-assets" + (fastCompare ? "" : "-long"));
    }

    @Override
    public void onMinecraftCheckingJre() {
        setWestString("launcher.step.checking-jre");
    }

    @Override
    public void onMinecraftMalwareScanning() {
        setWestString("launcher.step.jarscanner");
    }

    public void onMinecraftDownloading() {
        setWestString("launcher.step.downloading");
    }

    public void onMinecraftReconstructingAssets() {
        setupBar();
        setWestString("launcher.step.reconstructing-assets");
    }

    public void onMinecraftUnpackingNatives() {
        setWestString("launcher.step.unpacking-natives");
    }

    public void onMinecraftDeletingEntries() {
        setWestString("launcher.step.deleting-entries");
    }

    public void onMinecraftConstructing() {
        setWestString("launcher.step.constructing");
    }

    public void onMinecraftPostLaunch() {
        setStrings(null, null, null);
    }

    public void onMinecraftAbort() {
        stopProgress();
    }

    public void onMinecraftLaunch() {
        stopProgress();
    }

    public void onMinecraftClose() {
    }

    public void onMinecraftError(Throwable throwable) {
        stopProgress();
    }

    public void onMinecraftKnownError(MinecraftException exception) {
        stopProgress();
    }

    public void onCrashManagerInit(CrashManager manager) {
    }

    public void loggingIn() throws LoginException {
        setupBar();
        setWestString("launcher.step.logging-in");
    }

    public void loginFailed() {
        stopProgress();
    }

    public void loginSucceed() {
    }
}
