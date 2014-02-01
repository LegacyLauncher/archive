package com.turikhay.tlauncher.ui.listeners;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.UpdateListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import java.net.URI;
import net.minecraft.launcher.OperatingSystem;

public class UpdaterUIListener implements UpdaterListener, UpdateListener {
   private final TLauncher t;
   private final Settings lang;
   private final GlobalSettings global;

   public UpdaterUIListener(TLauncher tlauncher) {
      this.t = tlauncher;
      this.lang = this.t.getLang();
      this.global = this.t.getSettings();
   }

   public void onUpdaterRequesting(Updater u) {
   }

   public void onUpdaterRequestError(Updater u) {
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
   }

   public void onUpdateFound(Updater u, Update upd) {
      if (!this.t.isLauncherWorking()) {
         double version = upd.getVersion();
         Alert.showWarning(this.lang.get("updater.found.title"), this.lang.get("updater.found", "v", version), upd.getDescription());
         this.block();
         if (Updater.isAutomode()) {
            upd.addListener(this);
            upd.download();
         } else {
            if (this.openUpdateLink(upd.getDownloadLink())) {
               TLauncher.kill();
            }

         }
      }
   }

   public void onUpdateError(Update u, Throwable e) {
      if (Alert.showQuestion(this.lang.get("updater.error.title"), this.lang.get("updater.download-error"), e, true)) {
         this.openUpdateLink(u.getDownloadLink());
      }

      this.unblock();
   }

   public void onUpdateDownloading(Update u) {
   }

   public void onUpdateDownloadError(Update u, Throwable e) {
      this.onUpdateError(u, e);
   }

   public void onUpdateReady(Update u) {
      Alert.showWarning(this.lang.get("updater.downloaded.title"), this.lang.get("updater.downloaded"));
      u.apply();
   }

   public void onUpdateApplying(Update u) {
   }

   public void onUpdateApplyError(Update u, Throwable e) {
      if (Alert.showQuestion(this.lang.get("updater.save-error.title"), this.lang.get("updater.save-error"), e, true)) {
         this.openUpdateLink(u.getDownloadLink());
      }

      this.unblock();
   }

   private boolean openUpdateLink(URI uri) {
      try {
         OperatingSystem.openLink(uri);
         return true;
      } catch (Exception var3) {
         Alert.showError(this.lang.get("updater.found.cannotopen.title"), this.lang.get("updater.found.cannotopen"), (Object)uri);
         return false;
      }
   }

   public void onAdFound(Updater u, Ad ad) {
      if (this.global.getInteger("updater.ad") != ad.getID()) {
         if (ad.canBeShown()) {
            this.global.set("updater.ad", ad.getID());
            ad.show(false);
         }
      }
   }

   private void block() {
      Blocker.block((Blockable)this.t.getFrame().mp, (Object)"updater");
   }

   private void unblock() {
      Blocker.unblock((Blockable)this.t.getFrame().mp, (Object)"updater");
   }
}
