package ru.turikhay.tlauncher.ui.listener;

import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;

public class RequiredUpdateListener implements UpdaterListener {
   public RequiredUpdateListener(Updater updater) {
      updater.addListener(this);
   }

   public void onUpdaterErrored(Updater.SearchFailed failed) {
   }

   public void onUpdaterSucceeded(Updater.SearchSucceeded succeeded) {
      Update update = succeeded.getResponse().getUpdate();
      if (update.isApplicable() && update.isRequired()) {
         String prefix = "updater.required.found.";
         String title = prefix + "title";
         String message = prefix + "message";
         Alert.showWarning(Localizable.get(title), Localizable.get(message, update.getVersion()), update.getDescription());
         UpdateUIListener listener = new UpdateUIListener(update);
         listener.push();
      }

   }
}
