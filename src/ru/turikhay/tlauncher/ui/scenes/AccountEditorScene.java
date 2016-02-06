package ru.turikhay.tlauncher.ui.scenes;

import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.accounts.AccountEditor;
import ru.turikhay.tlauncher.ui.accounts.AccountHandler;
import ru.turikhay.tlauncher.ui.accounts.AccountList;
import ru.turikhay.tlauncher.ui.accounts.AccountTip;
import ru.turikhay.util.SwingUtil;

public class AccountEditorScene extends PseudoScene {
   private final int ELEMENT_WIDTH = SwingUtil.magnify(255);
   private final int ELEMENT_HEIGHT;
   public final AccountEditor editor;
   public final AccountList list;
   public final AccountHandler handler;
   public final AccountTip tip;

   public AccountEditorScene(MainPane main) {
      super(main);
      this.ELEMENT_HEIGHT = this.ELEMENT_WIDTH;
      this.editor = new AccountEditor(this);
      this.editor.setSize(this.ELEMENT_WIDTH, this.ELEMENT_HEIGHT);
      this.add(this.editor);
      this.list = new AccountList(this);
      this.list.setSize(this.ELEMENT_WIDTH, this.ELEMENT_HEIGHT);
      this.add(this.list);
      this.handler = new AccountHandler(this);
      this.tip = new AccountTip(this);
      this.add(this.tip);
      this.handler.notifyEmpty();
   }

   public void onResize() {
      super.onResize();
      int hw = this.getWidth() / 2;
      int hh = this.getHeight() / 2;
      int heh = this.ELEMENT_HEIGHT / 2;
      int y = hh - heh;
      byte MARGIN = 10;
      this.editor.setLocation(hw - this.ELEMENT_WIDTH - MARGIN, y);
      this.list.setLocation(hw + MARGIN, y);
      this.tip.setLocation(hw - this.ELEMENT_WIDTH, y + Math.max(this.editor.getHeight(), this.list.getHeight()) + MARGIN);
   }
}
