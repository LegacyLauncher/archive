package ru.turikhay.tlauncher.minecraft.auth;

public class KnownAuthenticatorException extends AuthenticatorException {
   KnownAuthenticatorException(String message, String langpath) {
      super(message, langpath);
   }
}
