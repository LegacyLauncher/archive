package com.turikhay.tlauncher.ui.settings;

import java.awt.Component;

public class SettingsFieldHandler extends SettingsHandler {
   private final SettingsField field;
   private final Component component;

   SettingsFieldHandler(String path, Component comp) {
      super(path);
      if (!(comp instanceof SettingsField)) {
         throw new IllegalArgumentException();
      } else {
         this.component = comp;
         this.field = (SettingsField)comp;
      }
   }

   public Component getComponent() {
      return this.component;
   }

   public String getValue() {
      return this.field.getSettingsValue();
   }

   protected void setValue0(String s) {
      this.field.setSettingsValue(s);
   }

   public boolean isValid() {
      return this.field.isValueValid();
   }
}
