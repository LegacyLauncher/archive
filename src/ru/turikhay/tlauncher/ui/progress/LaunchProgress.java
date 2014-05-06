package ru.turikhay.tlauncher.ui.progress;

import java.awt.Component;

import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftExtendedListener;

public class LaunchProgress extends DownloaderProgress implements
		MinecraftExtendedListener {
	private static final long serialVersionUID = -1003141285749311799L;

	public LaunchProgress(Component parentComp) {
		super(parentComp);
	}

	@Override
	public void clearProgress() {
		this.setIndeterminate(false);
		this.setValue(0);

		this.setCenterString(null);
		this.setEastString(null);
	}

	private void setupBar() {
		startProgress();
		setIndeterminate(true);
	}

	@Override
	public void onMinecraftPrepare() {
		setupBar();
	}

	@Override
	public void onMinecraftCollecting() {
		setWestString("launcher.step.collecting");
	}

	@Override
	public void onMinecraftComparingAssets() {
		setWestString("launcher.step.comparing-assets");
	}

	@Override
	public void onMinecraftDownloading() {
		setWestString("launcher.step.downloading");
	}

	@Override
	public void onMinecraftReconstructingAssets() {
		setupBar();
		setWestString("launcher.step.reconstructing-assets");
	}

	@Override
	public void onMinecraftUnpackingNatives() {
		setWestString("launcher.step.unpacking-natives");
	}

	@Override
	public void onMinecraftDeletingEntries() {
		setWestString("launcher.step.deleting-entries");
	}

	@Override
	public void onMinecraftConstructing() {
		setWestString("launcher.step.constructing");
	}

	@Override
	public void onMinecraftPostLaunch() {
		setStrings(null, null, null);
	}

	@Override
	public void onMinecraftAbort() {
		stopProgress();
	}

	@Override
	public void onMinecraftLaunch() {
		stopProgress();
	}

	@Override
	public void onMinecraftClose() {
	}

	@Override
	public void onMinecraftError(Throwable e) {
		stopProgress();
	}

	@Override
	public void onMinecraftKnownError(MinecraftException e) {
		stopProgress();
	}

	@Override
	public void onMinecraftCrash(Crash crash) {
		stopProgress();
	}

}
