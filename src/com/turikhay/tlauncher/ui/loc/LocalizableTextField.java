package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.text.ExtendedTextField;

public abstract class LocalizableTextField extends ExtendedTextField implements LocalizableComponent {
   private static final long serialVersionUID = 359096767189321072L;
   private String placeholderPath;

   public LocalizableTextField(CenterPanel panel, String placeholderPath, String value) {
      super(panel, (String)null, value);
      this.setValue(value);
      this.setPlaceholder(placeholderPath);
   }

   public LocalizableTextField(CenterPanel panel, String placeholderPath) {
      super(panel, placeholderPath, (String)null);
   }

   public void setPlaceholder(String placeholderPath) {
      this.placeholderPath = placeholderPath;
      super.setPlaceholder(Localizable.get() == null ? placeholderPath : Localizable.get().get(placeholderPath));
   }

   public String getPlaceholderPath() {
      return this.placeholderPath;
   }

   public void updateLocale() {
      this.setPlaceholder(this.placeholderPath);
   }
}
