package com.turikhay.tlauncher.ui;

import java.awt.GridLayout;
import java.awt.LayoutManager;

public class AdditionalButtonsPanel extends BlockablePanel {
   private static final long serialVersionUID = 7217356608637239309L;
   public final SupportButton support;
   public final FolderButton folder;
   public final RefreshButton refresh;
   public final SettingsButton settings;

   AdditionalButtonsPanel(ButtonPanel bp) {
      LoginForm lf = bp.lf;
      LayoutManager layout = new GridLayout(0, 4);
      this.setLayout(layout);
      this.setOpaque(false);
      this.support = new SupportButton(lf);
      this.folder = new FolderButton(lf);
      this.refresh = new RefreshButton(lf);
      this.settings = new SettingsButton(lf);
      this.add(this.support);
      this.add(this.folder);
      this.add(this.refresh);
      this.add(this.settings);
   }

   protected void blockElement(Object reason) {
      if (!reason.toString().endsWith("_refresh")) {
         this.refresh.setEnabled(false);
      }

      this.settings.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      if (!reason.toString().endsWith("_refresh")) {
         this.refresh.setEnabled(true);
      }

      this.settings.setEnabled(true);
   }
}
