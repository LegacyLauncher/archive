package ru.turikhay.tlauncher.minecraft.auth;

public class ElyAuthenticator extends StandardAuthenticator {
   public ElyAuthenticator(Account account) {
      super(account, "https://authserver.ely.by/auth/authenticate", "https://authserver.ely.by/auth/refresh");
   }

   protected AuthenticatorException getException(StandardAuthenticator.Response result) {
      if ("ForbiddenOperationException".equals(result.getError()) && "This account has been suspended.".equals(result.getErrorMessage())) {
         return new UserBannedException();
      } else {
         AuthenticatorException exception = super.getException(result);
         if (!exception.getClass().equals(AuthenticatorException.class)) {
            return exception;
         } else {
            return (AuthenticatorException)("ServiceTemporarilyUnavailableException".equals(result.getError()) ? new ServiceUnavailableException(result.getErrorMessage()) : exception);
         }
      }
   }
}
