package ru.turikhay.tlauncher.ui.swing;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;

public class AccountCellRenderer implements ListCellRenderer {
   public static final Account EMPTY = Account.randomAccount();
   public static final Account MANAGE = Account.randomAccount();
   private static final ImageIcon MANAGE_ICON = Images.getIcon("gear.png");
   private static final ImageIcon MOJANG_USER_ICON = Images.getIcon("mojang-user.png");
   private static final ImageIcon ELY_USER_ICON = Images.getIcon("ely.png");
   private final DefaultListCellRenderer defaultRenderer;
   private AccountCellRenderer.AccountCellType type;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$minecraft$auth$Account$AccountType;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$AccountCellRenderer$AccountCellType;

   public AccountCellRenderer(AccountCellRenderer.AccountCellType type) {
      if (type == null) {
         throw new NullPointerException("CellType cannot be NULL!");
      } else {
         this.defaultRenderer = new DefaultListCellRenderer();
         this.type = type;
      }
   }

   public AccountCellRenderer() {
      this(AccountCellRenderer.AccountCellType.PREVIEW);
   }

   public AccountCellRenderer.AccountCellType getType() {
      return this.type;
   }

   public void setType(AccountCellRenderer.AccountCellType type) {
      if (type == null) {
         throw new NullPointerException("CellType cannot be NULL!");
      } else {
         this.type = type;
      }
   }

   public Component getListCellRendererComponent(JList list, Account value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel renderer = (JLabel)this.defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      renderer.setAlignmentY(0.5F);
      if (value != null && !value.equals(EMPTY)) {
         if (value.equals(MANAGE)) {
            renderer.setText(Localizable.get("account.manage"));
            renderer.setIcon(MANAGE_ICON);
         } else {
            Icon icon = null;
            switch($SWITCH_TABLE$ru$turikhay$tlauncher$minecraft$auth$Account$AccountType()[value.getType().ordinal()]) {
            case 1:
               icon = TLauncher.getInstance().getElyManager().isRefreshing() ? ELY_USER_ICON.getDisabledInstance() : ELY_USER_ICON;
               break;
            case 2:
               icon = MOJANG_USER_ICON;
            }

            if (icon != null) {
               renderer.setIcon((Icon)icon);
               renderer.setFont(renderer.getFont().deriveFont(1));
            }

            switch($SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$AccountCellRenderer$AccountCellType()[this.type.ordinal()]) {
            case 2:
               if (!value.hasUsername()) {
                  renderer.setText(Localizable.get("account.creating"));
                  renderer.setFont(renderer.getFont().deriveFont(2));
               } else {
                  renderer.setText(value.getUsername());
               }
               break;
            default:
               if (value.getType() == Account.AccountType.ELY && TLauncher.getInstance().getElyManager().isRefreshing()) {
                  renderer.setText(value.getDisplayName() + " " + Localizable.get("account.loading.ely"));
               } else {
                  renderer.setText(value.getDisplayName());
               }
            }
         }
      } else {
         renderer.setText(Localizable.get("account.empty"));
      }

      return renderer;
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

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$AccountCellRenderer$AccountCellType() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$AccountCellRenderer$AccountCellType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[AccountCellRenderer.AccountCellType.values().length];

         try {
            var0[AccountCellRenderer.AccountCellType.EDITOR.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[AccountCellRenderer.AccountCellType.PREVIEW.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$AccountCellRenderer$AccountCellType = var0;
         return var0;
      }
   }

   public static enum AccountCellType {
      PREVIEW,
      EDITOR;
   }
}
