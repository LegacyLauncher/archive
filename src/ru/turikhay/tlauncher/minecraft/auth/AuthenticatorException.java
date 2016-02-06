package ru.turikhay.tlauncher.minecraft.auth;

public class AuthenticatorException extends Exception {
   private StandardAuthenticator.Response response;
   private String langpath;

   AuthenticatorException(Throwable cause) {
      super(cause);
   }

   AuthenticatorException(String message, String langpath) {
      super(message);
      this.langpath = langpath;
   }

   AuthenticatorException(StandardAuthenticator.Response response, String langpath) {
      super(response.getErrorMessage());
      this.response = response;
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
