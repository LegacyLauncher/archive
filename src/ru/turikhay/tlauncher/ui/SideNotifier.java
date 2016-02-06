package ru.turikhay.tlauncher.ui;

import java.awt.Image;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.listener.UpdateUIListener;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.ImagePanel;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;

public class SideNotifier extends ImagePanel implements UpdaterListener {
   private SideNotifier.NotifierStatus status;
   private Update update;

   public SideNotifier() {
      super((Image)null, 1.0F, 0.75F, false, true);
      TLauncher.getInstance().getUpdater().addListener(this);
   }

   public void setStatus(SideNotifier.NotifierStatus status) {
      if (status == null) {
         throw new NullPointerException();
      } else {
         this.status = status;
         this.setImage(status.getImage());
         if (status == SideNotifier.NotifierStatus.NONE) {
            this.hide();
         } else {
            this.show();
         }

      }
   }

   protected boolean onClick() {
      boolean result = this.processClick();
      if (result) {
         this.hide();
      }

      return result;
   }

   private boolean processClick() {
      if (!super.onClick()) {
         return false;
      } else {
         switch(this.status) {
         case FAILED:
            Alert.showWarning(Localizable.get("notifier.failed.title"), Localizable.get("notifier.failed"));
            break;
         case FOUND:
            if (this.update == null) {
               throw new IllegalStateException("Update is NULL!");
            }

            String prefix = "notifier." + this.status + ".";
            String title = prefix + "title";
            String question = prefix + "question";
            boolean ask = Alert.showQuestion(Localizable.get(title), Localizable.get(question, this.update.getVersion()), this.update.getDescription());
            if (!ask) {
               return false;
            }

            UpdateUIListener listener = new UpdateUIListener(this.update);
            listener.push();
         case NONE:
            break;
         default:
            throw new IllegalStateException("Unknown status: " + this.status);
         }

         return true;
      }
   }

   public void onUpdaterErrored(Updater.SearchFailed failed) {
      this.setStatus(SideNotifier.NotifierStatus.FAILED);
   }

   public void onUpdaterSucceeded(Updater.SearchSucceeded succeeded) {
      Update update = succeeded.getResponse().getUpdate();
      if (!update.isRequired()) {
         this.setFoundUpdate(update.isApplicable() ? update : null);
      }

   }

   private void setFoundUpdate(Update upd) {
      this.update = upd;
      this.setStatus(upd == null ? SideNotifier.NotifierStatus.NONE : SideNotifier.NotifierStatus.FOUND);
      if (upd != null && !TLauncher.getInstance().isLauncherWorking() && TLauncher.getInstance().getSettings().getDouble("update.asked") != upd.getVersion()) {
         if (!this.update.isRequired()) {
            this.processClick();
         }

         TLauncher.getInstance().getSettings().set("update.asked", upd.getVersion());
      }

   }

   public static enum NotifierStatus {
      FAILED("warning.png"),
      FOUND("down32.png"),
      NONE;

      private final Image image;

      private NotifierStatus(String imagePath) {
         this.image = imagePath == null ? null : Images.getImage(imagePath);
      }

      private NotifierStatus() {
         this((String)null);
      }

      public Image getImage() {
         return this.image;
      }

      public String toString() {
         return super.toString().toLowerCase();
      }
   }
}
