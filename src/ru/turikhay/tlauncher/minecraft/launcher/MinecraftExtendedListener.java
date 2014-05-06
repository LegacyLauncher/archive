package ru.turikhay.tlauncher.minecraft.launcher;

public interface MinecraftExtendedListener extends MinecraftListener {
	void onMinecraftCollecting();

	void onMinecraftComparingAssets();

	void onMinecraftDownloading();

	void onMinecraftReconstructingAssets();

	void onMinecraftUnpackingNatives();

	void onMinecraftDeletingEntries();

	void onMinecraftConstructing();

	void onMinecraftLaunch();

	void onMinecraftPostLaunch();
}
