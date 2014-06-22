package ru.turikhay.tlauncher.ui;

import java.awt.Image;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.listener.UpdateUIListener;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.ImagePanel;
import ru.turikhay.tlauncher.updater.Ad;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;

public class LeftSideNotifier extends ImagePanel implements UpdaterListener {
   private static final long serialVersionUID = 8089346864504410975L;
   private static final String LANG_PREFIX = "notifier.left.";
   private LeftSideNotifier.NotifierStatus status;
   private Update update;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$LeftSideNotifier$NotifierStatus;

   protected LeftSideNotifier() {
      super((Image)null, 1.0F, 0.75F, false, true);
      TLauncher.getInstance().getUpdater().addListener(this);
   }

   public LeftSideNotifier.NotifierStatus getStatus() {
      return this.status;
   }

   public void setStatus(LeftSideNotifier.NotifierStatus status) {
      if (status == null) {
         throw new NullPointerException();
      } else {
         this.status = status;
         this.setImage(status.getImage());
         if (status == LeftSideNotifier.NotifierStatus.NONE) {
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
         switch($SWITCH_TABLE$ru$turikhay$tlauncher$ui$LeftSideNotifier$NotifierStatus()[this.status.ordinal()]) {
         case 1:
            Alert.showLocAsyncWarning("notifier.left." + this.status.toString());
            break;
         case 2:
            if (this.update == null) {
               throw new IllegalStateException("Update is NULL!");
            }

            String prefix = "notifier.left." + this.status.toString() + ".";
            String title = prefix + "title";
            String question = prefix + "question";
            boolean ask = Alert.showQuestion(Localizable.get(title), Localizable.get(question, this.update.getVersion() + " (" + this.update.getCode() + ")"), this.update.getDescription());
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

   public void onUpdaterRequestError(Updater u) {
      this.setStatus(LeftSideNotifier.NotifierStatus.FAILED);
   }

   public void onUpdateFound(Update upd) {
      if (!upd.isRequired()) {
         this.setFoundUpdate(upd);
      }
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
      this.setFoundUpdate((Update)null);
   }

   public void onAdFound(Updater u, Ad ad) {
      this.setFoundUpdate((Update)null);
   }

   private void setFoundUpdate(Update upd) {
      this.update = upd;
      this.setStatus(upd == null ? LeftSideNotifier.NotifierStatus.NONE : LeftSideNotifier.NotifierStatus.FOUND);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$LeftSideNotifier$NotifierStatus() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$ui$LeftSideNotifier$NotifierStatus;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[LeftSideNotifier.NotifierStatus.values().length];

         try {
            var0[LeftSideNotifier.NotifierStatus.FAILED.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[LeftSideNotifier.NotifierStatus.FOUND.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[LeftSideNotifier.NotifierStatus.NONE.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$tlauncher$ui$LeftSideNotifier$NotifierStatus = var0;
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
