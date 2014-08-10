package ru.turikhay.tlauncher.ui.accounts.helper;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.util.Arrays;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.accounts.AccountHandler;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public class AccountEditorHelper extends ExtendedLayeredPane {
   static final int MARGIN = 5;
   static final byte LEFT = 0;
   static final byte UP = 1;
   static final byte RIGHT = 2;
   static final byte DOWN = 3;
   private static final long serialVersionUID = -8240523754377261945L;
   private final MainPane pane;
   private final HelperTip[] tips;
   private HelperState state;

   public AccountEditorHelper(AccountEditorScene scene) {
      super(scene);
      AccountHandler handler = scene.handler;
      this.pane = scene.getMainPane();
      this.tips = new HelperTip[]{new HelperTip("add", handler.list.add, handler.list, (byte)3, new HelperState[]{HelperState.PREMIUM, HelperState.FREE}), new HelperTip("username", handler.editor.username, handler.editor, (byte)0, new HelperState[]{HelperState.PREMIUM, HelperState.FREE}), new HelperTip("checkbox", handler.editor.premiumBox, handler.editor, (byte)0, new HelperState[]{HelperState.PREMIUM, HelperState.FREE}), new HelperTip("password", handler.editor.password, handler.editor, (byte)0, new HelperState[]{HelperState.PREMIUM}), new HelperTip("button", handler.editor.save, handler.editor, (byte)0, new HelperState[]{HelperState.PREMIUM, HelperState.FREE}), new HelperTip("exit", handler.list.back, handler.list, (byte)2, new HelperState[]{HelperState.PREMIUM, HelperState.FREE}), new HelperTip("help", handler.list.help, handler.list, (byte)3, new HelperState[]{HelperState.HELP})};
      this.add(this.tips);
      this.setState(HelperState.NONE);
   }

   public HelperState getState() {
      return this.state;
   }

   void updateState() {
      this.setState(this.state);
   }

   public void setState(HelperState state) {
      if (state == null) {
         throw new NullPointerException();
      } else {
         this.state = state;
         HelperState[] var5;
         int var4 = (var5 = HelperState.values()).length;

         int var3;
         for(var3 = 0; var3 < var4; ++var3) {
            HelperState st = var5[var3];
            st.item.setEnabled(!st.equals(state));
         }

         HelperTip step;
         HelperTip[] var22;
         if (state == HelperState.NONE) {
            var4 = (var22 = this.tips).length;

            for(var3 = 0; var3 < var4; ++var3) {
               step = var22[var3];
               if (step.isShowing()) {
                  step.setVisible(false);
               }
            }

         } else {
            var4 = (var22 = this.tips).length;

            for(var3 = 0; var3 < var4; ++var3) {
               step = var22[var3];
               if (Arrays.binarySearch(step.states, 0, step.states.length, state) < 0) {
                  step.setVisible(false);
               } else {
                  LocalizableLabel l = step.label;
                  l.setText("auth.helper." + state.toString() + "." + step.name);
                  Component c = step.component;
                  int cWidth = c.getWidth();
                  int cHeight = c.getHeight();
                  Point cp = this.pane.getLocationOf(c);
                  Component p = step.parent;
                  int pWidth = p.getWidth();
                  int pHeight = p.getHeight();
                  Point pp = this.pane.getLocationOf(p);
                  FontMetrics fm = l.getFontMetrics(l.getFont());
                  Insets i = step.getInsets();
                  int height = i.top + i.bottom + fm.getHeight();
                  int width = i.left + i.right + fm.stringWidth(l.getText());
                  int x;
                  int y;
                  switch(step.alignment) {
                  case 0:
                     x = pp.x - 5 - width;
                     y = cp.y + cHeight / 2 - height / 2;
                     break;
                  case 1:
                     x = cp.x + cWidth / 2 - width / 2;
                     y = pp.y - 5 - height;
                     break;
                  case 2:
                     x = pp.x + pWidth + 5;
                     y = cp.y + cHeight / 2 - height / 2;
                     break;
                  case 3:
                     x = cp.x + cWidth / 2 - width / 2;
                     y = pp.y + pHeight + 5;
                     break;
                  default:
                     throw new IllegalArgumentException("Unknown alignment");
                  }

                  if (x < 0) {
                     x = 0;
                  } else if (x + width > this.getWidth()) {
                     x = this.getWidth() - width;
                  }

                  if (y < 0) {
                     y = 0;
                  } else if (y + height > this.getHeight()) {
                     y = this.getHeight() - height;
                  }

                  step.setVisible(true);
                  step.setBounds(x, y, width, height);
               }
            }

         }
      }
   }

   public void onResize() {
      super.onResize();
      this.updateState();
   }
}
