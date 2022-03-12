package ru.turikhay.tlauncher.minecraft.auth;

import ru.turikhay.tlauncher.user.Auth;
import ru.turikhay.tlauncher.user.AuthException;
import ru.turikhay.tlauncher.user.User;

import java.io.IOException;
import java.util.Objects;

public class ValidateAuthenticator<U extends User> extends Authenticator<U> {
    private final Account<U> account;
    private final Auth<U> auth;

    ValidateAuthenticator(Account<U> account, Auth<U> auth) {
        this.account = Objects.requireNonNull(account, "account");
        this.auth = Objects.requireNonNull(auth, "auth");
    }

    public final Account<U> getAccount() {
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
