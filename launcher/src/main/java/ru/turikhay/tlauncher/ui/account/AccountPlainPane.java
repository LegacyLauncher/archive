package ru.turikhay.tlauncher.ui.account;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.*;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.DocumentChangeListener;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedCheckbox;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.user.PlainUser;
import ru.turikhay.tlauncher.user.User;
import ru.turikhay.util.SwingUtil;

import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AccountPlainPane extends ExtendedPanel implements AccountMultipaneCompCloseable {
    private static final String[] DISALLOWED = {"turikhay", "nik_mmzd", "mcmodder", "DarikXPlay", "ErickSkrauch"};

    private final AccountManagerScene scene;
    private final LocalizableTextField field;
    private final ExtendedCheckbox invalidNameAwareCheckbox;
    private final ExtendedCheckbox elySkins;

    private final PaneMode mode;

    private boolean unlocked;

    public AccountPlainPane(final AccountManagerScene scene, final PaneMode mode) {
        this.scene = scene;
        this.mode = mode;

        String LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + "account-plain.";

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

        ExtendedPanel invalidNameAwarePane = new ExtendedPanel();
        GridBagConstraints ac = new GridBagConstraints();
        ac.gridy = 0;
        ac.gridx = 0;
        ac.anchor = GridBagConstraints.WEST;
        invalidNameAwareCheckbox = new ExtendedCheckbox();
        invalidNameAwarePane.add(invalidNameAwareCheckbox, ac);
        ac.gridx++;
        LocalizableHTMLLabel invalidNameAwareLabel = new LocalizableHTMLLabel(LOC_PREFIX + "invalid-name-aware-checkbox");
        invalidNameAwareLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                invalidNameAwareCheckbox.doClick();
            }
        });
        invalidNameAwareLabel.setLabelWidth(SwingUtil.magnify(260));
        invalidNameAwarePane.add(invalidNameAwareLabel, ac);

        field = new LocalizableTextField();
        field.getDocument().addDocumentListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                String value = field.getValue();
                boolean invalid;
                if (value == null) {
                    invalid = false;
                } else {
                    invalid = !isNameValid(value);
                }
                invalidNameAwarePane.setVisible(invalid);
            }
        });
        field.setFont(field.getFont().deriveFont(field.getFont().getSize2D() + 4.f));
        field.setPlaceholder(LOC_PREFIX + "placeholder");
        c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);
        c.gridy++;
        add(field, c);

        c.gridy++;
        add(invalidNameAwarePane, c);

        elySkins = new LocalizableCheckbox("account.button.ely.toggle", true);
        c.gridy++;
        add(elySkins, c);

        final LocalizableButton button = new LocalizableButton(LOC_PREFIX + (mode == PaneMode.EDIT ? "edit" : "save"));
        button.addActionListener(e -> {
            String username = field.getValue();

            if ("разблокировать".equalsIgnoreCase(username)) {
                field.setValue(null);
                unlocked = true;
                return;
            }

            if (!isNameValid(username) && !invalidNameAwareCheckbox.getState()) {
                Alert.showLocError("account.manager.multipane.account-plain.invalid");
                return;
            }

            if (StringUtils.isBlank(username)) {
                Alert.showLocError("account.manager.multipane.add-account.error.no-credentials");
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
                TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(scene.list.getSelected().getUser());
            }

            if (checkUsername != null) {
                StandardAccountPane.removeAccountIfFound(checkUsername, Account.AccountType.PLAIN);
            }

            if (!unlocked) {
                for (String disallowed : DISALLOWED)
                    if (disallowed.equalsIgnoreCase(username))
                        return;
            }

            User user = AccountManager.getPlainAuth().authorize(username, elySkins.getState());
            TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().add(user);
            AccountPlainPane.this.scene.list.select(new Account<>(user));
            AccountPlainPane.this.scene.multipane.showTip("success-" + mode.toString().toLowerCase(java.util.Locale.ROOT));
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
                    TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(selected.getUser());
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
                invalidNameAwareCheckbox.setSelected(!isNameValid(field.getValue()));
                elySkins.setState(((PlainUser) selected.getUser()).isElySkins());
            }
        }
    }

    private static boolean isNameValid(String name) {
        return name != null && name.length() <= 16 && name.chars().noneMatch(ch -> ch <= 32 || ch >= 127);
    }
}
