package ru.turikhay.tlauncher.minecraft.auth;

public class ElyAuthenticator extends StandardAuthenticator {
   private static final String ELY_URL = "http://minecraft.ely.by/auth/";

   public ElyAuthenticator(Account account) {
      super(account, "http://minecraft.ely.by/auth/authenticate", "http://minecraft.ely.by/auth/refresh");
   }

   protected AuthenticatorException getException(StandardAuthenticator.Response result) {
      AuthenticatorException exception = super.getException(result);
      return (AuthenticatorException)(exception.getClass().equals(AuthenticatorException.class) && "ServiceUnavailableException".equals(result.getError()) ? new ServiceUnavailableException(result.getErrorMessage()) : exception);
   }
}
