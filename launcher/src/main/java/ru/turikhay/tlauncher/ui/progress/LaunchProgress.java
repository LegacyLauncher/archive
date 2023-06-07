package ru.turikhay.tlauncher.ui.progress;

import ru.turikhay.tlauncher.minecraft.crash.CrashManager;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftExtendedListener;

import java.awt.*;

public class LaunchProgress extends DownloaderProgress implements MinecraftExtendedListener {
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
        setupBar();
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

    public void onMinecraftError(Throwable e) {
        stopProgress();
    }

    public void onMinecraftKnownError(MinecraftException e) {
        stopProgress();
    }

    public void onCrashManagerInit(CrashManager manager) {
    }
}
