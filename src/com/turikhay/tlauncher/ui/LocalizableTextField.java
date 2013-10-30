package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;

public abstract class LocalizableTextField extends ExtendedTextField implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
   static Settings l;
   private String placeholderPath;

   public LocalizableTextField() {
   }

   public LocalizableTextField(CenterPanel parentPanel, String placeholder, String text, int columns) {
      super(parentPanel, (String)null, text, columns);
      this.setPlaceholder(placeholder);
   }

   public LocalizableTextField(String placeholder, String text, int columns) {
      this((CenterPanel)null, placeholder, text, columns);
   }

   public LocalizableTextField(String text, int columns) {
      this((String)null, text, columns);
   }

   public LocalizableTextField(CenterPanel parentPanel, String placeholder, String text) {
      this(parentPanel, placeholder, text, text != null ? text.length() : (placeholder != null ? placeholder.length() : 0));
   }

   public LocalizableTextField(CenterPanel parentPanel, String placeholder) {
      this(parentPanel, placeholder, (String)null);
   }

   public LocalizableTextField(String placeholder) {
      this((CenterPanel)null, placeholder, (String)null);
   }

   public LocalizableTextField(CenterPanel parentPanel) {
      this(parentPanel, (String)null, (String)null);
   }

   public void setPlaceholder(String placeholderPath) {
      this.placeholderPath = placeholderPath;
      super.setPlaceholder(l == null ? placeholderPath : l.get(placeholderPath));
   }

   public String getPlaceholderPath() {
      return this.placeholderPath;
   }

   public void updateLocale() {
      super.updateLocale();
      this.setPlaceholder(this.placeholderPath);
   }
}
