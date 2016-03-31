package ru.turikhay.tlauncher.ui.accounts;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.util.SwingUtil;

public class AccountTip extends CenterPanel implements LocalizableComponent {
   public static final int WIDTH = SwingUtil.magnify(510);
   private final AccountEditorScene scene;
   public final AccountTip.Tip freeTip;
   public final AccountTip.Tip mojangTip;
   public final AccountTip.Tip elyTip;
   private AccountTip.Tip tip;
   private final EditorPane content;

   public AccountTip(AccountEditorScene sc) {
      super(smallSquareInsets);
      this.setMagnifyGaps(false);
      this.scene = sc;
      this.content = new EditorPane(this.getFont().deriveFont(TLauncherFrame.getFontSize()));
      this.content.addMouseListener(new MouseListener() {
         public void mouseClicked(MouseEvent e) {
            if (!AccountTip.this.isVisible()) {
               e.consume();
            }

         }

         public void mousePressed(MouseEvent e) {
            if (!AccountTip.this.isVisible()) {
               e.consume();
            }

         }

         public void mouseReleased(MouseEvent e) {
            if (!AccountTip.this.isVisible()) {
               e.consume();
            }

         }

         public void mouseEntered(MouseEvent e) {
            if (!AccountTip.this.isVisible()) {
               e.consume();
            }

         }

         public void mouseExited(MouseEvent e) {
            if (!AccountTip.this.isVisible()) {
               e.consume();
            }

         }
      });
      this.add(this.content);
      this.freeTip = new AccountTip.Tip(Account.AccountType.FREE, (URL)null);
      this.mojangTip = new AccountTip.Tip(Account.AccountType.MOJANG, Images.getRes("mojang-user.png"));
      this.elyTip = new AccountTip.Tip(Account.AccountType.ELY, Images.getRes("ely.png"));
      this.setTip((AccountTip.Tip)null);
   }

   public void setAccountType(Account.AccountType type) {
      if (type != null) {
         switch(type) {
         case ELY:
            this.setTip(this.elyTip);
            return;
         case MOJANG:
            this.setTip(this.mojangTip);
            return;
         case FREE:
            this.setTip(this.freeTip);
            return;
         }
      }

      this.setTip((AccountTip.Tip)null);
   }

   public void setTip(AccountTip.Tip tip) {
      this.tip = tip;
      if (tip == null) {
         this.setVisible(false);
      } else {
         this.setVisible(true);
         StringBuilder builder = new StringBuilder();
         builder.append("<table width=\"").append(WIDTH - this.getInsets().left - this.getInsets().right).append("\" height=\"").append(tip.getHeight()).append("\"><tr><td align=\"center\" valign=\"center\">");
         if (tip.image != null) {
            builder.append("<img src=\"").append(tip.image).append("\" /></td><td align=\"center\" valign=\"center\" width=\"100%\">");
         }

         builder.append(Localizable.get(tip.path));
         builder.append("</td></tr></table>");
         this.setContent(builder.toString(), WIDTH, tip.getHeight());
      }

   }

   void setContent(String text, int width, int height) {
      if (width >= 1 && height >= 1) {
         this.content.setText(text);
         this.setSize(width, height + this.getInsets().top + this.getInsets().bottom);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public void updateLocale() {
      this.setTip(this.tip);
   }

   public class Tip {
      private final Account.AccountType type;
      private final String path;
      private final URL image;

      Tip(Account.AccountType type, URL image) {
         this.type = type;
         this.path = "auth.tip." + type;
         this.image = image;
      }

      public int getHeight() {
         return SwingUtil.magnify(AccountTip.this.tlauncher.getLang().getInteger(this.path + ".height"));
      }
   }
}
