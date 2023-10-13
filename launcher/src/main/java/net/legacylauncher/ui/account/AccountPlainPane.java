package net.legacylauncher.ui.account;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.managers.AccountManager;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.background.fx.FxAudioPlayer;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.loc.LocalizableLabel;
import net.legacylauncher.ui.loc.LocalizableTextField;
import net.legacylauncher.ui.scenes.AccountManagerScene;
import net.legacylauncher.ui.swing.DocumentChangeListener;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.ui.theme.Theme;
import net.legacylauncher.user.User;
import net.legacylauncher.util.Lazy;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.async.AsyncThread;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class AccountPlainPane extends ExtendedPanel implements AccountMultipaneCompCloseable {
    private static final String[] DISALLOWED = {"turikhay", "nik_mmzd", "mcmodder", "DarikXPlay", "ErickSkrauch"};
    private static final String NOSTALGIC = "nostalgic";

    private final AccountManagerScene scene;
    private final LocalizableTextField field;

    private final PaneMode mode;

    private boolean unlocked;

    public AccountPlainPane(final AccountManagerScene scene, final PaneMode mode) {
        this.scene = scene;
        this.mode = mode;

        String LOC_PREFIX = LOC_PREFIX_PATH + "account-plain.";

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;

        LocalizableLabel label = new LocalizableLabel(LOC_PREFIX + "label");
        c.gridy++;
        add(label, c);

        field = new LocalizableTextField();
        field.getDocument().addDocumentListener(new DocumentChangeListener() {
            private boolean wasValid = true;
            @Override
            public void documentChanged(DocumentEvent e) {
                String v = field.getValue();
                setValid(StringUtils.isEmpty(v) || isNameValid(v));
            }

            private void setValid(boolean valid) {
                if (wasValid == valid) {
                    return;
                }
                if (valid) {
                    field.setBackground(UIManager.getColor("TextField.background"));
                } else {
                    field.setBackground(Theme.getTheme().getFailure());
                }
                this.wasValid = valid;
            }
        });
        field.setFont(field.getFont().deriveFont(field.getFont().getSize2D() + 4.f));
        field.setPlaceholder(LOC_PREFIX + "placeholder");
        c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);
        c.gridy++;
        add(field, c);

        final LocalizableButton button = new LocalizableButton(LOC_PREFIX + (mode == PaneMode.EDIT ? "edit" : "save"));
        button.addActionListener(e -> {
            String username = field.getValue();

            if ("разблокировать".equalsIgnoreCase(username)) {
                field.setValue(null);
                unlocked = true;
                return;
            }

            if (StringUtils.isBlank(username)) {
                Alert.showLocError("account.manager.multipane.add-account.error.no-credentials");
                return;
            }

            if (!isNameValid(username)) {
                Alert.showLocError("account.manager.multipane.account-plain.invalid");
                return;
            }

            String checkUsername = null;
            switch (mode) {
                case ADD:
                    checkUsername = username;
                    break;
                case EDIT:
                    String oldUsername = scene.list.getSelected() != null && scene.list.getSelected().getType() == Account.AccountType.PLAIN ? scene.list.getSelected().getUsername() : null;
                    if (!username.equalsIgnoreCase(oldUsername)) {
                        checkUsername = username;
                    }
                    break;
            }

            if (mode == PaneMode.EDIT && scene.list.getSelected() != null) {
                LegacyLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(scene.list.getSelected().getUser());
            }

            if (checkUsername != null) {
                StandardAccountPane.removeAccountIfFound(checkUsername, Account.AccountType.PLAIN);
            }

            if (!unlocked) {
                for (String disallowed : DISALLOWED)
                    if (disallowed.equalsIgnoreCase(username))
                        return;
            }

            User user = AccountManager.getPlainAuth().authorize(username);
            LegacyLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().add(user);
            AccountPlainPane.this.scene.list.select(new Account<>(user));
            AccountPlainPane.this.scene.multipane.showTip("success-" + mode.toString().toLowerCase(java.util.Locale.ROOT));
            updateNostalgicBackground(username, true);
        });
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setIcon(Images.getIcon24("user-circle-o"));
        c.gridy++;
        add(button, c);

        if (mode == PaneMode.EDIT) {
            LocalizableButton removeButton = new LocalizableButton("account.manager.multipane.remove-account");
            removeButton.setIcon(Images.getIcon16("remove"));
            removeButton.addActionListener(e -> {
                Account<? extends User> selected = scene.list.getSelected();
                if (selected != null && selected.getType() == Account.AccountType.PLAIN) {
                    LegacyLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(selected.getUser());
                    updateNostalgicBackground(selected.getUsername(), false);
                }
                scene.multipane.goBack();
            });

            c.insets = new Insets(SwingUtil.magnify(12), 0, 0, 0);
            c.gridy++;
            c.anchor = GridBagConstraints.LINE_START;
            add(removeButton, c);
        }

        field.addActionListener(e -> {
            button.doClick();
            if (unlocked) {
                field.grabFocus();
            }
        });
    }

    @Override
    public void multipaneClosed() {

    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return mode.toString().toLowerCase(java.util.Locale.ROOT) + "-account-plain";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        field.setValue(null);

        if (mode == PaneMode.EDIT) {
            Account<? extends User> selected = scene.list.getSelected();
            if (selected != null && selected.getType() == Account.AccountType.PLAIN) {
                field.setValue(selected.getUsername());
            }
        }
    }

    private static boolean isNameValid(String name) {
        return name != null && name.length() <= 16 && name.chars().noneMatch(ch -> ch <= 32 || ch >= 127);
    }

    private void updateNostalgicBackground(String username, boolean newState) {
        if (NOSTALGIC.equalsIgnoreCase(username)) {
            SwingUtil.later(() -> LegacyLauncher.getInstance().getFrame().mp.background.setNostalgic(newState));
        }
    }
}
