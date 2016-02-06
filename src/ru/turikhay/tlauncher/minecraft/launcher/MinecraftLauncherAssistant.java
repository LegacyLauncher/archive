package ru.turikhay.tlauncher.minecraft.launcher;

import ru.turikhay.tlauncher.downloader.Downloader;

public abstract class MinecraftLauncherAssistant {
   protected abstract void collectInfo() throws MinecraftException;

   protected abstract void collectResources(Downloader var1) throws MinecraftException;

   protected abstract void constructJavaArguments() throws MinecraftException;

   protected abstract void constructProgramArguments() throws MinecraftException;
}
