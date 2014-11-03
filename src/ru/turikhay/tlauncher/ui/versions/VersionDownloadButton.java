package ru.turikhay.tlauncher.ui.versions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPopupMenu;
import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.AbortedDownloadException;
import ru.turikhay.tlauncher.downloader.DownloadableContainer;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionSyncInfoContainer;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Unblockable;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.swing.ImageButton;

public class VersionDownloadButton extends ImageButton implements VersionHandlerListener, Unblockable {
   private static final String SELECTION_BLOCK = "selection";
   private static final String PREFIX = "version.manager.downloader.";
   private static final String WARNING = "version.manager.downloader.warning.";
   private static final String WARNING_TITLE = "version.manager.downloader.warning.title";
   private static final String WARNING_FORCE = "version.manager.downloader.warning.force.";
   private static final String ERROR = "version.manager.downloader.error.";
   private static final String ERROR_TITLE = "version.manager.downloader.error.title";
   private static final String INFO = "version.manager.downloader.info.";
   private static final String INFO_TITLE = "version.manager.downloader.info.title";
   private static final String MENU = "version.manager.downloader.menu.";
   final VersionHandler handler;
   final Blockable blockable;
   private final JPopupMenu menu;
   private final LocalizableMenuItem ordinary;
   private final LocalizableMenuItem force;
   private VersionDownloadButton.ButtonState state;
   private boolean downloading;
   private boolean aborted;
   boolean forceDownload;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$versions$VersionDownloadButton$ButtonState;

   VersionDownloadButton(VersionList list) {
      this.handler = list.handler;
      this.blockable = new Blockable() {
         public void block(Object reason) {
            VersionDownloadButton.this.setEnabled(false);
         }

         public void unblock(Object reason) {
            VersionDownloadButton.this.setEnabled(true);
         }
      };
      this.menu = new JPopupMenu();
      this.ordinary = new LocalizableMenuItem("version.manager.downloader.menu.ordinary");
      this.ordinary.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            VersionDownloadButton.this.forceDownload = false;
            VersionDownloadButton.this.onDownloadCalled();
         }
      });
      this.menu.add(this.ordinary);
      this.force = new LocalizableMenuItem("version.manager.downloader.menu.force");
      this.force.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            VersionDownloadButton.this.forceDownload = true;
            VersionDownloadButton.this.onDownloadCalled();
         }
      });
      this.menu.add(this.force);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            VersionDownloadButton.this.onPressed();
         }
      });
      this.setState(VersionDownloadButton.ButtonState.DOWNLOAD);
      this.handler.addListener(this);
   }

   void setState(VersionDownloadButton.ButtonState state) {
      if (state == null) {
         throw new NullPointerException();
      } else {
         this.state = state;
         this.setImage(state.image);
      }
   }

   void onPressed() {
      switch($SWITCH_TABLE$ru$turikhay$tlauncher$ui$versions$VersionDownloadButton$ButtonState()[this.state.ordinal()]) {
      case 1:
         this.onDownloadPressed();
         break;
      case 2:
         this.onStopCalled();
      }

   }

   void onDownloadPressed() {
      this.menu.show(this, 0, this.getHeight());
   }

   void onDownloadCalled() {
      if (this.state != VersionDownloadButton.ButtonState.DOWNLOAD) {
         throw new IllegalStateException();
      } else {
         this.handler.thread.startThread.iterate();
      }
   }

   void onStopCalled() {
      if (this.state != VersionDownloadButton.ButtonState.STOP) {
         throw new IllegalStateException();
      } else {
         this.handler.thread.stopThread.iterate();
      }
   }

   void startDownload() {
      this.aborted = false;
      List list = this.handler.getSelectedList();
      if (list != null && !list.isEmpty()) {
         int countLocal = 0;
         VersionSyncInfo local = null;
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            VersionSyncInfo version = (VersionSyncInfo)var5.next();
            if (this.forceDownload) {
               if (!version.hasRemote()) {
                  Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.local", version.getID()));
                  return;
               }

               if (version.isUpToDate() && version.isInstalled()) {
                  ++countLocal;
                  local = version;
               }
            }
         }

         if (countLocal > 0) {
            String title = Localizable.get("version.manager.downloader.warning.title");
            Object var;
            String suffix;
            if (countLocal == 1) {
               suffix = "single";
               var = local.getID();
            } else {
               suffix = "multiply";
               var = countLocal;
            }

            if (!Alert.showQuestion(title, Localizable.get("version.manager.downloader.warning.force." + suffix, var))) {
               return;
            }
         }

         List containers = new ArrayList();
         VersionManager manager = TLauncher.getInstance().getVersionManager();

         Iterator var7;
         try {
            this.downloading = true;
            var7 = list.iterator();

            while(var7.hasNext()) {
               VersionSyncInfo version = (VersionSyncInfo)var7.next();

               try {
                  VersionSyncInfoContainer container = manager.downloadVersion(version, this.forceDownload);
                  if (this.aborted) {
                     return;
                  }

                  if (!container.getList().isEmpty()) {
                     containers.add(container);
                  }
               } catch (Exception var15) {
                  Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.getting", version.getID()), var15);
                  return;
               }
            }

            if (containers.isEmpty()) {
               Alert.showMessage(Localizable.get("version.manager.downloader.info.title"), Localizable.get("version.manager.downloader.info.no-needed"));
               return;
            }

            if (containers.size() > 1) {
               DownloadableContainer.removeDublicates(containers);
            }

            if (this.aborted) {
               return;
            }

            var7 = containers.iterator();

            while(var7.hasNext()) {
               DownloadableContainer c = (DownloadableContainer)var7.next();
               this.handler.downloader.add(c);
            }

            this.handler.downloading = list;
            this.handler.onVersionDownload(list);
            this.handler.downloader.startDownloadAndWait();
         } finally {
            this.downloading = false;
         }

         this.handler.downloading.clear();
         var7 = containers.iterator();

         while(var7.hasNext()) {
            VersionSyncInfoContainer container = (VersionSyncInfoContainer)var7.next();
            List errors = container.getErrors();
            VersionSyncInfo version = container.getVersion();
            if (errors.isEmpty()) {
               try {
                  manager.getLocalList().saveVersion(version.getCompleteVersion(this.forceDownload));
               } catch (IOException var14) {
                  Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.saving", version.getID()), var14);
                  return;
               }
            } else if (!(errors.get(0) instanceof AbortedDownloadException)) {
               Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.downloading", version.getID()), errors);
            }
         }

         this.handler.refresh();
      }
   }

   void stopDownload() {
      this.aborted = true;
      if (this.downloading) {
         this.handler.downloader.stopDownloadAndWait();
      }

   }

   public void onVersionRefreshing(VersionManager vm) {
   }

   public void onVersionRefreshed(VersionManager vm) {
   }

   public void onVersionSelected(List versions) {
      if (!this.downloading) {
         this.blockable.unblock("selection");
      }

   }

   public void onVersionDeselected() {
      if (!this.downloading) {
         this.blockable.block("selection");
      }

   }

   public void onVersionDownload(List list) {
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$versions$VersionDownloadButton$ButtonState() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$ui$versions$VersionDownloadButton$ButtonState;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[VersionDownloadButton.ButtonState.values().length];

         try {
            var0[VersionDownloadButton.ButtonState.DOWNLOAD.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[VersionDownloadButton.ButtonState.STOP.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$tlauncher$ui$versions$VersionDownloadButton$ButtonState = var0;
         return var0;
      }
   }

   public static enum ButtonState {
      DOWNLOAD("down.png"),
      STOP("cancel.png");

      final Image image;

      private ButtonState(String image) {
         this.image = ImageCache.getImage(image);
      }
   }
}
