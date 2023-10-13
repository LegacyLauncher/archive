package net.legacylauncher.minecraft.auth;

import net.legacylauncher.managers.AccountManager;
import net.legacylauncher.user.Auth;
import net.legacylauncher.user.AuthException;
import net.legacylauncher.user.User;
import net.legacylauncher.util.async.AsyncThread;

import java.io.IOException;

public abstract class Authenticator<U extends User> {

    public abstract Account<U> getAccount();

    public abstract Account.AccountType getType();

    private Exception e;

    public boolean pass(AuthenticatorListener<? super U> l) {
        if (l != null) {
            l.onAuthPassing(this);
        }

        try {
            pass();
        } catch (Exception var3) {
            e = var3;
            if (l != null) {
                l.onAuthPassingError(this, var3);
            }
            return false;
        }

        if (l != null) {
            l.onAuthPassed(this);
        }

        return true;
    }

    public Exception getException() {
        return e;
    }

    public void asyncPass(final AuthenticatorListener<U> l) {
        AsyncThread.execute(() -> pass(l));
    }

    protected abstract void pass() throws AuthException, IOException;

    @SuppressWarnings("unchecked")
    public static <U extends User> ValidateAuthenticator<U> instanceFor(Account<U> account) {
        return new ValidateAuthenticator<>(account, (Auth<U>) AccountManager.getAuthFor(account.getUser()));
    }

    public static <U extends User> ExecAuthenticator<U> instanceFor(AuthExecutor<U> executor, Account.AccountType type) {
        return new ExecAuthenticator<>(executor, type);
    }
}
