package ru.turikhay.tlauncher.ui.editor;

public class EditorIntegerField extends EditorTextField {
   private static final long serialVersionUID = -7930510655707946312L;

   public EditorIntegerField() {
   }

   public EditorIntegerField(String prompt) {
      super(prompt);
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
