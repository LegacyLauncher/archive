package ru.turikhay.tlauncher.ui.editor;

public class EditorIntegerField extends EditorTextField {
   public EditorIntegerField() {
   }

   public EditorIntegerField(String prompt) {
      super(prompt);
   }

   public int getIntegerValue() {
      try {
         return Integer.parseInt(this.getSettingsValue());
      } catch (Exception var2) {
         return -1;
      }
   }

   public boolean isValueValid() {
      try {
         Integer.parseInt(this.getSettingsValue());
         return true;
      } catch (Exception var2) {
         return false;
      }
   }
}
