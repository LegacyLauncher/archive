package ru.turikhay.tlauncher.minecraft.auth;

import ru.turikhay.tlauncher.user.AuthException;
import ru.turikhay.tlauncher.user.User;

import java.io.IOException;
import java.util.Objects;

public class ExecAuthenticator<U extends User> extends Authenticator<U> {
    private final AuthExecutor<U> executor;
    private final Account.AccountType type;
    private Account<U> account;

    public ExecAuthenticator(AuthExecutor<U> executor, Account.AccountType type) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public Account<U> getAccount() {
        return account;
    }

    public Account.AccountType getType() {
        return type;
    }

    @Override
    protected void pass() throws AuthException, IOException {
        account = executor.pass();
    }
}
