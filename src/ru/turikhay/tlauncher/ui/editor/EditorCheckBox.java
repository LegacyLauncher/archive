package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;

public class EditorCheckBox extends LocalizableCheckbox implements EditorField {
   public EditorCheckBox(String path) {
      super(path);
   }

   public String getSettingsValue() {
      return this.isSelected() ? "true" : "false";
   }

   public void setSettingsValue(String value) {
      this.setSelected(Boolean.parseBoolean(value));
   }

   public boolean isValueValid() {
      return true;
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }
}
