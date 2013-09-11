package com.turikhay.tlauncher.ui;

import java.awt.GridLayout;
import java.awt.LayoutManager;

public class AdditionalButtonsPanel extends BlockablePanel {
   private static final long serialVersionUID = 7217356608637239309L;
   SupportButton support;
   FolderButton folder;
   RefreshButton refresh;
   SettingsButton settings;

   AdditionalButtonsPanel(ButtonPanel bp) {
      LoginForm lf = bp.lf;
      LayoutManager layout = new GridLayout(0, 4);
      this.setLayout(layout);
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
      this.refresh.setEnabled(false);
      this.settings.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      this.refresh.setEnabled(true);
      this.settings.setEnabled(true);
   }
}
