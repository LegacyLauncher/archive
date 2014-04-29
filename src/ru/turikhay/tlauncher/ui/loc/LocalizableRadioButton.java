package ru.turikhay.tlauncher.ui.loc;

import java.awt.event.ItemListener;
import javax.swing.JRadioButton;

public class LocalizableRadioButton extends JRadioButton implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
   private String path;

   public LocalizableRadioButton() {
      this.init();
   }

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

   public String getLangPath() {
      return this.path;
   }

   public void addListener(ItemListener l) {
      super.getModel().addItemListener(l);
   }

   public void removeListener(ItemListener l) {
      super.getModel().removeItemListener(l);
   }

   public void updateLocale() {
      this.setLabel(this.path);
   }

   private void init() {
      this.setOpaque(false);
   }
}
