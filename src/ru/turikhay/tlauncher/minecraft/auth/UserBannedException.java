package ru.turikhay.tlauncher.minecraft.auth;

public class UserBannedException extends AuthenticatorException {
   UserBannedException() {
      super("This account has been suspended", "banned");
   }
}
