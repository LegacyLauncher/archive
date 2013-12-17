package com.turikhay.tlauncher.ui;

import java.awt.GridLayout;
import java.awt.LayoutManager;

public class AdditionalButtonsPanel extends BlockablePanel {
   private static final long serialVersionUID = 7217356608637239309L;
   public final SupportButton support;
   public final FolderButton folder;
   public final RefreshButton refresh;
   public final SettingsButton settings;
   private final LoginForm lf;

   AdditionalButtonsPanel(ButtonPanel bp) {
      this.lf = bp.lf;
      LayoutManager layout = new GridLayout(0, 4);
      this.setLayout(layout);
      this.setOpaque(false);
      this.support = new SupportButton(this.lf);
      this.folder = new FolderButton(this.lf);
      this.refresh = new RefreshButton(this.lf);
      this.settings = new SettingsButton(this.lf);
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
