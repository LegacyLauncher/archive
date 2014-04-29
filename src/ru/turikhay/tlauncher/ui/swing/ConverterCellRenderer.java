package ru.turikhay.tlauncher.ui.swing;

import javax.swing.ListCellRenderer;
import ru.turikhay.tlauncher.ui.converter.StringConverter;

public abstract class ConverterCellRenderer implements ListCellRenderer {
   protected final StringConverter converter;

   ConverterCellRenderer(StringConverter converter) {
      if (converter == null) {
         throw new NullPointerException();
      } else {
         this.converter = converter;
      }
   }

   public StringConverter getConverter() {
      return this.converter;
   }
}
