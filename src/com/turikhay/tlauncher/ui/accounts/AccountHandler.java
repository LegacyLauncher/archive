package com.turikhay.tlauncher.ui.accounts;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.managers.ProfileManager;
import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.ui.accounts.helper.HelperState;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.listener.AuthUIListener;
import com.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import com.turikhay.util.U;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPopupMenu;

public class AccountHandler {
   private final AccountEditorScene scene;
   public final AccountList list;
   public final AccountEditor editor;
   private final ProfileManager manager = TLauncher.getInstance().getProfileManager();
   private final AuthUIListener listener;
   private Account lastAccount;
   private Account tempAccount;
   private JPopupMenu popup;

   public AccountHandler(AccountEditorScene sc) {
      this.scene = sc;
      this.list = this.scene.list;
      this.editor = this.scene.editor;
      this.popup = new JPopupMenu();
      HelperState[] var5;
      int var4 = (var5 = HelperState.values()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         final HelperState state = var5[var3];
         if (state.showInList) {
            state.item.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  AccountHandler.this.scene.helper.setState(state);
               }
            });
            this.popup.add(state.item);
         }
      }

      this.listener = new AuthUIListener(false, new AuthenticatorListener() {
         public void onAuthPassing(Authenticator auth) {
            AccountHandler.this.block();
         }

         public void onAuthPassingError(Authenticator auth, Throwable e) {
            AccountHandler.this.unblock();
         }

         public void onAuthPassed(Authenticator auth) {
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

      }
   }

   void clearEditor() {
      this.lastAccount = null;
      this.editor.clear();
      if (!this.list.model.isEmpty()) {
         this.list.list.setSelectedValue(this.lastAccount, true);
      } else {
         this.notifyEmpty();
      }

   }

   void saveEditor() {
      if (this.lastAccount != null) {
         Account acc = this.editor.get();
         if (acc.getUsername() == null) {
            Alert.showLocError("auth.error.nousername");
         } else {
            this.lastAccount.complete(acc);
            if (this.lastAccount.hasLicense()) {
               if (this.lastAccount.getAccessToken() == null && this.lastAccount.getPassword() == null) {
                  Alert.showLocError("auth.error.nopass");
                  return;
               }

               this.lastAccount.getAuthenticator().asyncPass(this.listener);
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
         this.list.model.removeElement(this.lastAccount);
         this.lastAccount = acc;
         if (this.tempAccount != null) {
            this.tempAccount = null;
            this.clearEditor();
         } else {
            U.log("Removing", this.lastAccount);
            this.manager.getAuthDatabase().unregisterAccount(this.lastAccount);
            this.clearEditor();
            this.listener.saveProfiles();
         }
      }
   }

   void registerTemp() {
      if (this.tempAccount != null) {
         this.manager.getAuthDatabase().registerAccount(this.tempAccount);
         this.scene.getMainPane().defaultScene.loginForm.accounts.refreshAccounts(this.manager.getAuthDatabase(), this.tempAccount.getUsername());
         this.tempAccount = null;
      }
   }

   void notifyEmpty() {
      Blocker.block((Blockable)this.editor, (Object)"empty");
      if (this.scene.helper.isShowing()) {
         this.scene.helper.setState(HelperState.HELP);
      }

   }

   void callPopup() {
      if (!this.popup.isShowing()) {
         this.popup.show(this.list.help, 0, this.list.help.getHeight());
      }
   }

   private void block() {
      Blocker.block((Object)"auth", (Blockable[])(this.editor, this.list));
   }

   private void unblock() {
      Blocker.unblock((Object)"auth", (Blockable[])(this.editor, this.list));
   }
}
