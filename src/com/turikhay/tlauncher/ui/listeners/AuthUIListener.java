package com.turikhay.tlauncher.ui.listeners;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorException;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.loc.Localizable;
import java.io.IOException;

public class AuthUIListener implements AuthenticatorListener {
   private final AuthenticatorListener listener;

   public AuthUIListener(AuthenticatorListener listener) {
      this.listener = listener;
   }

   public AuthUIListener() {
      this.listener = null;
   }

   public void onAuthPassing(Authenticator auth) {
      if (this.listener != null) {
         this.listener.onAuthPassing(auth);
      }
   }

   public void onAuthPassingError(Authenticator auth, Throwable e) {
      this.onAuthPassingError(auth, e, true);
   }

   public void onAuthPassingError(Authenticator auth, Throwable e, boolean showError) {
      if (this.listener != null) {
         this.listener.onAuthPassingError(auth, e);
      }

      if (showError) {
         String langpath = "unknown";
         if (e instanceof AuthenticatorException) {
            AuthenticatorException ae = (AuthenticatorException)e;
            langpath = ae.getLangpath() == null ? "unknown" : ae.getLangpath();
            e = null;
         }

         Alert.showError(Localizable.get("auth.error.title"), Localizable.get("auth.error." + langpath), e);
      }
   }

   public void onAuthPassed(Authenticator auth) {
      if (this.listener != null) {
         this.listener.onAuthPassed(auth);
      }

      this.saveProfiles();
   }

   public void saveProfiles() {
      try {
         TLauncher.getInstance().getProfileManager().saveProfiles();
      } catch (IOException var2) {
         Alert.showError("auth.profiles.save-error");
      }

   }
}
