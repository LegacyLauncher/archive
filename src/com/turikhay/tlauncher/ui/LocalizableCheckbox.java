package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import javax.swing.JCheckBox;

public class LocalizableCheckbox extends JCheckBox implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
   static Settings l;
   private String path;

   public LocalizableCheckbox(String path) {
      this.init();
      this.setLabel(path);
   }

   public LocalizableCheckbox(String path, boolean state) {
      super("", state);
      this.init();
      this.setText(path);
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

   public boolean getState() {
      return super.getModel().isSelected();
   }

   public void setState(boolean state) {
      super.getModel().setSelected(state);
   }

   public void updateLocale() {
      this.setLabel(this.path);
   }

   private void init() {
      this.setOpaque(false);
   }
}
