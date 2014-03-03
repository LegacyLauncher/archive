package com.turikhay.tlauncher.ui.listeners;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.alert.Alert;
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
   private final LangConfiguration lang;
   private final Configuration global;
   private Update hiddenUpdate;
   private Throwable hiddenError;

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

   public void onUpdateFound(Update upd, boolean force, boolean async) {
      boolean download = true;
      if (!force && this.t.isLauncherWorking()) {
         download = this.global.getConnectionQuality().equals(Configuration.ConnectionQuality.GOOD);
         this.hiddenUpdate = upd;
      }

      boolean shown = force || this.hiddenUpdate == null;
      double version = upd.getVersion();
      if (shown) {
         Alert.showWarning(this.lang.get("updater.found.title"), this.lang.get("updater.found", version), upd.getDescription());
      }

      this.block();
      if (Updater.isAutomode()) {
         upd.addListener(this);
         if (download) {
            upd.download(async);
         }

      } else {
         if (shown && this.openUpdateLink(upd.getDownloadLink())) {
            TLauncher.kill();
         }

      }
   }

   public void onUpdateFound(Update upd) {
      this.onUpdateFound(upd, false, false);
   }

   public void onUpdateError(Update u, Throwable e) {
      if (this.hiddenUpdate != null) {
         this.hiddenError = e;
      } else if (Alert.showQuestion(this.lang.get("updater.error.title"), this.lang.get("updater.download-error"), e, true)) {
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
      this.onUpdateReady(u, false, false);
   }

   public void onUpdateReady(Update u, boolean force, boolean showChangeLog) {
      if (force || !u.equals(this.hiddenUpdate)) {
         Alert.showWarning(this.lang.get("updater.downloaded.title"), this.lang.get("updater.downloaded"), showChangeLog ? u.getDescription() : null);
         u.apply();
      }
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
   }

   public void applyDelayedUpdate() {
      if (this.hiddenUpdate != null) {
         int step = this.hiddenUpdate.getStep();
         if (this.hiddenError != null) {
            this.onUpdateError(this.hiddenUpdate, this.hiddenError);
         } else {
            switch(step) {
            case 0:
               this.onUpdateFound(this.hiddenUpdate, true, true);
            case 1:
               this.hiddenUpdate = null;
               return;
            case 2:
               this.onUpdateReady(this.hiddenUpdate, true, true);
               return;
            default:
            }
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
