package ru.turikhay.tlauncher.ui.scenes;

import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.account.*;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.util.SwingUtil;

import java.awt.*;

public class AccountManagerScene extends PseudoScene {
    private static final int LIST_WIDTH = 300, MULTIPANE_WIDTH = 325, HEIGHT = 325, GAP = 15;

    public final AccountList list;
    public final AccountMultipane multipane;

    public AccountManagerScene(MainPane main) {
        super(main);

        this.list = new AccountList(this);
        list.setSize(SwingUtil.magnify(new Dimension(LIST_WIDTH, HEIGHT)));
        add(list);

        this.multipane = new AccountMultipane(this);
        multipane.setSize(SwingUtil.magnify(new Dimension(MULTIPANE_WIDTH, HEIGHT)));
        add(multipane);

        multipane.registerTip(new AccountWelcomeTip());
        multipane.registerTip(new AccountAdd(this));

        multipane.registerTip(new AccountPlainPane(this, PaneMode.ADD));
        multipane.registerTip(new AccountPlainPane(this, PaneMode.EDIT));

        multipane.registerTip(new AccountMojangPane(this, PaneMode.ADD));
        multipane.registerTip(new AccountMojangPane(this, PaneMode.EDIT));

        multipane.registerTip(new AccountElyLegacyPane(this, PaneMode.ADD));
        multipane.registerTip(new AccountElyLegacyPane(this, PaneMode.EDIT));

        multipane.registerTip(new AccountElyStart(this));
        multipane.registerTip(new AccountElyProcess(this));
        multipane.registerTip(new NoAccountEdit(this, Account.AccountType.ELY));

        multipane.registerTip(new AccountMcleaksStart(this));
        multipane.registerTip(new AccountMcleaksPane(this, PaneMode.ADD));
        multipane.registerTip(new AccountMcleaksPane(this, PaneMode.EDIT));

        multipane.registerTip(new AccountMinecraftProcess(this));
        multipane.registerTip(new NoAccountEdit(this, Account.AccountType.MINECRAFT));

        //multipane.registerTip(new AccountAddMojang(this));
        //multipane.registerTip(new AccountAddEly(this));

        multipane.registerTip(new AccountSuccess(PaneMode.ADD));
        multipane.registerTip(new AccountSuccess(PaneMode.EDIT));
    }

    public void setShown(boolean shown, boolean animate) {
        super.setShown(shown, animate);
        if (shown) {
            multipane.showTip("welcome");
        }
    }

    @Override
    public void onResize() {
        super.onResize();

        int gap = SwingUtil.magnify(GAP),
                width = list.getWidth() + gap + multipane.getWidth(),
                hw = getWidth() / 2, y = getHeight() / 2 - SwingUtil.magnify(HEIGHT) / 2;

        list.setLocation(hw - width / 2, y);
        multipane.setLocation(list.getX() + list.getWidth() + gap, y);
    }

    @Override
    public void block(Object reason) {
        //super.block(reason);
        Blocker.block(list, reason);
    }

    @Override
    public void unblock(Object reason) {
        //super.unblock(reason);
        Blocker.unblock(list, reason);
    }
}
