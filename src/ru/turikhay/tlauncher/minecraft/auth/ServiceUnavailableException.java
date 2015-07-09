package ru.turikhay.tlauncher.minecraft.auth;

public class ServiceUnavailableException extends AuthenticatorException {
   ServiceUnavailableException(String message) {
      super(message, "unavailable");
   }
}
