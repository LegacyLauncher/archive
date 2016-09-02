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
import ru.turikhay.util.U;

public class MinecraftUIListener implements MinecraftListener, CrashManagerListener, LocalizableComponent {
    private final CrashProcessingFrame crashFrame;

    private final TLauncher t;
    private final LangConfiguration lang;

    public MinecraftUIListener(TLauncher tlauncher) {
        t = tlauncher;
        lang = t.getLang();
        crashFrame = new CrashProcessingFrame();
    }

    public CrashProcessingFrame getCrashProcessingFrame() {
        return crashFrame;
    }

    public void onMinecraftPrepare() {
    }

    public void onMinecraftAbort() {
    }

    public void onMinecraftLaunch() {
        if (!t.getSettings().getActionOnLaunch().equals(Configuration.ActionOnLaunch.NOTHING)) {
            t.hide();
        }

    }

    public void onMinecraftClose() {
        if (t.getLauncher().isLaunchAssist()) {
            t.show();
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
        manager.addListener(crashFrame);
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
        crashFrame.updateLocale();
    }
}
