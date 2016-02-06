package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableTextField;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;

public class EditorTextField extends BorderPanel implements EditorField {
   private final boolean canBeEmpty;
   public final LocalizableTextField textField;

   public EditorTextField(String prompt, boolean canBeEmpty) {
      this.canBeEmpty = canBeEmpty;
      this.textField = new LocalizableTextField(prompt);
      this.textField.setColumns(1);
      this.setCenter(this.textField);
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
      return this.textField.getValue();
   }

   public void setSettingsValue(String value) {
      this.textField.setText(value);
      this.textField.setCaretPosition(0);
   }

   public boolean isValueValid() {
      String text = this.textField.getValue();
      return text != null || this.canBeEmpty;
   }

   public void block(Object reason) {
      this.textField.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.textField.setEnabled(true);
   }
}
