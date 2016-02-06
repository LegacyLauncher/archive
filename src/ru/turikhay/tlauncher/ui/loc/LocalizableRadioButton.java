package ru.turikhay.tlauncher.ui.loc;

import javax.swing.JRadioButton;

public class LocalizableRadioButton extends JRadioButton implements LocalizableComponent {
   private String path;

   public LocalizableRadioButton(String path) {
      this.init();
      this.setLabel(path);
   }

   /** @deprecated */
   @Deprecated
   public void setLabel(String path) {
      this.setText(path);
   }

   public void setText(String path) {
      this.path = path;
      super.setText(Localizable.get() == null ? path : Localizable.get().get(path));
   }

   public void updateLocale() {
      this.setLabel(this.path);
   }

   private void init() {
      this.setOpaque(false);
   }
}
