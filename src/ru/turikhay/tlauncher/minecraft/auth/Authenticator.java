package ru.turikhay.tlauncher.minecraft.auth;

import java.util.UUID;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

public abstract class Authenticator {
   protected final Account account;
   private final String logPrefix = '[' + this.getClass().getSimpleName() + ']';
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$minecraft$auth$Account$AccountType;

   protected Authenticator(Account account) {
      if (account == null) {
         throw new NullPointerException("account");
      } else {
         this.account = account;
      }
   }

   public final Account getAccount() {
      return this.account;
   }

   public boolean pass(AuthenticatorListener l) {
      if (l != null) {
         l.onAuthPassing(this);
      }

      try {
         this.pass();
      } catch (Exception var3) {
         this.log("Cannot authenticate:", var3);
         if (l != null) {
            l.onAuthPassingError(this, var3);
         }

         return false;
      }

      if (l != null) {
         l.onAuthPassed(this);
      }

      return true;
   }

   public void asyncPass(final AuthenticatorListener l) {
      AsyncThread.execute(new Runnable() {
         public void run() {
            Authenticator.this.pass(l);
         }
      });
   }

   protected abstract void pass() throws AuthenticatorException;

   protected void log(Object... o) {
      U.log(this.logPrefix, o);
   }

   public static Authenticator instanceFor(Account account) {
      if (account == null) {
         throw new NullPointerException("account");
      } else {
         U.log("instancefor:", account);
         switch($SWITCH_TABLE$ru$turikhay$tlauncher$minecraft$auth$Account$AccountType()[account.getType().ordinal()]) {
         case 1:
            return new ElyAuthenticator(account);
         case 2:
            return new MojangAuthenticator(account);
         default:
            throw new IllegalArgumentException("illegal account type");
         }
      }
   }

   protected static UUID getClientToken() {
      return TLauncher.getInstance().getProfileManager().getClientToken();
   }

   protected static void setClientToken(String uuid) {
      TLauncher.getInstance().getProfileManager().setClientToken(uuid);
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
}
