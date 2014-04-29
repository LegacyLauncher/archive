package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableTextField;

public class EditorTextField extends LocalizableTextField implements EditorField {
   private static final long serialVersionUID = 3920711425159165958L;
   private final boolean canBeEmpty;

   public EditorTextField(String prompt, boolean canBeEmpty) {
      super(prompt);
      this.canBeEmpty = canBeEmpty;
      this.setColumns(1);
   }

   public EditorTextField(String prompt) {
      this(prompt, false);
   }

   public EditorTextField(boolean canBeEmpty) {
      this((String)null, canBeEmpty);
   }

   public EditorTextField() {
      this(false);
   }

   public String getSettingsValue() {
      return this.getValue();
   }

   public void setSettingsValue(String value) {
      this.setText(value);
      this.setCaretPosition(0);
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
