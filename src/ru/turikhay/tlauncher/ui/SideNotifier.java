package ru.turikhay.tlauncher.ui;

import java.awt.Image;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.listener.UpdateUIListener;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.ImagePanel;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;
import ru.turikhay.util.U;

public class SideNotifier extends ImagePanel implements UpdaterListener {
   private static final String LANG_PREFIX = "notifier.";
   private SideNotifier.NotifierStatus status;
   private Update update;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$SideNotifier$NotifierStatus;

   public SideNotifier() {
      super((Image)null, 1.0F, 0.75F, false, true);
      TLauncher.getInstance().getUpdater().addListener(this);
   }

   public SideNotifier.NotifierStatus getStatus() {
      return this.status;
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
         switch($SWITCH_TABLE$ru$turikhay$tlauncher$ui$SideNotifier$NotifierStatus()[this.status.ordinal()]) {
         case 1:
            Alert.showAsyncWarning(Localizable.get("notifier.failed.title"), Localizable.get("notifier.failed" + (U.getProxy() == null ? "" : ".proxy")));
            break;
         case 2:
            if (this.update == null) {
               throw new IllegalStateException("Update is NULL!");
            }

            String prefix = "notifier." + this.status.toString() + ".";
            String title = prefix + "title";
            String question = prefix + "question";
            boolean ask = Alert.showQuestion(Localizable.get(title), Localizable.get(question, this.update.getVersion()), this.update.getDescription());
            if (!ask) {
               return false;
            }

            UpdateUIListener listener = new UpdateUIListener(this.update);
            listener.push();
         case 3:
            break;
         default:
            throw new IllegalStateException("Unknown status: " + this.status);
         }

         return true;
      }
   }

   public void onUpdaterRequesting(Updater u) {
      this.setFoundUpdate((Update)null);
   }

   public void onUpdaterErrored(Updater.SearchFailed failed) {
      this.setStatus(SideNotifier.NotifierStatus.FAILED);
   }

   public void onUpdaterSucceeded(Updater.SearchSucceeded succeeded) {
      Update update = succeeded.getResponse().getUpdate();
      this.setFoundUpdate(update.isApplicable() ? update : null);
   }

   private void setFoundUpdate(Update upd) {
      this.update = upd;
      this.setStatus(upd == null ? SideNotifier.NotifierStatus.NONE : SideNotifier.NotifierStatus.FOUND);
      if (upd != null && !TLauncher.getInstance().isLauncherWorking() && TLauncher.getInstance().getSettings().getDouble("update.asked") != upd.getVersion()) {
         this.processClick();
         TLauncher.getInstance().getSettings().set("update.asked", upd.getVersion());
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$SideNotifier$NotifierStatus() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$ui$SideNotifier$NotifierStatus;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[SideNotifier.NotifierStatus.values().length];

         try {
            var0[SideNotifier.NotifierStatus.FAILED.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[SideNotifier.NotifierStatus.FOUND.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[SideNotifier.NotifierStatus.NONE.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$tlauncher$ui$SideNotifier$NotifierStatus = var0;
         return var0;
      }
   }

   public static enum NotifierStatus {
      FAILED("warning.png"),
      FOUND("down32.png"),
      NONE;

      private final Image image;

      private NotifierStatus(String imagePath) {
         this.image = imagePath == null ? null : ImageCache.getImage(imagePath);
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
