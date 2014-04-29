package ru.turikhay.tlauncher.ui;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.swing.ImagePanel;
import ru.turikhay.tlauncher.updater.Ad;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;

public class ConnectionWarning extends ImagePanel implements UpdaterListener {
   private static final long serialVersionUID = 8089346864504410975L;
   private final String langPath = "firewall";

   public ConnectionWarning() {
      super("warning.png", 1.0F, 0.75F, false, false);
      TLauncher.getInstance().getUpdater().addListener(this);
   }

   protected boolean onClick() {
      if (!super.onClick()) {
         return false;
      } else {
         Alert.showLocAsyncWarning(this.langPath);
         return true;
      }
   }

   public void onUpdaterRequestError(Updater u) {
      this.show();
   }

   public void onUpdaterRequesting(Updater u) {
   }

   public void onUpdateFound(Update upd) {
      this.hide();
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
      this.hide();
   }

   public void onAdFound(Updater u, Ad ad) {
   }
}
