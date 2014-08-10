package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.text.CheckableTextField;

public abstract class LocalizableCheckableTextField extends CheckableTextField implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
   private String placeholderPath;

   private LocalizableCheckableTextField(CenterPanel panel, String placeholderPath, String value) {
      super(panel, (String)null, (String)null);
      this.placeholderPath = placeholderPath;
      this.setValue(value);
   }

   public LocalizableCheckableTextField(CenterPanel panel, String placeholderPath) {
      this(panel, placeholderPath, (String)null);
   }

   public LocalizableCheckableTextField(String placeholderPath, String value) {
      this((CenterPanel)null, placeholderPath, value);
   }

   public LocalizableCheckableTextField(String placeholderPath) {
      this((CenterPanel)null, placeholderPath, (String)null);
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
