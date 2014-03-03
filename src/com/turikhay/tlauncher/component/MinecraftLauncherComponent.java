package com.turikhay.tlauncher.component;

import com.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftLauncherAssistant;

public interface MinecraftLauncherComponent {
   MinecraftLauncherAssistant getAssistant(MinecraftLauncher var1);
}
