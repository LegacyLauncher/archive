package com.turikhay.tlauncher.ui;

import javax.swing.BoxLayout;

public class VersionsPanel extends BlockablePanel {
   private static final long serialVersionUID = -9108973380914818944L;

   VersionsPanel(SettingsForm sf) {
      this.setLayout(new BoxLayout(this, 3));
      this.add(sf.snapshotsSelect);
      this.add(sf.betaSelect);
      this.add(sf.alphaSelect);
   }

   protected void blockElement(Object reason) {
      this.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      this.setEnabled(true);
   }
}
