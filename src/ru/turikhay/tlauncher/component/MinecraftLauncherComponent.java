package ru.turikhay.tlauncher.component;

import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncherAssistant;

public interface MinecraftLauncherComponent {
	public MinecraftLauncherAssistant getAssistant(MinecraftLauncher launcher);
}
