package com.turikhay.tlauncher.ui.swing.extended;

import com.turikhay.tlauncher.ui.converter.StringConverter;
import javax.swing.ListCellRenderer;

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
