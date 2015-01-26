package ru.turikhay.tlauncher.ui.accounts;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.listener.AuthUIListener;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.util.U;

public class AccountHandler {
   private final AccountEditorScene scene;
   public final AccountList list;
   public final AccountEditor editor;
   private final ProfileManager manager = TLauncher.getInstance().getProfileManager();
   private final AuthUIListener listener;
   private Account lastAccount;
   private Account tempAccount;

   public AccountHandler(AccountEditorScene sc) {
      this.scene = sc;
      this.list = this.scene.list;
      this.editor = this.scene.editor;
      this.listener = new AuthUIListener(false, new AuthenticatorListener() {
         public void onAuthPassing(Authenticator auth) {
            AccountHandler.this.block();
         }

         public void onAuthPassingError(Authenticator auth, Throwable e) {
            AccountHandler.this.unblock();
         }

         public void onAuthPassed(Authenticator auth) {
            TLauncher.getInstance().getElyManager().setRefreshAllowed(true);
            TLauncher.getInstance().getElyManager().refreshComponent();
            AccountHandler.this.unblock();
            AccountHandler.this.registerTemp();
         }
      });
   }

   public void selectAccount(Account acc) {
      if (acc != null) {
         if (!acc.equals((Account)this.list.list.getSelectedValue())) {
            this.list.list.setSelectedValue(acc, true);
         }
      }
   }

   void refreshEditor(Account account) {
      if (account == null) {
         this.clearEditor();
      } else if (!account.equals(this.lastAccount)) {
         this.lastAccount = account;
         Blocker.unblock((Blockable)this.editor, (Object)"empty");
         this.editor.fill(account);
         if (!account.equals(this.tempAccount)) {
            this.scene.getMainPane().defaultScene.loginForm.accounts.setAccount(this.lastAccount);
         }

         this.scene.tip.setAccountType(account.getType());
      }
   }

   void clearEditor() {
      this.lastAccount = null;
      this.editor.clear();
      this.notifyEmpty();
   }

   void saveEditor() {
      if (this.lastAccount != null) {
         Account acc = this.editor.get();
         if (acc.getUsername() == null) {
            Alert.showLocError("auth.error.nousername");
         } else {
            this.lastAccount.complete(acc);
            U.log(this.lastAccount.isFree());
            if (!this.lastAccount.isFree()) {
               if (this.lastAccount.getAccessToken() == null && this.lastAccount.getPassword() == null) {
                  Alert.showLocError("auth.error.nopass");
                  return;
               }

               Authenticator.instanceFor(this.lastAccount).asyncPass(this.listener);
            } else {
               this.registerTemp();
               this.listener.saveProfiles();
            }

         }
      }
   }

   void exitEditor() {
      this.scene.getMainPane().openDefaultScene();
      this.listener.saveProfiles();
      this.list.list.clearSelection();
      this.scene.tip.setAccountType((Account.AccountType)null);
      this.notifyEmpty();
   }

   void addAccount() {
      if (this.tempAccount == null) {
         this.tempAccount = new Account();
         this.list.model.addElement(this.tempAccount);
         this.list.list.setSelectedValue(this.tempAccount, true);
         this.refreshEditor(this.tempAccount);
      }
   }

   void removeAccount() {
      if (this.lastAccount != null) {
         Account acc = this.lastAccount;
         int num = this.list.model.indexOf(this.lastAccount) - 1;
         this.list.model.removeElement(this.lastAccount);
         this.lastAccount = acc;
         if (this.tempAccount == null) {
            U.log("Removing", this.lastAccount);
            this.manager.getAuthDatabase().unregisterAccount(this.lastAccount);
            this.listener.saveProfiles();
         } else {
            this.tempAccount = null;
            this.clearEditor();
         }

         if (num > -1) {
            this.list.list.setSelectedIndex(num);
         }

      }
   }

   void registerTemp() {
      if (this.tempAccount != null) {
         this.manager.getAuthDatabase().registerAccount(this.tempAccount);
         this.scene.getMainPane().defaultScene.loginForm.accounts.refreshAccounts(this.manager.getAuthDatabase(), this.tempAccount.getUsername());
         int num = this.list.model.indexOf(this.tempAccount);
         this.list.list.setSelectedIndex(num);
         this.tempAccount = null;
      }
   }

   public void notifyEmpty() {
      if (this.list.list.getSelectedIndex() == -1) {
         Blocker.block((Blockable)this.editor, (Object)"empty");
      }

   }

   private void block() {
      Blocker.block((Object)"auth", (Blockable[])(this.editor, this.list));
   }

   private void unblock() {
      Blocker.unblock((Object)"auth", (Blockable[])(this.editor, this.list));
   }
}
