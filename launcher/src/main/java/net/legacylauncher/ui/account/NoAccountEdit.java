package net.legacylauncher.ui.account;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.loc.LocalizableLabel;
import net.legacylauncher.ui.loc.LocalizableTextField;
import net.legacylauncher.ui.scenes.AccountManagerScene;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.util.SwingUtil;

import java.awt.*;

public class NoAccountEdit extends ExtendedPanel implements AccountMultipaneCompCloseable {

    private final AccountManagerScene scene;
    private final Account.AccountType type;
    private final LocalizableTextField field;

    public NoAccountEdit(final AccountManagerScene scene, final Account.AccountType type) {
        this.scene = scene;
        this.type = type;

        String LOC_PREFIX = LOC_PREFIX_PATH + "account-" + type + ".";

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;

        LocalizableLabel label = new LocalizableLabel(LOC_PREFIX + "label");
        label.setIcon(Images.getIcon24(type.getIcon()));
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
        removeButton.addActionListener(e -> {
            Account<?> selected = scene.list.getSelected();
            if (selected != null && selected.getType() == type) {
                LegacyLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(selected.getUser());
            }
            scene.multipane.goBack();
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
        return "edit-account-" + type;
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        field.setValue(null);

        Account<?> selected = scene.list.getSelected();
        if (selected != null && selected.getType().equals(type)) {
            field.setValue(selected.getUsername());
        }
    }
}
