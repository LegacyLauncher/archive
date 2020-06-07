package ru.turikhay.tlauncher.minecraft.auth;

import ru.turikhay.tlauncher.user.AuthException;
import ru.turikhay.util.U;

import java.io.IOException;

public class ExecAuthenticator extends Authenticator {
    private final AuthExecutor executor;
    private final Account.AccountType type;
    private Account account;

    public ExecAuthenticator(AuthExecutor executor, Account.AccountType type) {
        this.executor = U.requireNotNull(executor, "executor");
        this.type = U.requireNotNull(type, "type");
    }

    @Override
    public Account getAccount() {
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
