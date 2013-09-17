package com.turikhay.tlauncher.ui;

import javax.swing.BoxLayout;

public class ArgsPanel extends BlockablePanel {
   private static final long serialVersionUID = -197599581121292338L;

   ArgsPanel(SettingsForm sf) {
      this.setLayout(new BoxLayout(this, 3));
      this.add(sf.javaArgsField);
      this.add(sf.minecraftArgsField);
   }

   protected void blockElement(Object reason) {
      this.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      this.setEnabled(true);
   }
}
