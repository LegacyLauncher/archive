package ru.turikhay.tlauncher.minecraft.auth;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.user.Auth;
import ru.turikhay.tlauncher.user.AuthException;
import ru.turikhay.util.U;

import java.io.IOException;

public class ValidateAuthenticator extends Authenticator {
    private final Account account;
    private final Auth auth;

    ValidateAuthenticator(Account account, Auth auth) {
        this.account = U.requireNotNull(account, "account");
        this.auth = U.requireNotNull(auth, "auth");
    }

    public final Account getAccount() {
        return account;
    }

    @Override
    public Account.AccountType getType() {
        return account.getType();
    }

    protected void pass() throws AuthException, IOException {
        auth.validate(account.getUser());
    }
}
