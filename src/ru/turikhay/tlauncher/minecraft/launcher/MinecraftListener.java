package ru.turikhay.tlauncher.minecraft.launcher;

import ru.turikhay.tlauncher.minecraft.crash.Crash;

public interface MinecraftListener {
	void onMinecraftPrepare();

	void onMinecraftAbort();

	void onMinecraftLaunch();

	void onMinecraftClose();

	void onMinecraftError(Throwable e);

	void onMinecraftKnownError(MinecraftException e);

	void onMinecraftCrash(Crash crash);
}
