package ru.turikhay.tlauncher.ui.accounts;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.managers.ProfileManagerListener;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import ru.turikhay.tlauncher.ui.block.Unblockable;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
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
import java.util.Iterator;

public class AccountList extends CenterPanel {
    private final AccountEditorScene scene;

    public final DefaultListModel<Account> model;
    public final JList<Account> list;

    public final LocalizableButton add, remove, back;

    /*public final DefaultListModel<Account> model;
    public final JList<Account> list;
    public final LocalizableButton add, remove,  back;

    public final LocalizableToggleButton ely;
    private final ActionListener elyButtonListener;*/

    public AccountList(AccountEditorScene sc) {
        super(squareInsets);
        scene = sc;

        BorderPanel wrapper = new BorderPanel();
        wrapper.setVgap(5);

        LocalizableLabel label = new LocalizableLabel("account.list");
        wrapper.setNorth(label);

        model = new DefaultListModel<Account>();
        list = new JList<Account>(model);

        list.setCellRenderer(new AccountCellRenderer(AccountCellRenderer.AccountCellType.EDITOR));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Account account = list.getSelectedValue();
                scene.handler.refreshEditor(account);
            }
        });

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

        add = new LocalizableButton(Images.getScaledIcon("plus.png", 16), "account.button.add");
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scene.handler.addAccount();
                defocus();
            }
        });
        firstLineButtons.add(add);

        remove = new LocalizableButton(Images.getScaledIcon("minus.png", 16), "account.button.remove");
        remove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scene.handler.removeAccount();
                defocus();
            }
        });
        firstLineButtons.add(remove);

        back = new AccountList.UnblockableImageButton(Images.getScaledIcon("home.png", 16), "account.button.home");
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scene.handler.exitEditor();
            }
        });
        firstLineButtons.add(back);

        wrapper.setSouth(buttons);
        add(wrapper);

        ProfileManagerListener listener = new ProfileManagerListener() {
            public void onProfilesRefreshed(ProfileManager pm) {
                refreshFrom(pm.getAuthDatabase());
            }

            public void onProfileManagerChanged(ProfileManager pm) {
                refreshFrom(pm.getAuthDatabase());
            }

            public void onAccountsRefreshed(AuthenticatorDatabase db) {
                refreshFrom(db);
            }
        };
        TLauncher.getInstance().getProfileManager().addListener(listener);
    }

    void refreshFrom(AuthenticatorDatabase db) {
        model.clear();
        Iterator var3 = db.getAccounts().iterator();

        while (var3.hasNext()) {
            Account account = (Account) var3.next();
            model.addElement(account);
        }

        if (model.isEmpty()) {
            scene.handler.notifyEmpty();
        }
    }

    class UnblockableImageButton extends LocalizableButton implements Unblockable {
        public UnblockableImageButton(ru.turikhay.tlauncher.ui.images.ImageIcon icon, String tooltip) {
            super(icon, tooltip);
        }
    }
}
