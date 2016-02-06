package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.text.ExtendedTextField;

public class LocalizableTextField extends ExtendedTextField implements LocalizableComponent {
   protected String placeholderPath;
   protected String[] variables;

   public LocalizableTextField(CenterPanel panel, String placeholderPath, String value) {
      super(panel, (String)null, value);
      this.setValue(value);
      this.setPlaceholder(placeholderPath);
      this.setFont(this.getFont().deriveFont(TLauncherFrame.getFontSize()));
   }

   public LocalizableTextField(CenterPanel panel, String placeholderPath) {
      this(panel, placeholderPath, (String)null);
   }

   public LocalizableTextField(String placeholderPath) {
      this((CenterPanel)null, placeholderPath, (String)null);
   }

   public LocalizableTextField() {
      this((CenterPanel)null, (String)null, (String)null);
   }

   public void setPlaceholder(String placeholderPath, Object... vars) {
      this.placeholderPath = placeholderPath;
      this.variables = Localizable.checkVariables(vars);
      String value = Localizable.get(placeholderPath);

      for(int i = 0; i < this.variables.length; ++i) {
         value = value.replace("%" + i, this.variables[i]);
      }

      super.setPlaceholder(value);
   }

   public void setPlaceholder(String placeholderPath) {
      this.setPlaceholder(placeholderPath, Localizable.EMPTY_VARS);
   }

   public void updateLocale() {
      this.setPlaceholder(this.placeholderPath, this.variables);
   }
}
