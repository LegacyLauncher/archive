package com.turikhay.tlauncher.ui.swing;

import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.tlauncher.ui.loc.Localizable;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class AccountCellRenderer implements ListCellRenderer {
   public static final Account EMPTY = Account.randomAccount();
   public static final Account MANAGE = Account.randomAccount();
   private static final Icon MANAGE_ICON = new ImageIcon(ImageCache.getImage("gear.png"));
   private static final Icon CROWN_ICON = new ImageIcon(ImageCache.getImage("crown.png"));
   private final DefaultListCellRenderer defaultRenderer;
   private AccountCellRenderer.AccountCellType type;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$ui$swing$AccountCellRenderer$AccountCellType;

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
            switch($SWITCH_TABLE$com$turikhay$tlauncher$ui$swing$AccountCellRenderer$AccountCellType()[this.type.ordinal()]) {
            case 2:
               if (value.hasLicense()) {
                  renderer.setIcon(CROWN_ICON);
               }

               if (!value.hasUsername()) {
                  renderer.setText(Localizable.get("account.creating"));
                  renderer.setFont(renderer.getFont().deriveFont(2));
                  break;
               }
            default:
               renderer.setText(value.getUsername());
            }
         }
      } else {
         renderer.setText(Localizable.get("account.empty"));
      }

      return renderer;
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$turikhay$tlauncher$ui$swing$AccountCellRenderer$AccountCellType() {
      int[] var10000 = $SWITCH_TABLE$com$turikhay$tlauncher$ui$swing$AccountCellRenderer$AccountCellType;
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

         $SWITCH_TABLE$com$turikhay$tlauncher$ui$swing$AccountCellRenderer$AccountCellType = var0;
         return var0;
      }
   }

   public static enum AccountCellType {
      PREVIEW,
      EDITOR;
   }
}
