package ru.turikhay.tlauncher.ui.accounts;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.listener.AuthUIListener;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import java.awt.*;

public class AccountHandler {
    private final AccountEditorScene scene;
    public final AccountList list;
    public final AccountEditor editor;
    private final ProfileManager manager = TLauncher.getInstance().getProfileManager();
    private final AuthUIListener listener;
    private Account lastAccount;
    private Account tempAccount;

    public AccountHandler(AccountEditorScene sc) {
        scene = sc;
        list = scene.list;
        editor = scene.editor;
        listener = new AuthUIListener(new AuthenticatorListener() {
            public void onAuthPassing(Authenticator auth) {
                block();
            }

            public void onAuthPassingError(Authenticator auth, Throwable e) {
                unblock();
            }

            public void onAuthPassed(Authenticator auth) {
                TLauncher.getInstance().getElyManager().refreshComponent();
                unblock();
                registerTemp();
            }
        });
        listener.editorOpened = true;
    }

    public void selectAccount(Account acc) {
        if (acc != null) {
            if (!acc.equals(list.list.getSelectedValue())) {
                list.list.setSelectedValue(acc, true);
            }
        }
    }

    void refreshEditor(Account account) {
        if (account == null) {
            clearEditor();
        } else if (!account.equals(lastAccount)) {
            lastAccount = account;
            Blocker.unblock(editor, "empty");
            editor.fill(account);
            if (!account.equals(tempAccount)) {
                scene.getMainPane().defaultScene.loginForm.accounts.setAccount(lastAccount);
            }

            scene.tip.setAccountType(account.getType());
        }
    }

    void clearEditor() {
        lastAccount = null;
        editor.clear();
        notifyEmpty();
    }

    void saveEditor() {
        if (lastAccount != null) {
            Account acc = editor.get();
            if (acc.getUsername() == null) {
                Alert.showLocError("auth.error.nousername");
            } else {
                if (containDisallowed(acc))
                    return;

                lastAccount.complete(acc);
                if (!lastAccount.isFree()) {
                    if (lastAccount.getAccessToken() == null && lastAccount.getPassword() == null) {
                        Alert.showLocError("auth.error.nopass");
                        return;
                    }

                    Authenticator.instanceFor(lastAccount).asyncPass(listener);
                } else {
                    registerTemp();
                    listener.saveProfiles();

                    scene.getMainPane().defaultScene.loginForm.accounts.refreshAccounts(manager.getAuthDatabase(), tempAccount);
                    list.refreshFrom(manager.getAuthDatabase());
                    list.list.setSelectedValue(lastAccount, true);

                    tempAccount = null;
                }

            }
        }
    }

    void exitEditor() {
        scene.getMainPane().openDefaultScene();
        listener.saveProfiles();
        list.list.clearSelection();
        scene.tip.setAccountType(null);
        notifyEmpty();
    }

    void addAccount() {
        if (tempAccount == null) {
            tempAccount = new Account();
            list.model.addElement(tempAccount);
            list.list.setSelectedValue(tempAccount, true);
            refreshEditor(tempAccount);
        }
    }

    void removeAccount() {
        if (lastAccount != null && !list.model.isEmpty()) {
            Account acc = lastAccount;
            int num = list.model.indexOf(lastAccount) - 1;
            list.model.removeElement(lastAccount);
            lastAccount = acc;
            if (tempAccount == null) {
                U.log("Removing", lastAccount);
                manager.getAuthDatabase().unregisterAccount(lastAccount);
                listener.saveProfiles();
            } else {
                tempAccount = null;
                clearEditor();
            }

            if (num > -1) {
                list.list.setSelectedIndex(num);
            }

        }
    }

    private boolean allowDisallowed = false;

    void registerTemp() {
        editor.password.setText(null);
        if (tempAccount != null) {
            manager.getAuthDatabase().registerAccount(tempAccount);
            scene.getMainPane().defaultScene.loginForm.accounts.refreshAccounts(manager.getAuthDatabase(), tempAccount);
            list.refreshFrom(manager.getAuthDatabase());
            int num = list.model.indexOf(tempAccount);
            list.list.setSelectedIndex(num);
            tempAccount = null;
        }
    }

    public void notifyEmpty() {
        if (list.list.getSelectedIndex() == -1) {
            Blocker.block(editor, "empty");
        }

    }

    private void block() {
        Blocker.block("auth", (Blockable[]) (new Blockable[]{editor, list}));
    }

    private void unblock() {
        Blocker.unblock("auth", (Blockable[]) (new Blockable[]{editor, list}));
    }

    private static final String[] DISALLOWED = {"turikhay", "nik_mmzd", "mcmodder", "DarikXPlay", "magic_kitten51"};

    private boolean containDisallowed(Account acc) {
        if (allowDisallowed || acc == null || StringUtils.isEmpty(acc.getUsername()))
            return false;

        String username = acc.getUsername();

        if (username.equals("разблокировать")) {
            allowDisallowed = true;
            editor.username.setBackground(Color.GREEN);
            editor.username.setValue(null);
            AsyncThread.execute(new Runnable() {
                @Override
                public void run() {
                    U.sleepFor(500);
                    editor.username.requestFocus();
                }
            });
            return true;
        }

        for (String disallowed : DISALLOWED)
            if (disallowed.equalsIgnoreCase(username))
                return true;

        return false;
    }
}
