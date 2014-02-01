package com.turikhay.tlauncher.ui.scenes;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.accounts.AccountEditor;
import com.turikhay.tlauncher.ui.accounts.AccountHandler;
import com.turikhay.tlauncher.ui.accounts.AccountList;
import com.turikhay.tlauncher.ui.accounts.helper.AccountEditorHelper;
import com.turikhay.tlauncher.ui.accounts.helper.HelperState;

public class AccountEditorScene extends PseudoScene {
   private static final long serialVersionUID = -151325577614420989L;
   private final int ELEMENT_WIDTH = 225;
   private final int ELEMENT_HEIGHT = 225;
   private final int MARGIN = 10;
   public final AccountEditor editor = new AccountEditor(this);
   public final AccountList list;
   public final AccountEditorHelper helper;
   public final AccountHandler handler;

   public AccountEditorScene(MainPane main) {
      super(main);
      this.editor.setSize(225, 225);
      this.add(this.editor);
      this.list = new AccountList(this);
      this.list.setSize(225, 225);
      this.add(this.list);
      this.handler = new AccountHandler(this);
      this.helper = new AccountEditorHelper(this);
      this.add(this.helper);
   }

   public void setShown(boolean shown, boolean animate) {
      super.setShown(shown, animate);
      if (shown && this.list.model.isEmpty()) {
         this.helper.setState(HelperState.HELP);
      } else {
         this.helper.setState(HelperState.NONE);
      }

   }

   public void onResize() {
      super.onResize();
      int hw = this.getWidth() / 2;
      int hh = this.getHeight() / 2;
      int heh = 112;
      int y = hh - heh;
      this.editor.setLocation(hw - 225 - 10, y);
      this.list.setLocation(hw + 10, y);
   }
}
