package ru.turikhay.tlauncher.ui.listener;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.*;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;

import java.io.IOException;

public class AuthUIListener implements AuthenticatorListener {
    private final AuthenticatorListener listener;

    public boolean editorOpened = false;

    public AuthUIListener(AuthenticatorListener listener) {
        this.listener = listener;
    }

    public void onAuthPassing(Authenticator auth) {
        if (listener != null) {
            listener.onAuthPassing(auth);
        }
    }

    public void onAuthPassingError(Authenticator auth, Throwable e) {
        showError(auth, e);
        if (listener != null) {
            listener.onAuthPassingError(auth, e);
        }

    }

    private void showError(Authenticator auth, Throwable e) {
        String description = null;
        Object textarea = e;

        if (e instanceof AuthenticatorException) {
            AuthenticatorException ae = (AuthenticatorException) e;

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

        if (editorOpened) {
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
        if (listener != null) {
            listener.onAuthPassed(auth);
        }

        saveProfiles();
    }

    public void saveProfiles() {
        try {
            TLauncher.getInstance().getProfileManager().saveProfiles();
        } catch (IOException var2) {
            Alert.showLocError("auth.profiles.save-error");
        }

    }
}
