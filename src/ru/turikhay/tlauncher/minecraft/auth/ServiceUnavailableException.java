package ru.turikhay.tlauncher.minecraft.auth;

public class ServiceUnavailableException extends KnownAuthenticatorException {
   ServiceUnavailableException(String message) {
      super(message, "unavailable");
   }
}
