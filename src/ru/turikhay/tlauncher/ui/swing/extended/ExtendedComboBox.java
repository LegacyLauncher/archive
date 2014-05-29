package ru.turikhay.tlauncher.ui.swing.extended;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.ListCellRenderer;
import ru.turikhay.tlauncher.ui.converter.StringConverter;
import ru.turikhay.tlauncher.ui.swing.DefaultConverterCellRenderer;
import ru.turikhay.tlauncher.ui.swing.SimpleComboBoxModel;
import ru.turikhay.util.U;

public class ExtendedComboBox extends JComboBox {
   private static final long serialVersionUID = -4509947341182373649L;
   private StringConverter converter;

   public ExtendedComboBox(ListCellRenderer renderer) {
      this.setModel(new SimpleComboBoxModel());
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

   public SimpleComboBoxModel getSimpleModel() {
      return (SimpleComboBoxModel)this.getModel();
   }

   public Object getValueAt(int i) {
      Object value = this.getItemAt(i);
      return this.returnAs(value);
   }

   public Object getSelectedValue() {
      Object selected = this.getSelectedItem();
      return this.returnAs(selected);
   }

   public void setSelectedValue(Object value) {
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
