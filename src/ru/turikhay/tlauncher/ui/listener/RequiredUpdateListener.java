package ru.turikhay.tlauncher.ui.listener;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.updater.AdParser;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;

public class RequiredUpdateListener implements UpdaterListener {
   public RequiredUpdateListener(Updater updater) {
      updater.addListener(this);
   }

   public void onUpdaterRequesting(Updater u) {
   }

   public void onUpdaterRequestError(Updater u) {
   }

   public void onUpdateFound(Update upd) {
      if (upd.isRequired() || TLauncher.isBeta()) {
         String prefix = "updater.required.found.";
         String title = prefix + "title";
         String message = prefix + "message";
         Alert.showWarning(Localizable.get(title), Localizable.get(message, upd.getVersion() + " (" + upd.getCode() + ")"), upd.getDescription());
         UpdateUIListener listener = new UpdateUIListener(upd);
         listener.push();
      }
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
   }

   public void onAdFound(Updater u, AdParser ad) {
   }
}
