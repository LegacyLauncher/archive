package ru.turikhay.tlauncher.ui.scenes;

import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.accounts.AccountEditor;
import ru.turikhay.tlauncher.ui.accounts.AccountHandler;
import ru.turikhay.tlauncher.ui.accounts.AccountList;
import ru.turikhay.tlauncher.ui.accounts.AccountTip;

public class AccountEditorScene extends PseudoScene {
   private static final long serialVersionUID = -151325577614420989L;
   private final int ELEMENT_WIDTH = 225;
   private final int ELEMENT_HEIGHT = 255;
   public final AccountEditor editor = new AccountEditor(this);
   public final AccountList list;
   public final AccountHandler handler;
   public final AccountTip tip;

   public AccountEditorScene(MainPane main) {
      super(main);
      this.editor.setSize(225, 255);
      this.add(this.editor);
      this.list = new AccountList(this);
      this.list.setSize(225, 255);
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
      int heh = 127;
      int y = hh - heh;
      int MARGIN = 10;
      this.editor.setLocation(hw - 225 - MARGIN, y);
      this.list.setLocation(hw + MARGIN, y);
      this.tip.setLocation(hw - 255, y + Math.max(this.editor.getHeight(), this.list.getHeight()) + MARGIN);
   }
}
