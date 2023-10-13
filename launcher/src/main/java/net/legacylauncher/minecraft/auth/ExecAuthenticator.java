package net.legacylauncher.minecraft.auth;

import net.legacylauncher.user.AuthException;
import net.legacylauncher.user.User;

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
