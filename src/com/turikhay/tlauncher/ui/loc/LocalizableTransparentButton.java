package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.ui.TransparentButton;

public class LocalizableTransparentButton extends TransparentButton implements LocalizableComponent {
   private static final long serialVersionUID = -1357535949476677157L;
   private String path;
   private String[] variables;

   public LocalizableTransparentButton(String path, Object... vars) {
      this.setOpaque(false);
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
