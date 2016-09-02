package ru.turikhay.tlauncher.ui.accounts;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ElyManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import ru.turikhay.tlauncher.ui.loc.LocalizableRadioButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableToggleButton;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.swing.CheckBoxListener;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.text.ExtendedPasswordField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class AccountEditor extends CenterPanel {
    private static final String passlock = "passlock", PROGRESS_BAR = "pb", ELY_BUTTON = "ely";

    private final AccountEditorScene scene;
    public final UsernameField username;
    public final AccountEditor.BlockablePasswordField password;
    public final ButtonGroup authGroup;
    public final AccountEditor.AuthTypeRadio freeAuth;
    public final AccountEditor.AuthTypeRadio mojangAuth;
    public final AccountEditor.AuthTypeRadio elyAuth;
    public final LinkedHashMap<Account.AccountType, AccountEditor.AuthTypeRadio> radioMap = new LinkedHashMap();
    public final LocalizableButton save;

    private final ExtendedPanel bottomCardPanel;
    private final CardLayout bottomCardLayout;

    private final ProgressBar progressBar;
    private final LocalizableCheckbox elyButton;
    private final ActionListener elyButtonListener;

    public AccountEditor(AccountEditorScene sc) {
        super(new MagnifiedInsets(15, 15, 0, 15));

        scene = sc;
        ActionListener enterHandler = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                defocus();
                scene.handler.saveEditor();
            }
        };
        username = new UsernameField(this, UsernameField.UsernameState.USERNAME);
        username.addActionListener(enterHandler);
        password = new AccountEditor.BlockablePasswordField();
        password.addActionListener(enterHandler);
        password.setEnabled(false);
        authGroup = new ButtonGroup();
        freeAuth = new AccountEditor.AuthTypeRadio(Account.AccountType.FREE);
        mojangAuth = new AccountEditor.AuthTypeRadio(Account.AccountType.MOJANG);
        elyAuth = new AccountEditor.AuthTypeRadio(Account.AccountType.ELY);
        save = new LocalizableButton("account.save");
        save.addActionListener(enterHandler);

        bottomCardPanel = new UnblockablePanel();
        bottomCardLayout = new CardLayout();
        bottomCardPanel.setLayout(bottomCardLayout);


        progressBar = new ProgressBar();
        progressBar.setPreferredSize(new Dimension(200, 20));
        bottomCardPanel.add(progressBar, PROGRESS_BAR);

        //ImageIcon elyIcon = Images.getScaledIcon("ely.png", 16);
        elyButton = new LocalizableCheckbox();
        elyButton.setText("account.button.ely.toggle");
        /*elyButton.setIcon(elyIcon);
        elyButton.setDisabledIcon(elyIcon.getDisabledIcon());*/
        elyButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!elyButton.isEnabled()) {
                    return;
                }

                boolean selected = elyButton.isSelected();

                if (selected) {
                    Alert.showLocMessage("account.button.ely.enabled");
                } else {
                    if (!Alert.showLocQuestion("account.button.ely.disabled")) {
                        updateElyToggle();
                        return;
                    }
                }

                TLauncher.getInstance().getSettings().set("ely.globally", selected);
            }
        };
        updateElyToggle();
        bottomCardPanel.add(elyButton, ELY_BUTTON);

        add(del(0));
        add(sepPan(new Component[]{username}));
        add(sepPan(new Component[]{freeAuth, mojangAuth, elyAuth}));
        add(sepPan(new Component[]{password}));
        add(del(0));
        add(sepPan(new Component[]{save}));
        add(del(0));
        add(sepPan(new Component[]{bottomCardPanel}));

        bottomCardLayout.show(bottomCardPanel, ELY_BUTTON);
    }

    public Account.AccountType getSelectedAccountType() {
        Iterator var2 = radioMap.entrySet().iterator();

        while (var2.hasNext()) {
            Entry en = (Entry) var2.next();
            if (((AccountEditor.AuthTypeRadio) en.getValue()).isSelected()) {
                return (Account.AccountType) en.getKey();
            }
        }

        return Account.AccountType.FREE;
    }

    public void setSelectedAccountType(Account.AccountType type) {
        AccountEditor.AuthTypeRadio selectable = radioMap.get(type);
        if (selectable != null) {
            selectable.setSelected(true);
        }

    }

    public void fill(Account account) {
        setSelectedAccountType(account.getType());
        username.setText(account.getUsername());
        password.setText(null);
    }

    public void clear() {
        setSelectedAccountType(null);
        username.setText(null);
        password.setText(null);
    }

    public Account get() {
        Account account = new Account();
        account.setUsername(username.getValue());
        Account.AccountType type = getSelectedAccountType();
        switch (type) {
            case MOJANG:
            case ELY:
                if (password.hasPassword()) {
                    account.setPassword(password.getPassword());
                }
            case FREE:
            default:
                account.setType(type);
                return account;
        }
    }

    public void updateElyToggle() {
        elyButton.removeActionListener(elyButtonListener);

        ElyManager manager = TLauncher.getInstance().getElyManager();
        elyButton.setEnabled(manager.isAllowedGlobally());
        elyButton.setSelected(manager.isUsingGlobally());

        elyButton.addActionListener(elyButtonListener);
    }

    public Insets getInsets() {
        return squareInsets;
    }

    public void block(Object reason) {
        super.block(reason);
        if (!reason.equals("empty")) {
            bottomCardLayout.show(bottomCardPanel, PROGRESS_BAR);
            progressBar.setIndeterminate(true);
        }

    }

    public void unblock(Object reason) {
        super.unblock(reason);
        if (!reason.equals("empty")) {
            bottomCardLayout.show(bottomCardPanel, ELY_BUTTON);
            progressBar.setIndeterminate(false);
        }

    }

    public class AuthTypeRadio extends LocalizableRadioButton {
        private final Account.AccountType type;

        private AuthTypeRadio(final Account.AccountType type) {
            super("account.auth." + type);
            radioMap.put(type, this);
            authGroup.add(this);
            this.type = type;
            final boolean free = type == Account.AccountType.FREE;
            addItemListener(new CheckBoxListener() {
                public void itemStateChanged(boolean newstate) {
                    if (newstate && !password.hasPassword()) {
                        password.setText(null);
                    }

                    if (newstate) {
                        scene.tip.setAccountType(type);
                    }

                    newstate &= free;
                    Blocker.setBlocked(password, passlock, newstate);
                    username.setState(newstate ? UsernameField.UsernameState.USERNAME : UsernameField.UsernameState.EMAIL);
                    defocus();
                }
            });
        }

        public Account.AccountType getAccountType() {
            return type;
        }
    }

    private class UnblockablePanel extends ExtendedPanel implements Blockable {
        @Override
        public void block(Object var1) {
        }

        @Override
        public void unblock(Object var1) {
        }
    }

    class BlockablePasswordField extends ExtendedPasswordField implements Blockable {
        private BlockablePasswordField() {
        }

        public void block(Object reason) {
            setEnabled(false);
        }

        public void unblock(Object reason) {
            setEnabled(true);
        }
    }
}
