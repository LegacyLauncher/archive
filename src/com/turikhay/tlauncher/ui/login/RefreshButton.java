package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.ui.ImageButton;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.minecraft.launcher.events.RefreshedListener;
import net.minecraft.launcher.updater.VersionManager;

public class RefreshButton extends ImageButton implements RefreshedListener, UpdaterListener {
   private static final long serialVersionUID = -1334187593288746348L;
   public static final int TYPE_REFRESH = 0;
   public static final int TYPE_CANCEL = 1;
   private boolean VERSIONS;
   private boolean RESOURCES;
   private LoginForm lf;
   private VersionManager vm;
   private int type;
   private final Image refresh;
   private final Image cancel;
   private Updater updaterFlag;

   RefreshButton(LoginForm loginform, int type) {
      this.refresh = loadImage("refresh.png");
      this.cancel = loadImage("cancel.png");
      this.lf = loginform;
      this.vm = this.lf.tlauncher.getVersionManager();
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.setType(type, false);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            RefreshButton.this.onPressButton();
         }
      });
      this.initImage();
      TLauncher.getInstance().getVersionManager().addRefreshedListener(this);
      TLauncher.getInstance().getUpdater().addListener(this);
   }

   RefreshButton(LoginForm loginform) {
      this(loginform, 0);
   }

   private void onPressButton() {
      switch(this.type) {
      case 0:
         if (this.updaterFlag != null) {
            this.updaterFlag.asyncFindUpdate();
         }

         this.vm.asyncRefresh();
         break;
      case 1:
         this.vm.cancelVersionRefresh();
         break;
      default:
         throw new IllegalArgumentException("Unknown type: " + this.type + ". Use RefreshButton.TYPE_* constants.");
      }

      this.lf.defocus();
   }

   public void setType(int type) {
      this.setType(type, true);
   }

   public void setType(int type, boolean repaint) {
      switch(type) {
      case 0:
         if (!this.VERSIONS && !this.RESOURCES) {
            this.image = this.refresh;
            break;
         }

         return;
      case 1:
         this.image = this.cancel;
         break;
      default:
         throw new IllegalArgumentException("Unknown type: " + type + ". Use RefreshButton.TYPE_* constants.");
      }

      this.type = type;
      if (repaint && this.getGraphics() != null) {
         this.paint(this.getGraphics());
      }

   }

   public void onVersionManagerUpdated(VersionManager vm) {
      this.vm = vm;
   }

   public void onVersionsRefreshing(VersionManager vm) {
      this.VERSIONS = true;
      this.setType(1);
   }

   public void onVersionsRefreshingFailed(VersionManager vm) {
      this.VERSIONS = false;
      this.setType(0);
   }

   public void onVersionsRefreshed(VersionManager vm) {
      this.VERSIONS = false;
      this.setType(0);
   }

   public void onResourcesRefreshing(VersionManager vm) {
      this.RESOURCES = true;
      this.setType(1);
   }

   public void onResourcesRefreshed(VersionManager vm) {
      this.RESOURCES = false;
      this.setType(0);
   }

   public void onUpdaterRequesting(Updater u) {
   }

   public void onUpdaterRequestError(Updater u) {
      this.updaterFlag = u;
   }

   public void onUpdateFound(Update upd) {
      this.updaterFlag = null;
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
      this.updaterFlag = null;
   }

   public void onAdFound(Updater u, Ad ad) {
   }
}
