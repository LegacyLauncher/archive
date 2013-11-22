package com.turikhay.tlauncher.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.minecraft.launcher.events.RefreshedListener;
import net.minecraft.launcher.updater.VersionManager;

public class RefreshButton extends ImageButton implements RefreshedListener {
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

   RefreshButton(LoginForm loginform, int type) {
      this.refresh = loadImage("refresh.png");
      this.cancel = loadImage("cancel.png");
      this.lf = loginform;
      this.vm = this.lf.t.getVersionManager();
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.setType(type, false);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            RefreshButton.this.onPressButton();
         }
      });
      this.initImage();
   }

   RefreshButton(LoginForm loginform) {
      this(loginform, 0);
   }

   private void onPressButton() {
      switch(this.type) {
      case 0:
         this.vm.asyncRefresh();
         this.vm.asyncRefreshResources();
         break;
      case 1:
         this.vm.cancelVersionRefresh();
         this.vm.cancelResourceRefresh();
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
}
