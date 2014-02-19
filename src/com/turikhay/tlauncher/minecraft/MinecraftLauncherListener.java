package com.turikhay.tlauncher.minecraft;


public interface MinecraftLauncherListener {
	public void onMinecraftCheck();
	public void onMinecraftPrepare();
	public void onMinecraftLaunch();
	public void onMinecraftLaunchStop();
	public void onMinecraftClose();
	public void onMinecraftKnownError(MinecraftLauncherException knownError);
	public void onMinecraftError(Throwable unknownError);
	public void onMinecraftWarning(String langpath, Object replace);
	public void onMinecraftCrash(Crash crash);
}
