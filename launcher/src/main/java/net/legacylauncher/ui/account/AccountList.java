package net.legacylauncher.ui.account;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.managers.ProfileManager;
import net.legacylauncher.managers.ProfileManagerListener;
import net.legacylauncher.managers.SwingProfileManagerListener;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.minecraft.auth.AccountListener;
import net.legacylauncher.minecraft.auth.AuthenticatorDatabase;
import net.legacylauncher.ui.block.Blockable;
import net.legacylauncher.ui.block.Blocker;
import net.legacylauncher.ui.center.CenterPanel;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.scenes.AccountManagerScene;
import net.legacylauncher.ui.swing.AccountCellRenderer;
import net.legacylauncher.ui.swing.ScrollPane;
import net.legacylauncher.ui.swing.extended.BorderPanel;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.user.User;

import javax.swing.*;
import java.awt.*;

public class AccountList extends CenterPanel implements ProfileManagerListener, AccountListener, Blockable {

    private final AccountManagerScene scene;

    private final DefaultListModel<Account<? extends User>> accountModel;
    private final JList<Account<? extends User>> list;
    private final LocalizableButton add;
    private final LocalizableButton edit;


    public AccountList(final AccountManagerScene scene) {
        super(squareInsets);

        this.scene = scene;

        BorderPanel wrapper = new BorderPanel();
        wrapper.setVgap(5);

        this.accountModel = new DefaultListModel<>();

        this.list = new JList<>(accountModel);
        list.setCellRenderer(new AccountCellRenderer(AccountCellRenderer.AccountCellType.EDITOR));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> {
            if ("success".equals(scene.multipane.currentTip())) {
                scene.multipane.showTip("welcome");
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

        add = new LocalizableButton(Images.getIcon24("plus-square"), "account.button.add");
        add.addActionListener(e -> AccountList.this.scene.multipane.showTip("add-account"));
        firstLineButtons.add(add);

        edit = new LocalizableButton(Images.getIcon24("pencil-square"), "account.button.remove");
        edit.addActionListener(e -> {
            if (scene.list.getSelected() != null) {
                scene.multipane.showTip("edit-account-" + scene.list.getSelected().getType().toString().toLowerCase(java.util.Locale.ROOT));
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
        });
        firstLineButtons.add(edit);

        LocalizableButton back = new LocalizableButton(Images.getIcon24("home"), "account.button.home");
        back.addActionListener(e -> LegacyLauncher.getInstance().getFrame().mp.openDefaultScene());
        firstLineButtons.add(back);

        wrapper.setSouth(buttons);
        add(wrapper);

        LegacyLauncher.getInstance().getProfileManager().addListener(new SwingProfileManagerListener(this));
    }

    public Account<? extends User> getSelected() {
        return list.getSelectedValue();
    }

    public void select(Account<? extends User> account) {
        list.setSelectedValue(account, true);
    }

    public void updateList() {
        onAccountsRefreshed(LegacyLauncher.getInstance().getProfileManager().getAuthDatabase());
    }

    @Override
    public void onAccountsRefreshed(AuthenticatorDatabase db) {
        accountModel.clear();
        for (Account<? extends User> account : db.getAccounts()) {
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
