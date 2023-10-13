package net.legacylauncher.ui.listener;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.minecraft.launcher.MinecraftExtendedListener;
import net.legacylauncher.minecraft.launcher.MinecraftListener;
import net.legacylauncher.minecraft.launcher.SwingMinecraftExtendedListener;
import net.legacylauncher.minecraft.launcher.SwingMinecraftListener;
import net.legacylauncher.ui.loc.LocalizableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UIListeners implements LocalizableComponent {
    private final MinecraftUIListener minecraftUIListener;
    private final List<MinecraftListener> minecraftListeners = new ArrayList<>();
    private final VersionManagerUIListener versionManagerUIListener;

    public UIListeners(LegacyLauncher tlauncher) {
        this.minecraftListeners.add(minecraftUIListener = new MinecraftUIListener(tlauncher));
        this.versionManagerUIListener = new VersionManagerUIListener(tlauncher);
    }

    public MinecraftUIListener getMinecraftUIListener() {
        return minecraftUIListener;
    }

    public List<MinecraftListener> getMinecraftListeners() {
        return Collections.unmodifiableList(minecraftListeners);
    }

    public void registerMinecraftLauncherListener(MinecraftListener listener) {
        this.minecraftListeners.add(new SwingMinecraftListener(listener));
    }

    public void registerMinecraftLauncherListener(MinecraftExtendedListener listener) {
        this.minecraftListeners.add(new SwingMinecraftExtendedListener(listener));
    }

    public VersionManagerUIListener getVersionManagerUIListener() {
        return versionManagerUIListener;
    }

    @Override
    public void updateLocale() {
        for (MinecraftListener l : minecraftListeners) {
            if (l instanceof LocalizableComponent) {
                ((LocalizableComponent) l).updateLocale();
            }
        }
    }
}
