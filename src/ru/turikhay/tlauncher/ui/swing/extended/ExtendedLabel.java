package ru.turikhay.tlauncher.ui.swing.extended;

import javax.swing.Icon;
import javax.swing.JLabel;

public class ExtendedLabel extends JLabel {
   private static final long serialVersionUID = -758117308854118352L;

   public ExtendedLabel(String text, Icon icon, int horizontalAlignment) {
      super(text, icon, horizontalAlignment);
      this.setFont(this.getFont().deriveFont(12.0F));
      this.setOpaque(false);
   }

   public ExtendedLabel(String text, int horizontalAlignment) {
      this(text, (Icon)null, horizontalAlignment);
   }

   public ExtendedLabel(String text) {
      this(text, (Icon)null, 10);
   }

   public ExtendedLabel(Icon image, int horizontalAlignment) {
      this((String)null, image, horizontalAlignment);
   }

   public ExtendedLabel(Icon image) {
      this((String)null, image, 0);
   }

   public ExtendedLabel() {
      this((String)null, (Icon)null, 10);
   }
}
