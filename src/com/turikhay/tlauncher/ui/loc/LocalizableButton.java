package com.turikhay.tlauncher.ui.loc;

import javax.swing.JButton;

public class LocalizableButton extends JButton implements LocalizableComponent {
   private static final long serialVersionUID = 1073130908385613323L;
   private String path;
   private String[] variables;

   public LocalizableButton(String path, Object... vars) {
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
