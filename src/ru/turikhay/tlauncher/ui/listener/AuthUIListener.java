package ru.turikhay.tlauncher.ui.listener;

import java.io.IOException;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorException;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.ui.alert.Alert;

public class AuthUIListener implements AuthenticatorListener {
   private final AuthenticatorListener listener;
   private final boolean showErrorOnce;
   private boolean errorShown;

   public AuthUIListener(boolean showErrorOnce, AuthenticatorListener listener) {
      this.listener = listener;
      this.showErrorOnce = showErrorOnce;
   }

   public void onAuthPassing(Authenticator auth) {
      if (this.listener != null) {
         this.listener.onAuthPassing(auth);
      }
   }

   public void onAuthPassingError(Authenticator auth, Throwable e) {
      this.showError(e);
      if (this.listener != null) {
         this.listener.onAuthPassingError(auth, e);
      }

   }

   private void showError(Throwable e) {
      boolean serious = true;
      String langpath = "unknown";
      if (e instanceof AuthenticatorException) {
         Throwable cause = e.getCause();
         if (cause instanceof IOException) {
            serious = false;
         }

         if (this.showErrorOnce && this.errorShown && !serious) {
            return;
         }

         AuthenticatorException ae = (AuthenticatorException)e;
         langpath = ae.getLangpath() == null ? "unknown" : ae.getLangpath();
         e = null;
      }

      Alert.showLocError("auth.error.title", "auth.error." + langpath, e);
      if (!serious) {
         this.errorShown = true;
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
         Alert.showLocError("auth.profiles.save-error");
      }

   }
}
