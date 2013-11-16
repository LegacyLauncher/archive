package com.turikhay.tlauncher.ui;

public class UsernameField extends LocalizableTextField {
   private static final long serialVersionUID = -5813187607562947592L;
   String username;

   UsernameField(CenterPanel pan) {
      super(pan, "profile.username", (String)null, 20);
   }

   public boolean check() {
      return this.check(true);
   }

   protected boolean check(String text) {
      return this.check(text, true);
   }

   public boolean check(boolean canBeEmpty) {
      String text = this.getValue();
      return this.check(text, canBeEmpty) ? this.ok() : this.wrong(l.get("username.incorrect"));
   }

   protected boolean check(String text, boolean canBeEmpty) {
      if (text == null) {
         return false;
      } else {
         String regexp = "^[A-Za-z0-9_|\\-|\\.]" + (canBeEmpty ? "*" : "+") + "$";
         if (text.matches(regexp)) {
            this.username = text;
            return true;
         } else {
            return false;
         }
      }
   }
}
