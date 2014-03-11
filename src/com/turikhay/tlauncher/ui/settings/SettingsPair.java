package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.loc.LocalizableLabel;
import com.turikhay.tlauncher.ui.swing.extended.VPanel;
import java.awt.Component;

class SettingsPair {
   private final LocalizableLabel label;
   private final SettingsHandler[] handlers;
   private final Component[] fields;
   private final VPanel panel;

   SettingsPair(String labelPath, SettingsHandler... handlers) {
      this.label = new LocalizableLabel(labelPath);
      int num = handlers.length;
      this.fields = new Component[num];

      for(int i = 0; i < num; ++i) {
         this.fields[i] = handlers[i].getComponent();
      }

      this.handlers = handlers;
      this.panel = new VPanel();
      this.panel.add(this.fields);
   }

   public SettingsHandler[] getHandlers() {
      return this.handlers;
   }

   public LocalizableLabel getLabel() {
      return this.label;
   }

   public Component[] getFields() {
      return this.fields;
   }

   public VPanel getPanel() {
      return this.panel;
   }
}
