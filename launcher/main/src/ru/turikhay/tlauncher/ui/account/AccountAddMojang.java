package ru.turikhay.tlauncher.ui.account;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.minecraft.auth.MojangAuthenticator;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
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
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Random;

public class AccountAddMojang extends ExtendedPanel implements AccountMultipaneCompCloseable {
    private final String LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + multipaneName() + ".";

    private final AccountManagerScene scene;

    private final LocalizableTextField emailField;
    private final ExtendedPasswordField passwordField;

    private final ProgressBar progressBar;

    private int session = -1;

    public AccountAddMojang(final AccountManagerScene scene) {
        this.scene = scene;

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
                OS.openLink("https://account.mojang.com/password");
            }
        });
        c.insets = new Insets(SwingUtil.magnify(3), 0, 0, 0);
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_END;
        add(forgotpasswordLabel, c);

        final LocalizableButton button = new LocalizableButton(LOC_PREFIX + "auth");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getValue();
                char[] password = passwordField.getPassword();

                if(StringUtils.isBlank(email) || password == null || password.length == 0) {
                    Alert.showLocError("account.manager.multipane.add-account.error.no-credentials");
                    return;
                }

                Account existing = TLauncher.getInstance().getProfileManager().getAuthDatabase().getByUsername(email, Account.AccountType.MOJANG);
                if(existing != null) {
                    Alert.showLocError("account.manager.multipane.add-account.error.exists");
                    return;
                }

                final int currentSession = session = Math.abs(new Random().nextInt());

                final Account account = new Account();
                account.setType(Account.AccountType.MOJANG);
                account.setUsername(email);
                account.setPassword(password);
                MojangAuthenticator mojangAuthenticator = new MojangAuthenticator(account);
                AuthUIListener l = new AuthUIListener(new AuthenticatorListener() {
                    @Override
                    public void onAuthPassing(Authenticator var1) {
                        if(session == currentSession) {
                            Blocker.blockComponents(AccountAddMojang.this, "mojang-pass");
                            progressBar.setIndeterminate(true);
                        }
                    }

                    @Override
                    public void onAuthPassingError(Authenticator var1, Throwable var2) {
                        if(session == currentSession) {
                            Blocker.unblockComponents(AccountAddMojang.this, "mojang-pass");
                            progressBar.setIndeterminate(false);
                        }
                    }

                    @Override
                    public void onAuthPassed(Authenticator var1) {
                        if(session == currentSession) {
                            Blocker.unblockComponents(AccountAddMojang.this, "mojang-pass");
                            progressBar.setIndeterminate(false);
                            TLauncher.getInstance().getProfileManager().getAuthDatabase().registerAccount(account);
                            try {
                                TLauncher.getInstance().getProfileManager().saveProfiles();
                            } catch (IOException e1) {
                                Alert.showError(e1);
                                return;
                            }
                            AccountAddMojang.this.scene.list.select(account);
                            AccountAddMojang.this.scene.multipane.showTip("success");
                        }
                    }
                });
                l.editorOpened = true;
                mojangAuthenticator.asyncPass(l);
                //TLauncher.getInstance().getProfileManager().getAuthDatabase().registerAccount();
                //AccountAddMojang.this.scene.multipane.showTip("success");
            }
        });
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setIcon(Images.getIcon("mojang.png", 24));
        c.insets = new Insets(SwingUtil.magnify(15), 0, 0, 0);
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_START;
        add(button, c);

        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.doClick();
            }
        });

        progressBar = new ProgressBar();
        c.insets = new Insets(SwingUtil.magnify(2), 0, 0, 0);
        c.gridy++;
        add(progressBar, c);
    }

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
        return "add-account-mojang";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown() {
        emailField.setValue(null);
        passwordField.setText(null);
    }
}
