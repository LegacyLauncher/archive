package ru.turikhay.tlauncher.minecraft.auth;

public class UserBannedException extends KnownAuthenticatorException {
   UserBannedException() {
      super("This account has been suspended", "banned");
   }
}
