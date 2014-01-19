package com.turikhay.tlauncher.ui;

public class UsernameField extends LocalizableTextField {
   private static final long serialVersionUID = -5813187607562947592L;
   UsernameField.UsernameState state;
   String username;
   private boolean check;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$ui$UsernameField$UsernameState;

   UsernameField(CenterPanel pan, UsernameField.UsernameState state) {
      super(pan, "profile.username", (String)null, 20);
      this.setState(state);
   }

   public UsernameField.UsernameState getState() {
      return this.state;
   }

   public void setState(UsernameField.UsernameState state) {
      if (state == null) {
         throw new NullPointerException();
      } else {
         this.state = state;
      }
   }

   public boolean getCheck() {
      return this.check;
   }

   public void setCheck(boolean c) {
      this.check = c;
      this.check();
   }

   public boolean check() {
      return this.check(true);
   }

   protected boolean check(String text) {
      return this.check(text, true);
   }

   public boolean check(boolean canBeEmpty) {
      String text = this.getValue();
      return this.check(text, canBeEmpty) ? this.ok() : this.wrong(l.get("username.incorrect"));
   }

   protected boolean check(String text, boolean canBeEmpty) {
      if (text == null) {
         return false;
      } else {
         if (this.check) {
            String regexp;
            switch($SWITCH_TABLE$com$turikhay$tlauncher$ui$UsernameField$UsernameState()[this.state.ordinal()]) {
            case 1:
               regexp = "^[A-Za-z0-9_|\\-|\\.]" + (canBeEmpty ? "*" : "+") + "$";
               break;
            case 2:
               regexp = "^.*$";
               break;
            default:
               throw new IllegalArgumentException("Unknown field state!");
            }

            if (!text.matches(regexp)) {
               return false;
            }
         }

         this.username = text;
         return true;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$turikhay$tlauncher$ui$UsernameField$UsernameState() {
      int[] var10000 = $SWITCH_TABLE$com$turikhay$tlauncher$ui$UsernameField$UsernameState;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[UsernameField.UsernameState.values().length];

         try {
            var0[UsernameField.UsernameState.EMAIL.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[UsernameField.UsernameState.USERNAME.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$turikhay$tlauncher$ui$UsernameField$UsernameState = var0;
         return var0;
      }
   }

   static enum UsernameState {
      USERNAME,
      EMAIL;
   }
}
