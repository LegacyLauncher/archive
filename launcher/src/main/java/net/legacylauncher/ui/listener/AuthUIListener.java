package net.legacylauncher.ui.listener;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.minecraft.auth.Authenticator;
import net.legacylauncher.minecraft.auth.AuthenticatorListener;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.user.AuthDetailedException;
import net.legacylauncher.user.AuthException;
import net.legacylauncher.user.AuthUnknownException;
import net.legacylauncher.user.User;
import net.legacylauncher.util.SwingUtil;

import java.io.IOException;

public class AuthUIListener<U extends User> implements AuthenticatorListener<U> {
    private final AuthenticatorListener<U> listener;

    public boolean editorOpened = false;

    public AuthUIListener(AuthenticatorListener<U> listener) {
        this.listener = listener;
    }

    @Override
    public void onAuthPassing(Authenticator<? extends U> auth) {
        if (listener != null) {
            SwingUtil.wait(() -> listener.onAuthPassing(auth));
        }
    }

    public void onAuthPassingError(Authenticator<? extends U> auth, Throwable e) {
        showError(auth, e);
        if (listener != null) {
            SwingUtil.wait(() -> listener.onAuthPassingError(auth, e));
        }
    }

    private void showError(Authenticator<? extends U> auth, Throwable e) {
        String title = "account.manager.error.title", locPath = "unknown";
        Object[] locVars = null;
        Object textarea = null;

        if (e instanceof IOException) {
            locPath = "ioe";
            textarea = e;
        }
        if (e instanceof AuthException) {
            locPath = ((AuthException) e).getLocPath();
            locVars = ((AuthException) e).getLocVars();
            if (e instanceof AuthUnknownException) {
                textarea = e;
            } else if (e instanceof AuthDetailedException) {
                textarea = ((AuthDetailedException) e).getErrorContent();
            } else if (e.getCause() != null) {
                textarea = e.getCause().toString();
            }
        }

        Account.AccountType accountType = auth.getType();
        String path, description;

        path = "account.manager.error." + accountType.toString().toLowerCase(java.util.Locale.ROOT) + "." + locPath + (editorOpened ? ".editor" : "");
        if (editorOpened && Localizable.nget(path) == null) {
            // try without ".editor"
            path = "account.manager.error." + accountType.toString().toLowerCase(java.util.Locale.ROOT) + "." + locPath;
        }
        if (Localizable.nget(path) == null) {
            path = "account.manager.error." + locPath + (editorOpened ? ".editor" : "");
        }
        if (editorOpened && Localizable.nget(path) == null) {
            path = "account.manager.error." + locPath;
        }
        description = Localizable.get(path, locVars);

        Alert.showLocError(title, description, textarea);
    }

    public void onAuthPassed(Authenticator<? extends U> auth) {
        if (listener != null) {
            SwingUtil.wait(() -> listener.onAuthPassed(auth));
        }

        saveProfiles();
    }

    public void saveProfiles() {
        try {
            LegacyLauncher.getInstance().getProfileManager().saveProfiles();
        } catch (IOException var2) {
            Alert.showLocError("auth.profiles.save-error");
        }

    }
}
