package ru.turikhay.tlauncher.ui.account;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.listener.AuthUIListener;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableTextField;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.text.ExtendedPasswordField;
import ru.turikhay.tlauncher.user.StandardAuth;
import ru.turikhay.tlauncher.user.User;
import ru.turikhay.tlauncher.user.UserSet;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Random;

public abstract class StandardAccountPane<T extends StandardAuth<Y>, Y extends User> extends ExtendedPanel implements AccountMultipaneCompCloseable {

    protected final AccountManagerScene scene;
    private final PaneMode mode;
    private final Account.AccountType accountType;

    private final LocalizableTextField emailField;
    private final ExtendedPasswordField passwordField;

    protected final ProgressBar progressBar;

    protected int session = -1;

    public StandardAccountPane(final AccountManagerScene scene, PaneMode m, final Account.AccountType accountType) {
        this.scene = scene;
        this.mode = Objects.requireNonNull(m, "mode");
        this.accountType = Objects.requireNonNull(accountType, "accountType");

        String LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + "account-" + accountType.toString().toLowerCase(java.util.Locale.ROOT) + ".";

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;

        LocalizableLabel emailLabel = new LocalizableLabel(LOC_PREFIX + "email");
        c.gridy++;
        add(emailLabel, c);

        emailField = new LocalizableTextField();
        emailField.setFont(emailField.getFont().deriveFont(emailField.getFont().getSize2D() + 2.f));
        emailField.setPlaceholder(LOC_PREFIX + "email.placeholder");
        c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);
        c.gridy++;
        add(emailField, c);

        LocalizableLabel passwordLabel = new LocalizableLabel(LOC_PREFIX + "password");
        c.insets = new Insets(SwingUtil.magnify(10), 0, 0, 0);
        c.gridy++;
        add(passwordLabel, c);

        passwordField = new ExtendedPasswordField();
        passwordField.setFont(passwordField.getFont().deriveFont(passwordField.getFont().getSize2D() + 2.f));
        c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);
        c.gridy++;
        add(passwordField, c);

        LocalizableLabel forgotpasswordLabel = new LocalizableHTMLLabel(LOC_PREFIX + "forgot-password");
        forgotpasswordLabel.setAlignmentX(RIGHT_ALIGNMENT);
        forgotpasswordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        forgotpasswordLabel.setFont(forgotpasswordLabel.getFont().deriveFont(forgotpasswordLabel.getFont().getSize2D() - 2.f));
        forgotpasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotpasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                OS.openLink(forgotPasswordUrl());
            }
        });
        c.insets = new Insets(SwingUtil.magnify(3), 0, 0, 0);
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_END;
        add(forgotpasswordLabel, c);

        final LocalizableButton authButton = new LocalizableButton(LOC_PREFIX + "auth");
        authButton.addActionListener(e -> {
            final String email = emailField.getValue();
            final char[] password = passwordField.getPassword();

            boolean
                    haveEmail = StringUtils.isNotBlank(email),
                    havePassword = password != null && password.length > 0;

            if (!haveEmail || !havePassword) {
                Alert.showLocError("account.manager.multipane.add-account.error.no-credentials");
                return;
            }

            switch (mode) {
                case ADD:
                    removeAccountIfFound(email);
                    break;
                case EDIT:
                    String oldUsername = scene.list.getSelected() == null || scene.list.getSelected().getType() != accountType ? null : scene.list.getSelected().getUsername();
                    if (oldUsername != null) {
                        removeAccountIfFound(oldUsername);
                    }
                    break;
            }

            final int currentSession = session = Math.abs(new Random().nextInt());
            credentialsEntered(currentSession, email, new String(password));
        });
        authButton.setFont(authButton.getFont().deriveFont(Font.BOLD));
        authButton.setIcon(Images.getIcon24(accountIcon()));
        c.insets = new Insets(SwingUtil.magnify(15), 0, 0, 0);
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_START;
        add(authButton, c);

        progressBar = new ProgressBar();
        c.insets = new Insets(SwingUtil.magnify(2), 0, 0, 0);
        c.gridy++;
        add(progressBar, c);

        if (mode == PaneMode.EDIT) {
            LocalizableButton removeButton = new LocalizableButton("account.manager.multipane.remove-account");
            removeButton.setIcon(Images.getIcon16("remove"));
            removeButton.addActionListener(e -> {
                Account<?> selected = scene.list.getSelected();
                if (selected != null && selected.getType() == accountType) {
                    TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(selected.getUser());
                }
                scene.multipane.goBack();
            });

            c.insets = new Insets(SwingUtil.magnify(12), 0, 0, 0);
            c.gridy++;
            c.anchor = GridBagConstraints.LINE_START;
            add(removeButton, c);
        }

        passwordField.addActionListener(e -> authButton.doClick());
    }

    static void removeAccountIfFound(String username, Account.AccountType type) {
        UserSet userSet = TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet();
        User user = userSet.getByUsername(username, type.name().toLowerCase(java.util.Locale.ROOT));
        if (user != null) {
            userSet.remove(user);
        }
    }

    protected void removeAccountIfFound(String username) {
        removeAccountIfFound(username, accountType);
    }

    protected final void credentialsEntered(final int currentSession, final String email, final String password) {
        final Authenticator<Y> authenticator = Authenticator.instanceFor(() -> {
            T auth = standardAuth();
            Y user = auth.authorize(email, password);
            return new Account<>(user);
        }, accountType);

        AuthUIListener<Y> l = new AuthUIListener<>(new AuthenticatorListener<Y>() {
            @Override
            public void onAuthPassing(Authenticator<? extends Y> var1) {
                if (session == currentSession) {
                    Blocker.blockComponents(StandardAccountPane.this, "user-pass");
                    progressBar.setIndeterminate(true);
                }
            }

            @Override
            public void onAuthPassingError(Authenticator<? extends Y> var1, Throwable var2) {
                if (session == currentSession) {
                    Stats.accountCreation(accountType.toString().toLowerCase(java.util.Locale.ROOT), "standard", "", false);
                    Blocker.unblockComponents(StandardAccountPane.this, "user-pass");
                    progressBar.setIndeterminate(false);
                }
            }

            @Override
            public void onAuthPassed(Authenticator<? extends Y> var1) {
                if (session == currentSession) {
                    Blocker.unblockComponents(StandardAccountPane.this, "user-pass");
                    progressBar.setIndeterminate(false);

                    Account<Y> account = authenticator.getAccount();

                    switch (mode) {
                        case ADD:
                            removeAccountIfFound(account.getUsername());
                            Stats.accountCreation(accountType.toString().toLowerCase(java.util.Locale.ROOT), "standard", "", true);
                            break;
                        case EDIT:
                            User newUser = account.getUser();
                            TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(newUser);
                            break;
                    }
                    TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().add(account.getUser());
                    StandardAccountPane.this.scene.list.select(account);
                    StandardAccountPane.this.scene.multipane.showTip("success-" + mode.toString().toLowerCase(java.util.Locale.ROOT));
                }
            }
        });

        l.editorOpened = true;
        authenticator.asyncPass(l);
    }

    protected abstract String accountIcon();

    protected abstract String forgotPasswordUrl();

    protected abstract T standardAuth();

    @Override
    public void multipaneClosed() {
        session = -1;
    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return mode.toString().toLowerCase(java.util.Locale.ROOT) + "-account-" + accountType.toString().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        emailField.setValue(null);
        passwordField.setText(null);

        if (mode == PaneMode.EDIT && scene.list.getSelected() != null && scene.list.getSelected().getType() == accountType) {
            Account<?> account = scene.list.getSelected();
            emailField.setValue(account.getUsername());
        }
    }

}
