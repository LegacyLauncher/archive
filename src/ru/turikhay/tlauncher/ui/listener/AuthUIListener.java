package ru.turikhay.tlauncher.ui.listener;

import java.io.IOException;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorException;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.minecraft.auth.InvalidCredentialsException;
import ru.turikhay.tlauncher.minecraft.auth.ServiceUnavailableException;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;

public class AuthUIListener implements AuthenticatorListener {
   private final AuthenticatorListener listener;
   public boolean editorOpened = false;

   public AuthUIListener(AuthenticatorListener listener) {
      this.listener = listener;
   }

   public void onAuthPassing(Authenticator auth) {
      if (this.listener != null) {
         this.listener.onAuthPassing(auth);
      }

   }

   public void onAuthPassingError(Authenticator auth, Throwable e) {
      this.showError(auth, e);
      if (this.listener != null) {
         this.listener.onAuthPassingError(auth, e);
      }

   }

   private void showError(Authenticator auth, Throwable e) {
      String description = "unknown";
      Object textarea = e;
      if (e instanceof AuthenticatorException) {
         AuthenticatorException ae = (AuthenticatorException)e;
         if (ae.getLangpath() != null) {
            description = ae.getLangpath();
         }

         if (e instanceof ServiceUnavailableException) {
            String var7 = e.getMessage();
         }

         if (e instanceof InvalidCredentialsException) {
            if (description != null) {
               description = description + "." + auth.getAccount().getType().toString().toLowerCase();
            }

            textarea = null;
         } else {
            textarea = e;
         }
      }

      String text = Localizable.get("auth.error." + description + ".editor");
      if (!this.editorOpened || text == null) {
         text = Localizable.get("auth.error." + description);
         if (text == null) {
            text = "auth.error." + description;
         }
      }

      Alert.showError(Localizable.get("auth.error.title"), text, textarea);
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
