package com.turikhay.tlauncher.minecraft;

public interface MinecraftLauncherListener {
	public void onMinecraftCheck();
	public void onMinecraftPrepare();
	public void onMinecraftLaunch();
	public void onMinecraftClose();
	public void onMinecraftError(MinecraftLauncherException knownError);
	public void onMinecraftError(Throwable unknownError);
	public void onMinecraftWarning(String langpath, String replace);
}
