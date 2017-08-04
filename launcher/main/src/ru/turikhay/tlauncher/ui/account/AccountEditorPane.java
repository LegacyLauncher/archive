package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

import java.awt.*;

public abstract class AccountEditorPane extends ExtendedPanel implements AccountMultipaneCompCloseable {
    private final Mode mode;
    private final String name;

    protected final AccountManagerScene scene;
    protected Account selectedAccount;

    AccountEditorPane(AccountManagerScene scene, Account.AccountType accountType, Mode mode) {
        this.scene = scene;
        this.name = "account-" + accountType.toString().toLowerCase() + "-" + mode.toString().toLowerCase();
        this.mode = mode;
    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return name;
    }

    @Override
    public boolean multipaneLocksView() {
        return mode == Mode.CREATING;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        if(mode == Mode.CREATING) {
            clearFields();
        }
        if(mode == Mode.EDITING) {
            selectedAccount = scene.list.getSelected();
        }
    }

    @Override
    public void multipaneClosed() {
        if(mode == Mode.EDITING) {
            clearFields();
        }
    }

    abstract void fillFields(Account account);
    abstract void clearFields();

    public enum Mode {
        CREATING, EDITING
    }
}
