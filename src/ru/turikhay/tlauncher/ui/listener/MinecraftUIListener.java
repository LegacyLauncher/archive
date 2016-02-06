package ru.turikhay.tlauncher.ui.listener;

import java.net.URI;
import java.util.Iterator;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.crash.CrashSignatureContainer;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public class MinecraftUIListener implements MinecraftListener {
   private final TLauncher t;
   private final LangConfiguration lang;

   public MinecraftUIListener(TLauncher tlauncher) {
      this.t = tlauncher;
      this.lang = this.t.getLang();
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

   public void onMinecraftCrash(Crash crash) {
      if (!this.t.getLauncher().isLaunchAssist()) {
         this.t.show();
      }

      String p = "crash.";
      String title = Localizable.get(p + "title");
      String report = crash.getFile();
      if (!crash.isRecognized()) {
         Alert.showLocError(title, p + "unknown" + (crash.getNativeReport() == null ? "" : ".native"), (Object)null);
      } else {
         Iterator var6 = crash.getSignatures().iterator();

         while(var6.hasNext()) {
            CrashSignatureContainer.CrashSignature sign = (CrashSignatureContainer.CrashSignature)var6.next();
            String path = sign.getPath();
            String message = p + path;
            String url = message + ".url";
            URI uri = U.makeURI(url);
            if (uri != null) {
               if (Alert.showLocQuestion(title, message, report)) {
                  OS.openLink(uri);
               }
            } else {
               Alert.showLocMessage(title, message, report);
            }
         }
      }

   }

   public void onMinecraftError(Throwable e) {
      Alert.showLocError("launcher.error.title", "launcher.error.unknown", e);
   }

   public void onMinecraftKnownError(MinecraftException e) {
      Alert.showError(this.lang.get("launcher.error.title"), this.lang.get("launcher.error." + e.getLangPath(), e.getLangVars()), e.getCause());
   }
}
