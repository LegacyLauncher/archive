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
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.listener.AuthUIListener;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.AccountCellRenderer;
import ru.turikhay.tlauncher.ui.swing.SimpleComboBoxModel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;

public class AccountComboBox extends ExtendedComboBox implements Blockable, LoginListener, ProfileManagerListener, LocalizableComponent {
   private static final long serialVersionUID = 6618039863712810645L;
   private static final Account EMPTY;
   private static final Account MANAGE;
   private final ProfileManager manager;
   private final LoginForm loginForm;
   private final AuthenticatorListener listener;
   private final SimpleComboBoxModel model;
   private String selectedAccount;

   static {
      EMPTY = AccountCellRenderer.EMPTY;
      MANAGE = AccountCellRenderer.MANAGE;
   }

   AccountComboBox(LoginForm lf) {
      super((ListCellRenderer)(new AccountCellRenderer()));
      this.loginForm = lf;
      this.model = this.getSimpleModel();
      this.manager = TLauncher.getInstance().getProfileManager();
      this.manager.addListener(this);
      this.listener = new AuthUIListener(true, lf);
      this.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            Account selected = (Account)AccountComboBox.this.getSelectedItem();
            if (selected != null && !selected.equals(AccountComboBox.EMPTY)) {
               if (selected.equals(AccountComboBox.MANAGE)) {
                  AccountComboBox.this.loginForm.pane.openAccountEditor();
                  AccountComboBox.this.setSelectedIndex(0);
               } else {
                  AccountComboBox.this.selectedAccount = selected.getUsername();
               }
            }
         }
      });
      this.selectedAccount = lf.global.get("login.account");
   }

   public Account getAccount() {
      Account value = (Account)this.getSelectedItem();
      return value != null && !value.equals(EMPTY) ? value : null;
   }

   public void setAccount(Account account) {
      if (account != null) {
         if (!account.equals(this.getAccount())) {
            this.setSelectedItem(account);
         }
      }
   }

   void setAccount(String username) {
      if (username != null) {
         this.setSelectedItem(this.manager.getAuthDatabase().getByUsername(username));
      }

   }

   public void onLogin() throws LoginException {
      final Account account = this.getAccount();
      if (account == null) {
         this.loginForm.pane.openAccountEditor();
         Alert.showLocError("account.empty.error");
         throw new LoginException("Account list is empty!");
      } else if (account.isPremium()) {
         throw new LoginWaitException("Waiting for auth...", new LoginWaitException.LoginWaitTask() {
            public void runTask() {
               account.getAuthenticator().pass(AccountComboBox.this.listener);
            }
         });
      }
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }

   public void refreshAccounts(AuthenticatorDatabase db, String select) {
      if (select == null && this.selectedAccount != null) {
         select = this.selectedAccount;
      }

      this.removeAllItems();
      Collection list = db.getAccounts();
      if (list.isEmpty()) {
         this.addItem(EMPTY);
      } else {
         this.model.addElements(list);
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            Account account = (Account)var5.next();
            if (select != null && select.equals(account.getUsername())) {
               this.setSelectedItem(account);
            }
         }
      }

      this.addItem(MANAGE);
   }

   public void updateLocale() {
      this.refreshAccounts(this.manager.getAuthDatabase(), (String)null);
   }

   public void onAccountsRefreshed(AuthenticatorDatabase db) {
      this.refreshAccounts(db, (String)null);
   }

   public void onProfilesRefreshed(ProfileManager pm) {
      this.refreshAccounts(pm.getAuthDatabase(), (String)null);
   }

   public void onProfileManagerChanged(ProfileManager pm) {
      this.refreshAccounts(pm.getAuthDatabase(), (String)null);
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }
}
