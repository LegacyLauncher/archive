package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;

public class ConnectionWarning extends ImagePanel implements UpdaterListener {
   private static final long serialVersionUID = 8089346864504410975L;
   private final String langPath = "firewall";

   public ConnectionWarning() {
      super(ImageButton.loadImage("warning.png"), 1.0F, 0.75F, false, false);
   }

   protected boolean onClick() {
      if (!super.onClick()) {
         return false;
      } else {
         Alert.showAsyncWarning(this.langPath);
         return true;
      }
   }

   public void onUpdaterRequestError(Updater u) {
      this.show();
   }

   public void onUpdaterRequesting(Updater u) {
   }

   public void onUpdateFound(Updater u, Update upd) {
      this.hide();
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
      this.hide();
   }

   public void onAdFound(Updater u, Ad ad) {
   }
}
