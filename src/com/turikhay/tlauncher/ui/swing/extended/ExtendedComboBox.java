package com.turikhay.tlauncher.ui.swing.extended;

import com.turikhay.tlauncher.ui.converter.StringConverter;
import com.turikhay.util.U;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.ListCellRenderer;

public class ExtendedComboBox extends JComboBox {
   private static final long serialVersionUID = -4509947341182373649L;
   private StringConverter converter;

   public ExtendedComboBox(ListCellRenderer renderer) {
      this.setRenderer(renderer);
      this.setOpaque(false);
      this.setFont(this.getFont().deriveFont(12.0F));
      ((JComponent)U.getAs(this.getEditor().getEditorComponent(), JComponent.class)).setOpaque(false);
   }

   public ExtendedComboBox(StringConverter converter) {
      this((ListCellRenderer)(new DefaultConverterCellRenderer(converter)));
      this.converter = converter;
   }

   public ExtendedComboBox() {
      this((ListCellRenderer)null);
   }

   public Object getValueAt(int i) {
      Object value = this.getItemAt(i);
      return this.returnAs(value);
   }

   protected Object getSelectedValue() {
      Object selected = this.getSelectedItem();
      return this.returnAs(selected);
   }

   protected void setSelectedValue(Object value) {
      this.setSelectedItem(value);
   }

   public void setSelectedValue(String string) {
      Object value = this.convert(string);
      if (value != null) {
         this.setSelectedValue(value);
      }
   }

   public StringConverter getConverter() {
      return this.converter;
   }

   public void setConverter(StringConverter converter) {
      this.converter = converter;
   }

   protected String convert(Object obj) {
      Object from = this.returnAs(obj);
      if (this.converter != null) {
         return this.converter.toValue(from);
      } else {
         return from == null ? null : from.toString();
      }
   }

   protected Object convert(String from) {
      return this.converter == null ? null : this.converter.fromString(from);
   }

   private Object returnAs(Object obj) {
      try {
         return obj;
      } catch (ClassCastException var3) {
         return null;
      }
   }
}
