package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.converter.StringConverter;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;
import com.turikhay.util.U;

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
      if (string != null) {
         for(int i = 0; i < this.getItemCount(); ++i) {
            U.log(string, this.convert(this.getItemAt(i)), string.equals(this.convert(this.getItemAt(i))));
            if (string.equals(this.convert(this.getItemAt(i)))) {
               this.setSelectedIndex(i);
               return;
            }
         }

      }
   }

   public boolean isValueValid() {
      return true;
   }
}
