package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.ui.swing.extended.ExtendedButton;

public class LocalizableButton extends ExtendedButton implements LocalizableComponent {
   private static final long serialVersionUID = 1073130908385613323L;
   private String path;
   private String[] variables;

   protected LocalizableButton() {
   }

   public LocalizableButton(String path) {
      this();
      this.setText(path);
   }

   public LocalizableButton(String path, Object... vars) {
      this();
      this.setText(path, vars);
   }

   public void setText(String path, Object... vars) {
      this.path = path;
      this.variables = Localizable.checkVariables(vars);
      String value = Localizable.get(path);

      for(int i = 0; i < this.variables.length; ++i) {
         value = value.replace("%" + i, this.variables[i]);
      }

      super.setText(value);
   }

   public void setText(String path) {
      this.setText(path, Localizable.EMPTY_VARS);
   }

   public void updateLocale() {
      this.setText(this.path, this.variables);
   }
}
