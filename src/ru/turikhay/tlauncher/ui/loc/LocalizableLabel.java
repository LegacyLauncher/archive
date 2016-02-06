package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;

public class LocalizableLabel extends ExtendedLabel implements LocalizableComponent {
   protected String path;
   protected String[] variables;

   public LocalizableLabel(String path, Object... vars) {
      this.init();
      this.setText(path, vars);
      this.setFont(this.getFont().deriveFont(TLauncherFrame.getFontSize()));
   }

   public LocalizableLabel(String path) {
      this(path, Localizable.EMPTY_VARS);
   }

   public LocalizableLabel() {
      this((String)null);
   }

   public void setText(String path, Object... vars) {
      this.path = path;
      this.variables = Localizable.checkVariables(vars);
      String value = Localizable.get(path);

      for(int i = 0; i < this.variables.length; ++i) {
         value = value.replace("%" + i, this.variables[i]);
      }

      this.setRawText(value);
   }

   protected void setRawText(String value) {
      super.setText(value);
   }

   public void setText(String path) {
      this.setText(path, Localizable.EMPTY_VARS);
   }

   public void updateLocale() {
      this.setText(this.path, this.variables);
   }

   protected void init() {
   }
}
