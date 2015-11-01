package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.converter.StringConverter;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;

public class EditorComboBox extends ExtendedComboBox implements EditorField {
   private static final long serialVersionUID = -2320340434786516374L;
   private final boolean allowNull;

   public EditorComboBox(StringConverter converter, Object[] values, boolean allowNull) {
      super(converter);
      this.allowNull = allowNull;
      if (values != null) {
         Object[] var7 = values;
         int var6 = values.length;

         for(int var5 = 0; var5 < var6; ++var5) {
            Object value = var7[var5];
            this.addItem(value);
         }
      }

   }

   public EditorComboBox(StringConverter converter, Object[] values) {
      this(converter, values, false);
   }

   public String getSettingsValue() {
      Object value = this.getSelectedValue();
      return this.convert(value);
   }

   public void setSettingsValue(String string) {
      Object value = this.convert(string);
      if (!this.allowNull && string == null) {
         boolean hasNull = false;

         for(int i = 0; i < this.getItemCount(); ++i) {
            if (this.getItemAt(i) == null) {
               hasNull = true;
            }
         }

         if (!hasNull) {
            return;
         }
      }

      this.setSelectedValue(value);
   }

   public boolean isValueValid() {
      return true;
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }
}
