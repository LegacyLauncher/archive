package com.turikhay.tlauncher.ui.swing.extended;

import com.turikhay.tlauncher.ui.converter.StringConverter;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.ListCellRenderer;

public class ExtendedComboBox extends JComboBox {
   private static final long serialVersionUID = -4509947341182373649L;
   private StringConverter converter;

   public ExtendedComboBox(ListCellRenderer renderer) {
      this.setRenderer(renderer);
      this.setOpaque(false);
      ((JComponent)this.getEditor().getEditorComponent()).setOpaque(false);
   }

   public ExtendedComboBox(StringConverter converter) {
      this((ListCellRenderer)(new DefaultConverterCellRenderer(converter)));
      this.converter = converter;
   }

   public ExtendedComboBox() {
      this((ListCellRenderer)null);
   }

   public Object getSelectedValue() {
      Object selected = this.getSelectedItem();

      try {
         return selected;
      } catch (ClassCastException var3) {
         return null;
      }
   }

   public void setSelectedValue(Object value) {
      this.setSelectedItem(value);
   }

   public void setSelectedValue(String string) {
      Object value = this.convert(string);
      if (value != null) {
         this.setSelectedItem(value);
      }
   }

   public StringConverter getConverter() {
      return this.converter;
   }

   public void setConverter(StringConverter converter) {
      this.converter = converter;
   }

   protected String convert(Object from) {
      if (this.converter != null) {
         return this.converter.toValue(from);
      } else {
         return from == null ? null : from.toString();
      }
   }

   protected Object convert(String from) {
      return this.converter != null ? this.converter.fromString(from) : null;
   }
}
