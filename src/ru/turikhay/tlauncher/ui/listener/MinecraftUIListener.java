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

public class MinecraftUIListener implements CrashManagerListener, MinecraftListener, LocalizableComponent {
   private final CrashProcessingFrame crashFrame;
   private final TLauncher t;
   private final LangConfiguration lang;

   public MinecraftUIListener(TLauncher tlauncher) {
      this.t = tlauncher;
      this.lang = this.t.getLang();
      this.crashFrame = new CrashProcessingFrame();
   }

   public CrashProcessingFrame getCrashProcessingFrame() {
      return this.crashFrame;
   }

   public void onMinecraftPrepare() {
   }

   public void onMinecraftAbort() {
   }

   public void onMinecraftLaunch() {
      if (!this.t.getSettings().getActionOnLaunch().equals(Configuration.ActionOnLaunch.NOTHING)) {
         this.t.hide();
      }

   }

   public void onMinecraftClose() {
      if (this.t.getLauncher().isLaunchAssist()) {
         this.t.show();
      }

   }

   public void onMinecraftError(Throwable e) {
      Alert.showLocError("launcher.error.title", "launcher.error.unknown", e);
   }

   public void onMinecraftKnownError(MinecraftException e) {
      Alert.showError(this.lang.get("launcher.error.title"), this.lang.get("launcher.error." + e.getLangPath(), e.getLangVars()), e.getCause());
   }

   public void onCrashManagerInit(CrashManager manager) {
      manager.addListener(this);
      manager.addListener(this.crashFrame);
   }

   public void onCrashManagerProcessing(CrashManager manager) {
   }

   public void onCrashManagerComplete(CrashManager manager, Crash crash) {
   }

   public void onCrashManagerCancelled(CrashManager manager) {
   }

   public void onCrashManagerFailed(CrashManager manager, Exception e) {
      Alert.showLocError("crash.error", "support@tlauncher.ru");
   }

   public void updateLocale() {
      this.crashFrame.updateLocale();
   }
}
