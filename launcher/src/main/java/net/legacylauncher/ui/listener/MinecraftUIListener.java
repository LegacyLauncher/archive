package net.legacylauncher.ui.listener;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.configuration.Configuration;
import net.legacylauncher.configuration.LangConfiguration;
import net.legacylauncher.minecraft.crash.Crash;
import net.legacylauncher.minecraft.crash.CrashManager;
import net.legacylauncher.minecraft.crash.CrashManagerListener;
import net.legacylauncher.minecraft.crash.SwingCrashManagerListener;
import net.legacylauncher.minecraft.launcher.MinecraftException;
import net.legacylauncher.minecraft.launcher.MinecraftListener;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.crash.CrashProcessingFrame;
import net.legacylauncher.ui.loc.LocalizableComponent;
import net.legacylauncher.ui.swing.DelayedComponent;
import net.legacylauncher.ui.swing.DelayedComponentLoader;
import net.legacylauncher.util.SwingUtil;

public class MinecraftUIListener implements MinecraftListener, CrashManagerListener, LocalizableComponent {
    private final DelayedComponent<CrashProcessingFrame> crashFrame;

    private final LegacyLauncher t;
    private final LangConfiguration lang;

    public MinecraftUIListener(LegacyLauncher tlauncher) {
        t = tlauncher;
        lang = t.getLang();
        crashFrame = new DelayedComponent<>(new DelayedComponentLoader<CrashProcessingFrame>() {
            @Override
            public CrashProcessingFrame loadComponent() {
                return new CrashProcessingFrame();
            }

            @Override
            public void onComponentLoaded(CrashProcessingFrame loaded) {
                LegacyLauncher.getInstance().getFrame().updateLocales();
            }
        });
    }

    public DelayedComponent<CrashProcessingFrame> getCrashProcessingFrame() {
        return crashFrame;
    }

    public void onMinecraftPrepare() {
    }

    public void onMinecraftAbort() {
    }

    public void onMinecraftLaunch() {
        if (!t.getSettings().getActionOnLaunch().equals(Configuration.ActionOnLaunch.NOTHING)) {
            SwingUtil.later(() -> t.getFrame().setVisible(false));
        }

    }

    public void onMinecraftClose() {
        if (t.getMinecraftLauncher().isLaunchAssist()) {
            SwingUtil.later(() -> {
                t.getFrame().setVisible(true);
                t.getFrame().getNotices().selectRandom();
            });
        }
    }

    public void onMinecraftError(Throwable e) {
        Alert.showLocError("launcher.error.title", "launcher.error.unknown", e);
    }

    public void onMinecraftKnownError(MinecraftException e) {
        Alert.showError(lang.get("launcher.error.title"), lang.get("launcher.error." + e.getLangPath(), (Object[]) e.getLangVars()), e.getCause());
    }

    @Override
    public void onCrashManagerInit(CrashManager manager) {
        manager.addListener(new SwingCrashManagerListener(this));
        manager.addListener(new SwingCrashManagerListener(crashFrame.get()));
    }

    @Override
    public void onCrashManagerProcessing(CrashManager manager) {
    }

    @Override
    public void onCrashManagerComplete(CrashManager manager, Crash crash) {
    }

    @Override
    public void onCrashManagerCancelled(CrashManager manager) {
    }

    @Override
    public void onCrashManagerFailed(CrashManager manager, Exception e) {
        Alert.showLocError("crash.error", LegacyLauncher.getSupportEmail());
    }

    @Override
    public void updateLocale() {
        SwingUtil.later(() -> {
            if (crashFrame.isLoaded()) {
                crashFrame.get().updateLocale();
            }
        });
    }
}
