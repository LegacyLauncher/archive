package com.turikhay.tlauncher.minecraft.auth;

public interface AuthenticatorListener {
   void onAuthPassing(Authenticator var1);

   void onAuthPassingError(Authenticator var1, Throwable var2);

   void onAuthPassed(Authenticator var1);
}
