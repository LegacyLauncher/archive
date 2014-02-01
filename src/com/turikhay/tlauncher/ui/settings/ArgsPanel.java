package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.block.BlockablePanel;
import javax.swing.BoxLayout;

public class ArgsPanel extends BlockablePanel {
   private static final long serialVersionUID = -197599581121292338L;

   ArgsPanel(SettingsForm sf) {
      this.setOpaque(false);
      this.setLayout(new BoxLayout(this, 3));
      this.add(sf.javaArgsField);
      this.add(sf.minecraftArgsField);
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }
}
