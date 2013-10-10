package com.turikhay.tlauncher.ui;

import javax.swing.BoxLayout;

public class TLauncherSettingsPanel extends BlockablePanel {
   private static final long serialVersionUID = -9108973380914818944L;

   TLauncherSettingsPanel(SettingsForm sf) {
      this.setLayout(new BoxLayout(this, 3));
      this.add(sf.consoleSelect);
      this.add(sf.updaterSelect);
      this.add(sf.sunSelect);
   }

   protected void blockElement(Object reason) {
      this.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      this.setEnabled(true);
   }
}
