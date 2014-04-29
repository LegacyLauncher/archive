package ru.turikhay.tlauncher.ui.text;

import ru.turikhay.tlauncher.ui.center.CenterPanel;

public abstract class CheckableTextField extends ExtendedTextField {
   private static final long serialVersionUID = 2835507963141686372L;
   private CenterPanel parent;

   protected CheckableTextField(CenterPanel panel, String placeholder, String value) {
      super(panel, placeholder, value);
      this.parent = panel;
   }

   public CheckableTextField(String placeholder, String value) {
      this((CenterPanel)null, placeholder, value);
   }

   public CheckableTextField(String placeholder) {
      this((CenterPanel)null, placeholder, (String)null);
   }

   public CheckableTextField(CenterPanel panel) {
      this(panel, (String)null, (String)null);
   }

   boolean check() {
      String text = this.getValue();
      String result = this.check(text);
      return result == null ? this.setValid() : this.setInvalid(result);
   }

   public boolean setInvalid(String reason) {
      this.setBackground(this.getTheme().getFailure());
      this.setForeground(this.getTheme().getFocus());
      if (this.parent != null) {
         this.parent.setError(reason);
      }

      return false;
   }

   public boolean setValid() {
      this.setBackground(this.getTheme().getBackground());
      this.setForeground(this.getTheme().getFocus());
      if (this.parent != null) {
         this.parent.setError((String)null);
      }

      return true;
   }

   protected void updateStyle() {
      super.updateStyle();
      this.check();
   }

   protected void onChange() {
      this.check();
   }

   protected abstract String check(String var1);
}
