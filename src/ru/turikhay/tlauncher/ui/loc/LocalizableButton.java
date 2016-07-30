package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;

public class LocalizableButton extends ExtendedButton implements LocalizableComponent {
   private String path;
   private String[] variables;
   private String hint;
   private String[] hintVars;

   public LocalizableButton() {
      this.variables = Localizable.checkVariables(Localizable.EMPTY_VARS);
      this.hintVars = Localizable.checkVariables(Localizable.EMPTY_VARS);
   }

   public LocalizableButton(ImageIcon icon, String hint) {
      this();
      this.setIcon(icon);
      this.setToolTipText(hint);
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
      super.setText(Localizable.get(path, vars));
   }

   public void setText(String path) {
      this.setText(path, Localizable.EMPTY_VARS);
   }

   public void setToolTipText(String hint, Object... vars) {
      this.hint = hint;
      this.hintVars = Localizable.checkVariables(vars);
      super.setToolTipText(Localizable.get(hint, vars));
   }

   public void setToolTipText(String hint) {
      this.setToolTipText(hint, Localizable.EMPTY_VARS);
   }

   public void updateLocale() {
      this.setText(this.path, this.variables);
      this.setToolTipText(this.hint, this.hintVars);
   }
}
