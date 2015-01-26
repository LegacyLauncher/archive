package ru.turikhay.tlauncher.ui.accounts;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.util.OS;

public class AccountTip extends CenterPanel implements LocalizableComponent {
   public static final int WIDTH = 510;
   private final AccountEditorScene scene;
   public final AccountTip.Tip freeTip;
   public final AccountTip.Tip mojangTip;
   public final AccountTip.Tip elyTip;
   private AccountTip.Tip tip;
   private final EditorPane content;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$minecraft$auth$Account$AccountType;

   public AccountTip(AccountEditorScene sc) {
      super(smallSquareInsets);
      this.scene = sc;
      this.content = new EditorPane();
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
      this.mojangTip = new AccountTip.Tip(Account.AccountType.MOJANG, ImageCache.getRes("mojang-user.png"));
      this.elyTip = new AccountTip.Tip(Account.AccountType.ELY, ImageCache.getRes("ely.png"));
      this.setTip((AccountTip.Tip)null);
   }

   public void setAccountType(Account.AccountType type) {
      if (type != null) {
         switch($SWITCH_TABLE$ru$turikhay$tlauncher$minecraft$auth$Account$AccountType()[type.ordinal()]) {
         case 1:
            this.setTip(this.elyTip);
            return;
         case 2:
            this.setTip(this.mojangTip);
            return;
         case 3:
            this.setTip(this.freeTip);
            return;
         }
      }

      this.setTip((AccountTip.Tip)null);
   }

   public AccountTip.Tip getTip() {
      return this.tip;
   }

   public void setTip(AccountTip.Tip tip) {
      this.tip = tip;
      if (tip == null) {
         this.setVisible(false);
      } else {
         this.setVisible(true);
         StringBuilder builder = new StringBuilder();
         builder.append("<table width=\"").append(510 - this.getInsets().left - this.getInsets().right).append("\" height=\"").append(tip.getHeight()).append("\"><tr><td align=\"center\" valign=\"center\">");
         if (tip.image != null) {
            builder.append("<img src=\"").append(tip.image).append("\" /></td><td align=\"center\" valign=\"center\" width=\"100%\">");
         }

         builder.append(Localizable.get(tip.path));
         builder.append("</td></tr></table>");
         this.setContent(builder.toString(), 510, tip.getHeight());
      }
   }

   void setContent(String text, int width, int height) {
      if (width >= 1 && height >= 1) {
         this.content.setText(text);
         if (OS.CURRENT == OS.LINUX) {
            width = (int)((double)width * 1.2D);
            height = (int)((double)height * 1.2D);
         }

         this.setSize(width, height + this.getInsets().top + this.getInsets().bottom);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public void updateLocale() {
      this.setTip(this.tip);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$minecraft$auth$Account$AccountType() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$minecraft$auth$Account$AccountType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[Account.AccountType.values().length];

         try {
            var0[Account.AccountType.ELY.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[Account.AccountType.FREE.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[Account.AccountType.MOJANG.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$tlauncher$minecraft$auth$Account$AccountType = var0;
         return var0;
      }
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
         return AccountTip.this.tlauncher.getLang().getInteger(this.path + ".height");
      }
   }
}
