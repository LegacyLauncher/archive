package ru.turikhay.tlauncher.ui.accounts;

import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableTextField;

public class UsernameField extends LocalizableTextField {
   private UsernameField.UsernameState state;

   public UsernameField(CenterPanel pan, UsernameField.UsernameState state) {
      super(pan, "account.username");
      this.setState(state);
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
   }
}
