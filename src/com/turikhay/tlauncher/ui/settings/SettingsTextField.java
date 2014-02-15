package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.loc.LocalizableTextField;

public class SettingsTextField extends LocalizableTextField implements SettingsField {
   private static final long serialVersionUID = 3920711425159165958L;
   private final boolean canBeEmpty;

   SettingsTextField(boolean canBeEmpty) {
      this.canBeEmpty = canBeEmpty;
   }

   SettingsTextField() {
      this(false);
   }

   SettingsTextField(String prompt, boolean canBeEmpty) {
      super(prompt);
      this.canBeEmpty = canBeEmpty;
   }

   SettingsTextField(String prompt) {
      this(prompt, false);
   }

   public String getSettingsValue() {
      return this.getValue();
   }

   public void setSettingsValue(String value) {
      this.setText(value);
   }

   public boolean isValueValid() {
      String text = this.getValue();
      return text != null || this.canBeEmpty;
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }
}
