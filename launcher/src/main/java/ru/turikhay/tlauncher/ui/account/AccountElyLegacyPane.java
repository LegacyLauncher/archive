package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.user.ElyLegacyAuth;
import ru.turikhay.tlauncher.user.ElyLegacyUser;

public class AccountElyLegacyPane extends StandardAccountPane<ElyLegacyAuth, ElyLegacyUser> {
    public AccountElyLegacyPane(AccountManagerScene scene, PaneMode m) {
        super(scene, m, Account.AccountType.ELY_LEGACY);
    }

    protected void removeAccountIfFound(String username) {
        removeAccountIfFound(username, Account.AccountType.ELY);
        removeAccountIfFound(username, Account.AccountType.ELY_LEGACY);
    }

    @Override
    protected String accountIcon() {
        return "logo-ely";
    }

    @Override
    protected String forgotPasswordUrl() {
        return "https://account.ely.by/forgot-password";
    }

    @Override
    protected ElyLegacyAuth standardAuth() {
        return AccountManager.getElyLegacyAuth();
    }
}
