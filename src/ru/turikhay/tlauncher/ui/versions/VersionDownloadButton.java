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
import ru.turikhay.tlauncher.ui.images.Images;
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
      switch(this.state) {
      case DOWNLOAD:
         this.onDownloadPressed();
         break;
      case STOP:
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
         Iterator manager = list.iterator();

         while(manager.hasNext()) {
            VersionSyncInfo containers = (VersionSyncInfo)manager.next();
            if (this.forceDownload) {
               if (!containers.hasRemote()) {
                  Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.local", containers.getID()));
                  return;
               }

               if (containers.isUpToDate() && containers.isInstalled()) {
                  ++countLocal;
                  local = containers;
               }
            }
         }

         if (countLocal > 0) {
            String var17 = Localizable.get("version.manager.downloader.warning.title");
            Object container;
            String var19;
            if (countLocal == 1) {
               var19 = "single";
               container = local.getID();
            } else {
               var19 = "multiply";
               container = countLocal;
            }

            if (!Alert.showQuestion(var17, Localizable.get("version.manager.downloader.warning.force." + var19, container))) {
               return;
            }
         }

         ArrayList var18 = new ArrayList();
         VersionManager var20 = TLauncher.getInstance().getVersionManager();

         Iterator var7;
         try {
            this.downloading = true;
            var7 = list.iterator();

            while(var7.hasNext()) {
               VersionSyncInfo var21 = (VersionSyncInfo)var7.next();

               try {
                  var21.resolveCompleteVersion(var20, this.forceDownload);
                  VersionSyncInfoContainer errors = var20.downloadVersion(var21, false, this.forceDownload);
                  if (this.aborted) {
                     return;
                  }

                  if (!errors.getList().isEmpty()) {
                     var18.add(errors);
                  }
               } catch (Exception var16) {
                  Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.getting", var21.getID()), var16);
                  return;
               }
            }

            if (var18.isEmpty()) {
               Alert.showMessage(Localizable.get("version.manager.downloader.info.title"), Localizable.get("version.manager.downloader.info.no-needed"));
               return;
            }

            if (var18.size() > 1) {
               DownloadableContainer.removeDuplicates(var18);
            }

            if (this.aborted) {
               return;
            }

            var7 = var18.iterator();

            while(true) {
               if (!var7.hasNext()) {
                  this.handler.downloading = list;
                  this.handler.onVersionDownload(list);
                  this.handler.downloader.startDownloadAndWait();
                  break;
               }

               DownloadableContainer var22 = (DownloadableContainer)var7.next();
               this.handler.downloader.add(var22);
            }
         } finally {
            this.downloading = false;
         }

         this.handler.downloading.clear();
         var7 = var18.iterator();

         while(var7.hasNext()) {
            VersionSyncInfoContainer var23 = (VersionSyncInfoContainer)var7.next();
            List var24 = var23.getErrors();
            VersionSyncInfo version = var23.getVersion();
            if (var24.isEmpty()) {
               try {
                  var20.getLocalList().saveVersion(version.getCompleteVersion(this.forceDownload));
               } catch (IOException var15) {
                  Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.saving", version.getID()), var15);
                  return;
               }
            } else if (!(var24.get(0) instanceof AbortedDownloadException)) {
               Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.downloading", version.getID()), var24);
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

   public static enum ButtonState {
      DOWNLOAD("down.png"),
      STOP("cancel.png");

      final Image image;

      private ButtonState(String image) {
         this.image = Images.getImage(image);
      }
   }
}
