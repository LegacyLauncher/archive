package ru.turikhay.tlauncher.ui.listener;

import java.io.IOException;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorException;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.minecraft.auth.ServiceUnavailableException;
import ru.turikhay.tlauncher.ui.alert.Alert;

public class AuthUIListener implements AuthenticatorListener {
   private final AuthenticatorListener listener;

   public AuthUIListener(AuthenticatorListener listener) {
      this.listener = listener;
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
      String description = "unknown";
      Object textarea = e;
      if (e instanceof AuthenticatorException) {
         AuthenticatorException ae = (AuthenticatorException)e;
         if (ae.getLangpath() != null) {
            description = ae.getLangpath();
         }

         if (e instanceof ServiceUnavailableException) {
            textarea = e.getMessage();
         } else {
            textarea = null;
         }
      }

      Alert.showLocError("auth.error.title", "auth.error." + description, textarea);
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
