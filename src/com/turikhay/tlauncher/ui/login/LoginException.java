package com.turikhay.tlauncher.ui.login;

public class LoginException extends Exception {
   private static final long serialVersionUID = -1186718369369624107L;

   public LoginException(String reason) {
      super(reason);
   }
}
