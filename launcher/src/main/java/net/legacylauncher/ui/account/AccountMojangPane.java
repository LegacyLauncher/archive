package net.legacylauncher.ui.account;

import net.legacylauncher.managers.AccountManager;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.ui.scenes.AccountManagerScene;
import net.legacylauncher.user.MojangAuth;
import net.legacylauncher.user.MojangUser;

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
