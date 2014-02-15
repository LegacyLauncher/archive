package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.converter.StringConverter;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;

public class SettingsComboBox extends ExtendedComboBox implements SettingsField {
   private static final long serialVersionUID = -2320340434786516374L;

   public SettingsComboBox(StringConverter converter, Object... values) {
      super(converter);
      if (values != null) {
         Object[] var6 = values;
         int var5 = values.length;

         for(int var4 = 0; var4 < var5; ++var4) {
            Object value = var6[var4];
            this.addItem(value);
         }

      }
   }

   public String getSettingsValue() {
      Object value = this.getSelectedValue();
      return this.convert(value);
   }

   public void setSettingsValue(String string) {
      Object value = this.convert(string);
      this.setSelectedValue(value);
   }

   public boolean isValueValid() {
      return true;
   }
}
