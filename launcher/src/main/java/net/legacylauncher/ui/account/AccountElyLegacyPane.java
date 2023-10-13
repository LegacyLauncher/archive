package net.legacylauncher.ui.account;

import net.legacylauncher.managers.AccountManager;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.ui.scenes.AccountManagerScene;
import net.legacylauncher.user.ElyLegacyAuth;
import net.legacylauncher.user.ElyLegacyUser;

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
