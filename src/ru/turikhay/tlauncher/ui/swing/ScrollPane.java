package ru.turikhay.tlauncher.ui.swing;

import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

public class ScrollPane extends JScrollPane {
   private static final long serialVersionUID = -8296804097817210847L;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy;

   public ScrollPane(Component view, ScrollPane.ScrollBarPolicy vertical, ScrollPane.ScrollBarPolicy horizontal) {
      super(view);
      this.setOpaque(false);
      this.getViewport().setOpaque(false);
      this.setBorder((Border)null);
      this.setVBPolicy(vertical);
      this.setHBPolicy(horizontal);
   }

   public ScrollPane(Component view, ScrollPane.ScrollBarPolicy generalPolicy) {
      this(view, generalPolicy, generalPolicy);
   }

   public ScrollPane(Component view) {
      this(view, ScrollPane.ScrollBarPolicy.AS_NEEDED);
   }

   public void setVerticalScrollBarPolicy(ScrollPane.ScrollBarPolicy policy) {
      byte i_policy;
      switch($SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy()[policy.ordinal()]) {
      case 1:
         i_policy = 22;
         break;
      case 2:
         i_policy = 20;
         break;
      case 3:
         i_policy = 21;
         break;
      default:
         throw new IllegalArgumentException();
      }

      super.setVerticalScrollBarPolicy(i_policy);
   }

   public void setHorizontalScrollBarPolicy(ScrollPane.ScrollBarPolicy policy) {
      byte i_policy;
      switch($SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy()[policy.ordinal()]) {
      case 1:
         i_policy = 32;
         break;
      case 2:
         i_policy = 30;
         break;
      case 3:
         i_policy = 31;
         break;
      default:
         throw new IllegalArgumentException();
      }

      super.setHorizontalScrollBarPolicy(i_policy);
   }

   public void setVBPolicy(ScrollPane.ScrollBarPolicy policy) {
      this.setVerticalScrollBarPolicy(policy);
   }

   public void setHBPolicy(ScrollPane.ScrollBarPolicy policy) {
      this.setHorizontalScrollBarPolicy(policy);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[ScrollPane.ScrollBarPolicy.values().length];

         try {
            var0[ScrollPane.ScrollBarPolicy.ALWAYS.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[ScrollPane.ScrollBarPolicy.AS_NEEDED.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[ScrollPane.ScrollBarPolicy.NEVER.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy = var0;
         return var0;
      }
   }

   public static enum ScrollBarPolicy {
      ALWAYS,
      AS_NEEDED,
      NEVER;
   }
}
