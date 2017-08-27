package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccountAdd extends VPanel implements AccountMultipaneCompCloseable, Blockable {
    private final String LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + multipaneName() + ".";
    private final String ACCOUNT_TYPE_PREFIX =  LOC_PREFIX +"type.";

    private final AccountManagerScene scene;

    private final ExtendedPanel grid;
    private final GridBagConstraints c;

    private final ExtendedButton mojang, ely, free, mcleaks, idontknow;

    public AccountAdd(final AccountManagerScene scene) {
        this.scene = scene;

        grid = new ExtendedPanel();
        //grid.setBorder(BorderFactory.createLineBorder(Color.red));
        grid.setAlignmentX(0);
        grid.setLayout(new GridBagLayout());

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.gridy = -1;

        mojang = addRow("mojang.png", ACCOUNT_TYPE_PREFIX + "mojang", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AccountAdd.this.scene.multipane.showTip("add-account-mojang");
            }
        });
        free = addRow("user-circle-o.png", ACCOUNT_TYPE_PREFIX + "free", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AccountAdd.this.scene.multipane.showTip("add-account-plain");
            }
        });
        ely = addRow("ely.png", ACCOUNT_TYPE_PREFIX + "ely", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AccountAdd.this.scene.multipane.showTip("add-account-ely");
            }
        });
        mcleaks = addRow("mcleaks.png", ACCOUNT_TYPE_PREFIX + "mcleaks", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AccountAdd.this.scene.multipane.showTip("add-account-mcleaks");
            }
        });
        idontknow = addRow("info.png", LOC_PREFIX + "hint", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Blocker.toggle(AccountAdd.this, "idontknow");
            }
        });

        /*for(Account.AccountType accountType : Account.AccountType.values()) {
            c.gridy++;

            ExtendedButton button = new ExtendedButton();
            button.setPreferredSize(SwingUtil.magnify(new Dimension(48, 48)));
            button.setIcon(Images.getIcon(accountType.getIcon() == null? "plus.png" : accountType.getIcon(), 32));
            c.gridx = 0;
            c.weightx = 0;
            c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);
            c.fill = GridBagConstraints.NONE;
            grid.add(button, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.insets = new Insets(0, SwingUtil.magnify(10), 0, 0);
            c.fill = GridBagConstraints.HORIZONTAL;
            grid.add(new LocalizableLabel("account.manager.multipane.add-account.type." + accountType.name().toLowerCase()), c);
        }

        c.gridy++;

        ExtendedButton button = new ExtendedButton();
        button.setPreferredSize(SwingUtil.magnify(new Dimension(48, 48)));
        button.setIcon(Images.getIcon("info.png", 32));
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        grid.add(button, c);

        c.gridx = 1;
        c.weightx = 1.0;
        c.insets = new Insets(0, SwingUtil.magnify(10), 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        grid.add(new LocalizableLabel("account.manager.multipane.add-account.hint"), c);*/

        c.gridy++;
        c.gridx = 1;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0, 0, 0, 0);
        c.weighty = 1.0;
        grid.add(new ExtendedPanel(), c);

        add(grid);
    }

    private ExtendedButton addRow(String image, String label, ActionListener action) {
        c.gridy++;

        LocalizableButton button = new LocalizableButton(label);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(SwingUtil.magnify(16));
        button.addActionListener(action);
        //button.setPreferredSize(SwingUtil.magnify(new Dimension(48, 48)));
        button.setIcon(Images.getIcon(image, 32));
        c.gridx = 0;
        //c.weightx = 0;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);
        //c.fill = GridBagConstraints.NONE;
        c.fill = GridBagConstraints.HORIZONTAL;
        grid.add(button, c);

        /*c.gridx = 1;
        c.weightx = 1.0;
        c.insets = new Insets(0, SwingUtil.magnify(10), 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        grid.add(new LocalizableLabel(label), c);*/

        return button;
    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return "add-account";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        Blocker.unblock(AccountAdd.this, "idontknow");
    }

    @Override
    public void multipaneClosed() {
        Blocker.unblock(AccountAdd.this, "idontknow");
    }

    @Override
    public void block(Object var1) {
        if(!"idontknow".equals(var1)) {
            Blocker.blockComponents(var1, free, idontknow);
        }
        Blocker.blockComponents(var1, mojang, ely, mcleaks);
    }

    @Override
    public void unblock(Object var1) {
        Blocker.unblockComponents(var1, mojang, free, ely, mcleaks, idontknow);
    }
}