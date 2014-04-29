package ru.turikhay.tlauncher.ui.swing;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import ru.turikhay.tlauncher.ui.converter.StringConverter;

public class DefaultConverterCellRenderer extends ConverterCellRenderer {
   private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

   public DefaultConverterCellRenderer(StringConverter converter) {
      super(converter);
   }

   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel renderer = (JLabel)this.defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      renderer.setText(this.converter.toString(value));
      return renderer;
   }
}
