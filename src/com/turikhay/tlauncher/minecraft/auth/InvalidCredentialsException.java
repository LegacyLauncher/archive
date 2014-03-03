package com.turikhay.tlauncher.minecraft.auth;

public class InvalidCredentialsException extends AuthenticatorException {
   private static final long serialVersionUID = 7221509839484990453L;

   InvalidCredentialsException() {
      super("Invalid user / password / token", "relogin");
   }
}
