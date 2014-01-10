package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.event.ItemListener;
import javax.swing.JRadioButton;

public class LocalizableRadioButton extends JRadioButton implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
   static Settings l;
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
      super.setText(l == null ? path : l.get(path));
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
