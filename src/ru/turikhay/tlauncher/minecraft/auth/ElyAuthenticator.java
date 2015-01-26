package ru.turikhay.tlauncher.minecraft.auth;

public class ElyAuthenticator extends StandardAuthenticator {
   private static final String ELY_URL = "http://minecraft.ely.by/auth/";

   public ElyAuthenticator(Account account) {
      super(account, "http://minecraft.ely.by/auth/authenticate", "http://minecraft.ely.by/auth/refresh");
   }

   protected void pass() throws AuthenticatorException {
      super.pass();
   }
}
