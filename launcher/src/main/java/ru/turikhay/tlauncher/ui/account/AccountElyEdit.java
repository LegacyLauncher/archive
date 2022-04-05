package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableTextField;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccountElyEdit extends ExtendedPanel implements AccountMultipaneCompCloseable {
    private final String LOC_PREFIX;

    private final AccountManagerScene scene;
    private final LocalizableTextField field;

    public AccountElyEdit(final AccountManagerScene scene) {
        this.scene = scene;

        LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + "account-ely.";

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;

        LocalizableLabel label = new LocalizableLabel(LOC_PREFIX + "label");
        label.setIcon(Images.getIcon24("logo-ely"));
        c.gridy++;
        add(label, c);

        field = new LocalizableTextField();
        field.setEditable(false);
        field.setFont(field.getFont().deriveFont(field.getFont().getSize2D() + 4.f));
        c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);
        c.gridy++;
        add(field, c);

        LocalizableButton removeButton = new LocalizableButton("account.manager.multipane.remove-account");
        removeButton.setIcon(Images.getIcon16("remove"));
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Account selected = scene.list.getSelected();
                if(selected != null && selected.getType() == Account.AccountType.ELY) {
                    TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(selected.getUser());
                }
                scene.multipane.goBack();
            }
        });

        c.insets = new Insets(SwingUtil.magnify(12), 0, 0, 0);
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_START;
        add(removeButton, c);
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
        return "edit-account-ely";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        field.setValue(null);

        Account selected = scene.list.getSelected();
        if(selected != null && selected.getType() == Account.AccountType.ELY) {
            field.setValue(selected.getUsername());
        }
    }
}
