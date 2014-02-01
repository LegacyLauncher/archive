package com.turikhay.tlauncher.ui.settings;

import java.awt.GridLayout;
import javax.swing.JPanel;

public class SaveSettingsPanel extends JPanel {
   private static final long serialVersionUID = 4156489797984574935L;

   SaveSettingsPanel(SettingsForm sf) {
      this.setOpaque(false);
      this.setLayout(new GridLayout(0, 2));
      this.add("Center", sf.saveButton);
      this.add("South", sf.defButton);
   }
}
