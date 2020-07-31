package ru.turikhay.tlauncher.minecraft.auth;

import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.user.AuthException;
import ru.turikhay.util.async.AsyncThread;

import java.io.IOException;

public abstract class Authenticator {

    public abstract Account getAccount();
    public abstract Account.AccountType getType();

    private Exception e;

    public boolean pass(AuthenticatorListener l) {
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

    public void asyncPass(final AuthenticatorListener l) {
        AsyncThread.execute(new Runnable() {
            public void run() {
                pass(l);
            }
        });
    }

    protected abstract void pass() throws AuthException, IOException;

    public static ValidateAuthenticator instanceFor(Account account) {
        return new ValidateAuthenticator(account, AccountManager.getAuthFor(account.getUser()));
    }

    public static ExecAuthenticator instanceFor(AuthExecutor executor, Account.AccountType type) {
        return new ExecAuthenticator(executor, type);
    }
}
