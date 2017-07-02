package ru.turikhay.tlauncher.ui.account;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableTextField;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.text.ExtendedTextField;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class AccountAddFree extends ExtendedPanel implements AccountMultipaneCompCloseable {
    private static final String[] DISALLOWED = {"turikhay", "nik_mmzd", "mcmodder", "DarikXPlay", "ErickSkrauch"};
    private final String LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + multipaneName() + ".";

    private final AccountManagerScene scene;
    private final LocalizableTextField field;

    private boolean unlocked;

    public AccountAddFree(final AccountManagerScene scene) {
        this.scene = scene;

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
        field.setFont(field.getFont().deriveFont(field.getFont().getSize2D() + 4.f));
        field.setPlaceholder(LOC_PREFIX + "placeholder");
        c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);
        c.gridy++;
        add(field, c);

        final LocalizableButton button = new LocalizableButton(LOC_PREFIX + "save");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = field.getValue();

                if("разблокировать".equalsIgnoreCase(username)) {
                    field.setValue(null);
                    unlocked = true;
                    return;
                }

                if(StringUtils.isBlank(username)) {
                    Alert.showLocError("account.manager.multipane.add-account.error.no-credentials");
                    return;
                }

                Account existing = TLauncher.getInstance().getProfileManager().getAuthDatabase().getByUsername(username, Account.AccountType.FREE);
                if(existing != null) {
                    Alert.showLocError("account.manager.multipane.add-account.error.exists");
                    return;
                }

                if(!unlocked) {
                    for (String disallowed : DISALLOWED)
                        if (disallowed.equalsIgnoreCase(username))
                            return;
                }

                Account account = new Account();
                account.setUsername(username);
                account.setType(Account.AccountType.FREE);
                TLauncher.getInstance().getProfileManager().getAuthDatabase().registerAccount(account);
                try {
                    TLauncher.getInstance().getProfileManager().saveProfiles();
                } catch (IOException e1) {
                    Alert.showError(e1);
                    return;
                }
                AccountAddFree.this.scene.list.select(account);
                AccountAddFree.this.scene.multipane.showTip("success");
            }
        });
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setIcon(Images.getIcon("plus.png", 24));
        c.gridy++;
        add(button, c);

        field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.doClick();
                if (unlocked) {
                    field.grabFocus();
                }
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
        return "add-account-free";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown() {
        field.setValue(null);
    }
}
