package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.util.Range;

public class EditorIntegerRangeField extends EditorIntegerField {
   private final Range range;

   public EditorIntegerRangeField(Range range) {
      if (range == null) {
         throw new NullPointerException("range");
      } else {
         this.range = range;
         this.setPlaceholder("settings.range", new Object[]{range.getMinValue(), range.getMaxValue()});
      }
   }

   public boolean isValueValid() {
      try {
         return this.range.fits(Integer.parseInt(this.getSettingsValue()));
      } catch (Exception var2) {
         return false;
      }
   }
}
