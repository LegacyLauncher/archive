package ru.turikhay.tlauncher.minecraft.auth;

public class InvalidCredentialsException extends KnownAuthenticatorException {
   InvalidCredentialsException() {
      super("Invalid user / password / token", "relogin");
   }
}
