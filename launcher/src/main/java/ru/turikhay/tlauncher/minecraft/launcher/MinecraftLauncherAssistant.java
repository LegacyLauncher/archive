package ru.turikhay.tlauncher.minecraft.launcher;

import ru.turikhay.tlauncher.downloader.Downloader;

public abstract class MinecraftLauncherAssistant {
    private final MinecraftLauncher launcher;

    MinecraftLauncherAssistant(MinecraftLauncher launcher) {
        if (launcher == null) {
            throw new NullPointerException();
        } else {
            this.launcher = launcher;
        }
    }

    public MinecraftLauncher getLauncher() {
        return launcher;
    }

    protected abstract void collectInfo();

    protected abstract void collectResources(Downloader var1);

    protected abstract void constructJavaArguments();

    protected abstract void constructProgramArguments();
}
