package ru.turikhay.tlauncher.ui.login;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.ListCellRenderer;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.managers.ProfileManagerListener;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.listener.AuthUIListener;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.AccountCellRenderer;
import ru.turikhay.tlauncher.ui.swing.SimpleComboBoxModel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;
import ru.turikhay.util.Reflect;

public class AccountComboBox extends ExtendedComboBox implements ProfileManagerListener, Blockable, LocalizableComponent, LoginForm.LoginProcessListener {
   private static final Account EMPTY;
   private static final Account MANAGE;
   private final ProfileManager manager;
   private final LoginForm loginForm;
   private final AuthenticatorListener listener;
   private final SimpleComboBoxModel model;
   private Account selectedAccount;
   boolean refreshing;

   AccountComboBox(LoginForm lf) {
      super((ListCellRenderer)(new AccountCellRenderer()));
      this.loginForm = lf;
      this.model = this.getSimpleModel();
      this.manager = TLauncher.getInstance().getProfileManager();
      this.manager.addListener(this);
      this.listener = new AuthUIListener(lf);
      this.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            Account selected = (Account)AccountComboBox.this.getSelectedItem();
            if (selected != null && !selected.equals(AccountComboBox.EMPTY)) {
               if (selected.equals(AccountComboBox.MANAGE)) {
                  AccountComboBox.this.loginForm.pane.openAccountEditor();
                  AccountComboBox.this.setSelectedIndex(0);
               } else {
                  AccountComboBox.this.selectedAccount = selected;
                  AccountComboBox.this.updateAccount();
               }
            }

         }
      });
   }

   public void updateAccount() {
      if (!this.refreshing) {
         if (this.selectedAccount.getType() == Account.AccountType.ELY) {
            if (this.loginForm.tlauncher.getElyManager().isRefreshing()) {
               Blocker.block((Blockable)this.loginForm.buttons.play, (Object)"ely");
            } else {
               this.loginForm.tlauncher.getElyManager().refreshOnce();
            }
         } else {
            Blocker.unblock((Blockable)this.loginForm.buttons.play, (Object)"ely");
         }

         VersionComboBox.showElyVersions = this.selectedAccount.getType() == Account.AccountType.ELY;
         this.loginForm.global.setForcefully("login.account", this.selectedAccount.getUsername(), false);
         this.loginForm.global.setForcefully("login.account.type", this.selectedAccount.getType(), false);
         this.loginForm.global.store();
      }

   }

   public Account getAccount() {
      Account value = (Account)this.getSelectedItem();
      return value != null && !value.equals(EMPTY) ? value : null;
   }

   public void setAccount(Account account) {
      if (account != null && !account.equals(this.getAccount())) {
         this.setSelectedItem(account);
      }

   }

   public void logginingIn() throws LoginException {
      final Account account = this.getAccount();
      if (account == null) {
         this.loginForm.pane.openAccountEditor();
         Alert.showLocError("account.empty.error");
         throw new LoginException("Account list is empty!");
      } else if (!account.isFree()) {
         throw new LoginWaitException("Waiting for auth...", new LoginWaitException.LoginWaitTask() {
            public void runTask() {
               Authenticator.instanceFor(account).pass(AccountComboBox.this.listener);
            }
         });
      }
   }

   public void loginFailed() {
   }

   public void loginSucceed() {
   }

   public void refreshAccounts(AuthenticatorDatabase db, Account select) {
      if (select == null) {
         if (this.selectedAccount == null) {
            String list = this.loginForm.global.get("login.account");
            if (list != null) {
               Account.AccountType account = (Account.AccountType)Reflect.parseEnum(Account.AccountType.class, this.loginForm.global.get("login.account.type"));
               this.selectedAccount = this.loginForm.tlauncher.getProfileManager().getAuthDatabase().getByUsername(list, account);
            }
         }

         select = this.selectedAccount;
      }

      this.removeAllItems();
      Collection list1 = db.getAccounts();
      if (list1.isEmpty()) {
         this.addItem(EMPTY);
      } else {
         this.refreshing = true;
         this.model.addElements(list1);
         Iterator var5 = list1.iterator();

         while(var5.hasNext()) {
            Account account1 = (Account)var5.next();
            if (select != null && select.equals(account1)) {
               this.setSelectedItem(account1);
               break;
            }
         }

         this.refreshing = false;
         this.updateAccount();
      }

      this.addItem(MANAGE);
   }

   public void updateLocale() {
      this.refreshAccounts(this.manager.getAuthDatabase(), (Account)null);
   }

   public void onAccountsRefreshed(AuthenticatorDatabase db) {
      this.refreshAccounts(db, (Account)null);
   }

   public void onProfilesRefreshed(ProfileManager pm) {
      this.refreshAccounts(pm.getAuthDatabase(), (Account)null);
   }

   public void onProfileManagerChanged(ProfileManager pm) {
      this.refreshAccounts(pm.getAuthDatabase(), (Account)null);
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }

   static {
      EMPTY = AccountCellRenderer.EMPTY;
      MANAGE = AccountCellRenderer.MANAGE;
   }
}
