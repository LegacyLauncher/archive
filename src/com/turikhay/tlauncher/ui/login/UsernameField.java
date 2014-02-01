package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.loc.LocalizableTextField;

public class UsernameField extends LocalizableTextField {
   private static final long serialVersionUID = -5813187607562947592L;
   UsernameField.UsernameState state;
   String username;

   public UsernameField(CenterPanel pan, UsernameField.UsernameState state) {
      super(pan, "account.username");
      this.setState(state);
   }

   public UsernameField.UsernameState getState() {
      return this.state;
   }

   public void setState(UsernameField.UsernameState state) {
      if (state == null) {
         throw new NullPointerException();
      } else {
         this.state = state;
         this.setPlaceholder(state.placeholder);
      }
   }

   public static enum UsernameState {
      USERNAME("account.username"),
      EMAIL("account.email");

      private final String placeholder;

      private UsernameState(String placeholder) {
         this.placeholder = placeholder;
      }

      public String getPlaceholder() {
         return this.placeholder;
      }
   }
}
