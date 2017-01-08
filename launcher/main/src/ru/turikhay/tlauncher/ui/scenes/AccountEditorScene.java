package ru.turikhay.tlauncher.ui.scenes;

import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.accounts.AccountEditor;
import ru.turikhay.tlauncher.ui.accounts.AccountHandler;
import ru.turikhay.tlauncher.ui.accounts.AccountList;
import ru.turikhay.tlauncher.ui.accounts.AccountTip;
import ru.turikhay.util.SwingUtil;

public class AccountEditorScene extends PseudoScene {
    public static final int ELEMENT_WIDTH = SwingUtil.magnify(255), ELEMENT_HEIGHT = ELEMENT_WIDTH;
    public final AccountEditor editor = new AccountEditor(this);
    public final AccountList list;
    public final AccountHandler handler;
    public final AccountTip tip;

    public AccountEditorScene(MainPane main) {
        super(main);
        editor.setSize(ELEMENT_WIDTH, ELEMENT_HEIGHT);
        add(editor);
        list = new AccountList(this);
        list.setSize(ELEMENT_WIDTH, ELEMENT_HEIGHT);
        add(list);
        handler = new AccountHandler(this);
        tip = new AccountTip(this);
        add(tip);
        handler.notifyEmpty();
    }

    public void setShown(boolean shown, boolean animate) {
        super.setShown(shown, animate);
        editor.updateElyToggle();
    }

    public void onResize() {
        super.onResize();
        int hw = getWidth() / 2;
        int hh = getHeight() / 2;
        int heh = ELEMENT_HEIGHT / 2;
        int y = hh - heh;
        byte MARGIN = 10;
        editor.setLocation(hw - ELEMENT_WIDTH - MARGIN, y);
        list.setLocation(hw + MARGIN, y);
        tip.setLocation(hw - ELEMENT_WIDTH, y + Math.max(editor.getHeight(), list.getHeight()) + MARGIN);
    }
}
