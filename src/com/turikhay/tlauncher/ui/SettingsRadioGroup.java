package com.turikhay.tlauncher.ui;

import java.util.Iterator;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

public class SettingsRadioGroup extends ButtonGroup {
   private static final long serialVersionUID = 1L;
   private final String path;

   SettingsRadioGroup(String path, SettingsRadioButton... buttons) {
      SettingsRadioButton[] var6 = buttons;
      int var5 = buttons.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         SettingsRadioButton button = var6[var4];
         this.add(button);
      }

      this.path = path;
   }

   public void add(SettingsRadioButton b) {
      super.add(b);
   }

   public void remove(SettingsRadioButton b) {
      super.remove(b);
   }

   public String getSettingsPath() {
      return this.path;
   }

   public String getValue() {
      Iterator var2 = this.buttons.iterator();

      while(var2.hasNext()) {
         AbstractButton button = (AbstractButton)var2.next();
         if (button instanceof SettingsRadioButton) {
            SettingsRadioButton butt = (SettingsRadioButton)button;
            if (butt.isSelected()) {
               return butt.getValue();
            }
         }
      }

      return null;
   }

   public void selectValue(String value) {
      Iterator var3 = this.buttons.iterator();

      while(var3.hasNext()) {
         AbstractButton button = (AbstractButton)var3.next();
         if (button instanceof SettingsRadioButton) {
            SettingsRadioButton butt = (SettingsRadioButton)button;
            if (butt.getValue().equals(value)) {
               this.setSelected(butt.getModel(), true);
               break;
            }
         }
      }

   }
}
