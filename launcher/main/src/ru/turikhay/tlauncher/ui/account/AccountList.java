package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.managers.ProfileManagerListener;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.AccountListener;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.AccountCellRenderer;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class AccountList extends CenterPanel implements ProfileManagerListener, AccountListener, Blockable {

    private final AccountManagerScene scene;

    private final DefaultListModel<Account> accountModel;
    private final JList<Account> list;
    private final LocalizableButton add, back, edit;


    public AccountList(final AccountManagerScene scene) {
        super(squareInsets);

        this.scene = scene;

        BorderPanel wrapper = new BorderPanel();
        wrapper.setVgap(5);

        this.accountModel = new DefaultListModel<>();

        this.list = new JList<>(accountModel);
        list.setCellRenderer(new AccountCellRenderer(AccountCellRenderer.AccountCellType.EDITOR));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if("success".equals(scene.multipane.currentTip())) {
                    scene.multipane.showTip("welcome");
                }
            }
        });
        //list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setHBPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVBPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        wrapper.setCenter(scrollPane);

        ExtendedPanel buttons = new ExtendedPanel();
        buttons.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        ExtendedPanel firstLineButtons = new ExtendedPanel(new GridLayout(0, 3));
        ++c.gridy;
        buttons.add(firstLineButtons, c);

        add = new LocalizableButton(Images.getScaledIcon("plus.png", 24), "account.button.add");
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AccountList.this.scene.multipane.showTip("add-account");
            }
        });
        firstLineButtons.add(add);

        edit = new LocalizableButton(Images.getScaledIcon("pencil.png", 24), "account.button.remove");
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(scene.list.getSelected() != null) {
                    scene.multipane.showTip("edit-account-" + scene.list.getSelected().getType().toString().toLowerCase());
                }
                /*int index = list.getSelectedIndex();
                Account selected = list.getSelectedValue();
                if(selected != null) {
                    TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(selected.getUser());
                }
                try {
                    TLauncher.getInstance().getProfileManager().saveProfiles();
                } catch (IOException e1) {
                    Alert.showError(e1);
                    return;
                }
                if(index >= accountModel.getSize()) {
                    index = accountModel.getSize() - 1;
                }
                if(index > -1) {
                    list.setSelectedIndex(index);
                }*/
            }
        });
        firstLineButtons.add(edit);

        back = new LocalizableButton(Images.getScaledIcon("home.png", 24), "account.button.home");
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TLauncher.getInstance().getFrame().mp.openDefaultScene();
            }
        });
        firstLineButtons.add(back);

        wrapper.setSouth(buttons);
        add(wrapper);

        TLauncher.getInstance().getProfileManager().addListener(this);
    }

    public Account getSelected() {
        return list.getSelectedValue();
    }

    public void select(Account account) {
        list.setSelectedValue(account, true);
    }

    @Override
    public void onAccountsRefreshed(AuthenticatorDatabase db) {
        accountModel.clear();
        for(Account account : db.getAccounts()) {
            accountModel.addElement(account);
        }
    }

    @Override
    public void onProfilesRefreshed(ProfileManager var1) {
        onAccountsRefreshed(var1.getAuthDatabase());
    }

    @Override
    public void onProfileManagerChanged(ProfileManager var1) {
        onAccountsRefreshed(var1.getAuthDatabase());
    }

    @Override
    public void block(Object reason) {
        //super.block(reason);
        Blocker.blockComponents(reason, list, add, edit);
    }

    @Override
    public void unblock(Object reason) {
        //super.unblock(reason);
        Blocker.unblockComponents(reason, list, add, edit);
    }
}
