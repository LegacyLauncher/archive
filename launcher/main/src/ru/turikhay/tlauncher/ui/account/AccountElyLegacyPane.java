package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.user.ElyLegacyAuth;
import ru.turikhay.tlauncher.user.ElyLegacyUser;
import ru.turikhay.tlauncher.user.StandardAuth;

public class AccountElyLegacyPane extends StandardAccountPane<ElyLegacyAuth, ElyLegacyUser> {
    public AccountElyLegacyPane(AccountManagerScene scene, PaneMode m) {
        super(scene, m, Account.AccountType.ELY_LEGACY);
    }

    protected Account findAccount(String username, boolean alertIfFound) {
        Account ely = findAccount(username, Account.AccountType.ELY, alertIfFound);
        if(ely != null) {
            return ely;
        }
        return findAccount(username, Account.AccountType.ELY_LEGACY, alertIfFound);
    }

    @Override
    protected String accountIcon() {
        return "ely.png";
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
