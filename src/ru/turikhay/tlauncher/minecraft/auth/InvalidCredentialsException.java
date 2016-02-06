package ru.turikhay.tlauncher.minecraft.auth;

public class InvalidCredentialsException extends AuthenticatorException {
   InvalidCredentialsException() {
      super("Invalid user / password / token", "relogin");
   }
}
