package ru.turikhay.tlauncher.minecraft.auth;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import java.util.UUID;

public abstract class Authenticator {
    protected final Account account;
    private final String logPrefix = '[' + getClass().getSimpleName() + ']';

    protected Authenticator(Account account) {
        if (account == null) {
            throw new NullPointerException("account");
        } else {
            this.account = account;
        }
    }

    public final Account getAccount() {
        return account;
    }

    public boolean pass(AuthenticatorListener l) {
        if (l != null) {
            l.onAuthPassing(this);
        }

        try {
            pass();
        } catch (Exception var3) {
            log("Cannot authenticate:", var3);
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

    public void asyncPass(final AuthenticatorListener l) {
        AsyncThread.execute(new Runnable() {
            public void run() {
                pass(l);
            }
        });
    }

    protected abstract void pass() throws AuthenticatorException;

    protected void log(Object... o) {
        U.log(logPrefix, o);
    }

    public static Authenticator instanceFor(Account account) {
        if (account == null) {
            throw new NullPointerException("account");
        } else {
            U.log("instancefor:", account);
            switch (account.getType()) {
                case ELY:
                    return new ElyAuthenticator(account);
                case MOJANG:
                    return new MojangAuthenticator(account);
                default:
                    throw new IllegalArgumentException("illegal account type");
            }
        }
    }

    protected static UUID getClientToken() {
        return TLauncher.getInstance().getProfileManager().getClientToken();
    }

    protected static void setClientToken(String uuid) {
        TLauncher.getInstance().getProfileManager().setClientToken(uuid);
    }
}