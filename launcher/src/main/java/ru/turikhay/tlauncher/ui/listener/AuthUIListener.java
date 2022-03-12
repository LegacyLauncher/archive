package ru.turikhay.tlauncher.ui.listener;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.login.LoginException;
import ru.turikhay.tlauncher.user.AuthDetailedException;
import ru.turikhay.tlauncher.user.AuthException;
import ru.turikhay.tlauncher.user.AuthUnknownException;
import ru.turikhay.tlauncher.user.User;
import ru.turikhay.util.SwingException;
import ru.turikhay.util.SwingUtil;

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
            try {
                SwingUtil.wait(() -> listener.onAuthPassingError(auth, e));
            } catch (SwingException swingException) {
                Throwable t = swingException.unpackException();
                if (t instanceof LoginException) {
                    throw (LoginException) t;
                }
                throw swingException;
            }
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
            TLauncher.getInstance().getProfileManager().saveProfiles();
        } catch (IOException var2) {
            Alert.showLocError("auth.profiles.save-error");
        }

    }
}
