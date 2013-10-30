package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;

public class LocalizableCheckbox extends Checkbox implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
   static Settings l;
   private String path;

   public LocalizableCheckbox(String path) {
      this.setLabel(path);
   }

   public LocalizableCheckbox(String path, boolean state) {
      super("", state);
      this.setLabel(path);
   }

   public LocalizableCheckbox(String path, boolean state, CheckboxGroup group) {
      super("", state, group);
      this.setLabel(path);
   }

   public LocalizableCheckbox(String path, CheckboxGroup group, boolean state) {
      this(path, state, group);
   }

   public void setLabel(String path) {
      this.path = path;
      super.setLabel(l == null ? path : l.get(path));
   }

   public String getLangPath() {
      return this.path;
   }

   public void updateLocale() {
      this.setLabel(this.path);
   }
}
