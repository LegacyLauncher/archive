package ru.turikhay.tlauncher.ui.listener;

import java.io.IOException;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorException;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.minecraft.auth.KnownAuthenticatorException;
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
      String description = null;
      Object textarea = e;
      if (e instanceof AuthenticatorException) {
         AuthenticatorException ae = (AuthenticatorException)e;
         if (ae.getLangpath() != null) {
            description = ae.getLangpath();
         }

         if (e instanceof KnownAuthenticatorException) {
            textarea = null;
            if (e instanceof ServiceUnavailableException) {
               textarea = e.getMessage();
            }
         }
      }

      if (description == null) {
         description = "unknown";
      }

      String accountType = auth.getAccount().getType().toString().toLowerCase();
      String text = null;
      if (this.editorOpened) {
         text = Localizable.nget("auth.error." + description + "." + accountType + ".editor");
         if (text == null) {
            text = Localizable.nget("auth.error." + description + ".editor");
         }
      }

      if (text == null) {
         text = Localizable.nget("auth.error." + description + "." + accountType);
         if (text == null) {
            text = Localizable.nget("auth.error." + description);
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
