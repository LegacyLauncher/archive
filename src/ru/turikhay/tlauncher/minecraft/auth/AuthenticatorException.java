package ru.turikhay.tlauncher.minecraft.auth;

public class AuthenticatorException extends Exception {
   private static final long serialVersionUID = -6773418626800336871L;
   private String langpath;

   AuthenticatorException(String message) {
      super(message);
   }

   AuthenticatorException(Throwable cause) {
      super(cause);
   }

   AuthenticatorException(String message, String langpath) {
      super(message);
      this.langpath = langpath;
   }

   AuthenticatorException(String message, String langpath, Throwable cause) {
      super(message, cause);
      this.langpath = langpath;
   }

   public String getLangpath() {
      return this.langpath;
   }
}
