package ru.turikhay.tlauncher.ui.listener;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.util.U;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UIListeners implements LocalizableComponent {
    private final MinecraftUIListener minecraftUIListener;
    private final List<MinecraftListener> minecraftListeners = new ArrayList<MinecraftListener>();
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
        this.minecraftListeners.add(U.requireNotNull(listener, "listener"));
    }

    public VersionManagerUIListener getVersionManagerUIListener() {
        return versionManagerUIListener;
    }

    @Override
    public void updateLocale() {
        for(MinecraftListener l : minecraftListeners) {
            if(l instanceof LocalizableComponent) {
                ((LocalizableComponent) l).updateLocale();
            }
        }
    }
}
