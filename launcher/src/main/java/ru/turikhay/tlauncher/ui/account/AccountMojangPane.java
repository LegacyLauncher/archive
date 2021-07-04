package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.user.MojangAuth;
import ru.turikhay.tlauncher.user.MojangUser;

public class AccountMojangPane extends StandardAccountPane<MojangAuth, MojangUser> {
    public AccountMojangPane(AccountManagerScene scene, PaneMode mode) {
        super(scene, mode, Account.AccountType.MOJANG);
    }

    @Override
    protected String accountIcon() {
        return "logo-mojang";
    }

    @Override
    protected String forgotPasswordUrl() {
        return "https://account.mojang.com/password";
    }

    @Override
    protected MojangAuth standardAuth() {
        return AccountManager.getMojangAuth();
    }
}
