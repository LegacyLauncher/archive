package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.minecraft.auth.*;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.listener.AuthUIListener;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.user.*;

import java.io.IOException;

public class AccountMojangPane extends StandardAccountPane<MojangAuth, MojangUser> {
    public AccountMojangPane(AccountManagerScene scene, PaneMode mode) {
        super(scene, mode, Account.AccountType.MOJANG);
    }

    @Override
    protected String accountIcon() {
        return "mojang.png";
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
