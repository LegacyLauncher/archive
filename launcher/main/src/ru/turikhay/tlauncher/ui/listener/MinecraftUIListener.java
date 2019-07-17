package ru.turikhay.tlauncher.ui.listener;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.crash.CrashManager;
import ru.turikhay.tlauncher.minecraft.crash.CrashManagerListener;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.crash.CrashProcessingFrame;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.support.MailSupportFrame;
import ru.turikhay.tlauncher.ui.swing.DelayedComponent;
import ru.turikhay.tlauncher.ui.swing.DelayedComponentLoader;
import ru.turikhay.util.U;

public class MinecraftUIListener implements MinecraftListener, CrashManagerListener, LocalizableComponent {
    private final DelayedComponent<CrashProcessingFrame> crashFrame;

    private final TLauncher t;
    private final LangConfiguration lang;

    public MinecraftUIListener(TLauncher tlauncher) {
        t = tlauncher;
        lang = t.getLang();
        crashFrame = new DelayedComponent<>(new DelayedComponentLoader<CrashProcessingFrame>() {
            @Override
            public CrashProcessingFrame loadComponent() {
                return new CrashProcessingFrame();
            }

            @Override
            public void onComponentLoaded(CrashProcessingFrame loaded) {
                TLauncher.getInstance().getFrame().updateLocales();
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
            t.getFrame().setVisible(false);
        }

    }

    public void onMinecraftClose() {
        if (t.getMinecraftLauncher().isLaunchAssist()) {
            t.getFrame().setVisible(true);
            t.getFrame().getNotices().selectRandom();
        }
    }

    public void onMinecraftError(Throwable e) {
        Alert.showLocError("launcher.error.title", "launcher.error.unknown", e);
    }

    public void onMinecraftKnownError(MinecraftException e) {
        Alert.showError(lang.get("launcher.error.title"), lang.get("launcher.error." + e.getLangPath(), e.getLangVars()), e.getCause());
    }

    @Override
    public void onCrashManagerInit(CrashManager manager) {
        manager.addListener(this);
        manager.addListener(crashFrame.get());
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
        U.log(e);
        Alert.showLocError("crash.error", MailSupportFrame.SUPPORT_MAIL);
    }

    @Override
    public void updateLocale() {
        if(crashFrame.isLoaded()) {
            crashFrame.get().updateLocale();
        }
    }
}
