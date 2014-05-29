package ru.turikhay.tlauncher.ui.listener;

import java.net.URI;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.UpdateListener;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.util.OS;

public class UpdateUIListener implements UpdateListener {
   private final TLauncher t;
   private final Update u;

   public UpdateUIListener(Update u) {
      if (u == null) {
         throw new NullPointerException();
      } else {
         this.t = TLauncher.getInstance();
         this.u = u;
         u.addListener(this);
      }
   }

   public void push() {
      if (Updater.isAutomode()) {
         this.block();
         this.u.download(true);
      } else {
         this.openUpdateLink(this.u.getDownloadLink());
      }

   }

   public void onUpdateError(Update u, Throwable e) {
      if (Alert.showLocQuestion("updater.error.title", "updater.download-error", e)) {
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

   private void onUpdateReady(Update u, boolean force, boolean showChangeLog) {
      Alert.showLocWarning("updater.downloaded", (Object)(showChangeLog ? u.getDescription() : null));
      u.apply();
   }

   public void onUpdateApplying(Update u) {
   }

   public void onUpdateApplyError(Update u, Throwable e) {
      if (Alert.showLocQuestion("updater.save-error", (Object)e)) {
         this.openUpdateLink(u.getDownloadLink());
      }

      this.unblock();
   }

   private boolean openUpdateLink(URI uri) {
      if (OS.openLink(uri, false)) {
         return true;
      } else {
         Alert.showLocError("updater.found.cannotopen", uri);
         return false;
      }
   }

   private void block() {
      Blocker.block((Blockable)this.t.getFrame().mp, (Object)"updater");
   }

   private void unblock() {
      Blocker.unblock((Blockable)this.t.getFrame().mp, (Object)"updater");
   }
}
