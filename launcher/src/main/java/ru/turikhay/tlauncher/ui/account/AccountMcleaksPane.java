package ru.turikhay.tlauncher.ui.account;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.managers.McleaksManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.listener.AuthUIListener;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableTextField;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.user.McleaksUser;
import ru.turikhay.tlauncher.user.User;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class AccountMcleaksPane extends BorderPanel implements AccountMultipaneCompCloseable, Blockable {
    private final String LOC_PREFIX;
    private final PaneMode mode;

    private final AccountManagerScene scene;

    private final LocalizableTextField username, oldToken, newToken;
    private final LocalizableButton button;
    private final ProgressBar progressBar;

    private final Blockable buttonBlocker = new Blockable() {
        @Override
        public void block(Object var1) {
            button.setEnabled(false);
        }

        @Override
        public void unblock(Object var1) {
            button.setEnabled(true);
        }
    };

    private int currentSession;

    public AccountMcleaksPane(final AccountManagerScene scene, final PaneMode mode) {
        this.scene = scene;
        this.mode = mode;
        LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + "account-mcleaks.";

        ExtendedPanel content = new ExtendedPanel();
        content.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);

        ExtendedPanel bottom = new ExtendedPanel();
        bottom.setLayout(new GridBagLayout());
        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.gridy = -1;
        c1.gridwidth = 1;
        c1.weightx = 1.0;
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.anchor = GridBagConstraints.LINE_START;
        //c1.insets = new Insets(SwingUtil.magnify(10), 0, 0, 0);

        button = new LocalizableButton(LOC_PREFIX + "button." + mode.toString().toLowerCase(java.util.Locale.ROOT), c);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int session = new Random().nextInt();
                currentSession = session;
                final String altToken = newToken.getValue();
                if (StringUtils.isBlank(altToken)) {
                    Alert.showLocError("account.manager.multipane.add-account.error.no-credentials");
                    return;
                }
                Authenticator.instanceFor(() -> new Account<>(AccountManager.getMcleaksAuth().authorize(altToken)), Account.AccountType.MCLEAKS).asyncPass(new AuthUIListener<McleaksUser>(new AuthenticatorListener<McleaksUser>() {
                    @Override
                    public void onAuthPassing(Authenticator<? extends McleaksUser> var1) {
                        if (currentSession != session) {
                            return;
                        }
                        Blocker.blockComponents(AccountMcleaksPane.this, "mcleaks-auth");
                        progressBar.setIndeterminate(true);
                    }

                    @Override
                    public void onAuthPassingError(Authenticator<? extends McleaksUser> var1, Throwable var2) {
                        if (currentSession != session) {
                            return;
                        }
                        Blocker.unblockComponents(AccountMcleaksPane.this, "mcleaks-auth");
                        progressBar.setIndeterminate(false);
                        Stats.accountCreation("mcleaks", "standard", "", false);
                    }

                    @Override
                    public void onAuthPassed(Authenticator<? extends McleaksUser> var1) {
                        if (currentSession != session) {
                            return;
                        }
                        Blocker.unblockComponents(AccountMcleaksPane.this, "mcleaks-auth");
                        McleaksUser oldUser = null;
                        if (mode == PaneMode.EDIT) {
                            Account<? extends User> acc = scene.list.getSelected();
                            if (acc != null && acc.getType() == Account.AccountType.MCLEAKS) {
                                oldUser = (McleaksUser) acc.getUser();
                            }
                        }
                        progressBar.setIndeterminate(false);
                        McleaksUser user = var1.getAccount().getUser();
                        if (oldUser != null) {
                            if (oldUser.equals(user)) {
                                oldUser.setAltToken(user.getAltToken());
                            } else {
                                if (Alert.showLocQuestion(LOC_PREFIX + "overwrite", (Object) user.getUsername())) {
                                    TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(oldUser);
                                }
                            }
                        }
                        TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().add(user);
                        scene.list.select(new Account<>(user));
                        scene.multipane.showTip("success-" + mode.toString().toLowerCase(java.util.Locale.ROOT));
                        Stats.accountCreation("mcleaks", "standard", "", true);
                    }
                }) {{
                    editorOpened = true;
                }});
            }
        });
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setIcon(Images.getIcon24("logo-mcleaks"));
        c1.gridy++;
        bottom.add(button, c1);

        if (McleaksManager.isUnsupported()) {
            Blocker.block(buttonBlocker, "mcleaks-unsupported");
        }

        switch (mode) {
            case ADD:
                username = null;
                oldToken = null;
                LocalizableButton removeButton;

                c.gridy++;
                content.add(new LocalizableLabel(LOC_PREFIX + "token"), c);

                newToken = new LocalizableTextField();
                newToken.setFont(newToken.getFont().deriveFont(newToken.getFont().getSize2D() + 2.f));
                newToken.setPlaceholder(LOC_PREFIX + "token.placeholder");
                c.gridy++;
                content.add(newToken, c);

                LocalizableButton getTokenButton = new LocalizableButton(LOC_PREFIX + "button.get-token");
                getTokenButton.addActionListener(e -> OS.openLink("https://mcleaks.net/get"));
                c1.gridy++;
                bottom.add(getTokenButton, c1);

                break;
            case EDIT:
                c.gridy++;
                content.add(new LocalizableLabel(LOC_PREFIX + "username"), c);

                username = new LocalizableTextField();
                username.setEditable(false);
                c.gridy++;
                content.add(username, c);

                c.gridy++;
                content.add(new LocalizableLabel(LOC_PREFIX + "oldToken"), c);

                oldToken = new LocalizableTextField();
                oldToken.addMouseListener(new TextPopup());
                oldToken.setEditable(false);
                c.gridy++;
                content.add(oldToken, c);

                c.gridy++;
                content.add(new LocalizableLabel(LOC_PREFIX + "newToken"), c);

                newToken = new LocalizableTextField();
                newToken.setPlaceholder(LOC_PREFIX + "token.placeholder");
                c.gridy++;

                BorderPanel panel = new BorderPanel();
                panel.setInsets(0, 0, 0, 0);
                panel.setCenter(newToken);

                LocalizableButton b = new LocalizableButton(LOC_PREFIX + "newToken.renew");
                b.addActionListener(e -> OS.openLink("https://mcleaks.net/renew"));
                panel.setEast(b);
                c.gridy++;
                content.add(panel, c);

                removeButton = new LocalizableButton("account.manager.multipane.remove-account");
                removeButton.addActionListener(e -> {
                    Account<? extends User> account = scene.list.getSelected();
                    if (account != null && account.getType().equals(Account.AccountType.MCLEAKS)) {
                        TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(account.getUser());
                        scene.multipane.showTip("welcome");
                    }
                });
                c1.gridy++;
                bottom.add(removeButton, c1);
                break;
            default:
                throw new IllegalArgumentException("unknown mode: " + mode);
        }

        newToken.addMouseListener(new TextPopup());

        this.progressBar = new ProgressBar();
        progressBar.setPreferredSize(new Dimension(1, 10));
        c1.gridy++;
        bottom.add(progressBar, c1);

        setCenter(content);
        setSouth(bottom);
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        switch (mode) {
            case ADD:
                newToken.setValue(null);
                break;
            case EDIT:
                Account<? extends User> account = scene.list.getSelected();
                if (account == null || account.getType() != Account.AccountType.MCLEAKS) {
                    return;
                }
                username.setValue(account.getUsername());
                oldToken.setValue(((McleaksUser) account.getUser()).getAltToken());
                newToken.setValue(null);
                break;
        }
    }

    @Override
    public void multipaneClosed() {
        Blocker.unblockComponents(AccountMcleaksPane.this, "mcleaks-auth");
        currentSession = -1;
    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return (mode == PaneMode.ADD ? "process" : mode.toString().toLowerCase(java.util.Locale.ROOT)) + "-account-mcleaks";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void block(Object var1) {
        Blocker.block(buttonBlocker, var1);
        Blocker.blockComponents(var1, getComponents());
    }

    @Override
    public void unblock(Object var1) {
        Blocker.unblock(buttonBlocker, var1);
        Blocker.unblockComponents(var1, getComponents());
    }
}
