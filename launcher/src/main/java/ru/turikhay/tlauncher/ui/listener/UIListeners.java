package ru.turikhay.tlauncher.ui.listener;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftExtendedListener;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.minecraft.launcher.SwingMinecraftExtendedListener;
import ru.turikhay.tlauncher.minecraft.launcher.SwingMinecraftListener;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UIListeners implements LocalizableComponent {
    private final MinecraftUIListener minecraftUIListener;
    private final List<MinecraftListener> minecraftListeners = new ArrayList<>();
    private final VersionManagerUIListener versionManagerUIListener;

    public UIListeners(TLauncher tlauncher) {
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
