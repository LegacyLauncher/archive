package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.minecraft.profiles.ProfileListener;
import com.turikhay.tlauncher.minecraft.profiles.ProfileManager;
import com.turikhay.tlauncher.ui.ImageButton;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.BlockablePanel;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.listeners.AuthUIListener;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Iterator;

public class AccountChoicePanel extends BlockablePanel implements ProfileListener, LocalizableComponent, LoginListener {
   private static final long serialVersionUID = -8152546672896025263L;
   private static final String EMPTY = "empty";
   private final LoginForm lf;
   private final Choice choice;
   private final ImageButton editAccounts;
   private ProfileManager pm;
   Account account;
   boolean empty;
   boolean error_shown;
   private final AuthenticatorListener listener;

   AccountChoicePanel(LoginForm loginForm, String acc) {
      GridBagLayout layout = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      this.setLayout(layout);
      this.setOpaque(false);
      this.lf = loginForm;
      this.choice = new Choice();
      this.choice.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            String selected = e.getItem().toString();
            AccountChoicePanel.this.fireUpdate(selected);
         }
      });
      c.fill = 1;
      c.weightx = 2.0D;
      this.add(this.choice, c);
      this.editAccounts = new ImageButton("gear.png");
      this.editAccounts.setPreferredSize(new Dimension(32, 25));
      this.editAccounts.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            AccountChoicePanel.this.lf.pane.openAccountEditor();
         }
      });
      c.gridx = 1;
      c.weightx = 0.3D;
      this.add(this.editAccounts, c);
      this.pm = TLauncher.getInstance().getProfileManager();
      this.pm.addListener(this);
      this.listener = new AuthUIListener(new AuthenticatorListener() {
         public void onAuthPassing(Authenticator auth) {
            Blocker.block((Blockable)AccountChoicePanel.this.lf, (Object)"AUTH");
         }

         public void onAuthPassingError(Authenticator auth, Throwable e) {
            if (e.getCause() instanceof IOException) {
               AccountChoicePanel.this.lf.runLogin();
            } else {
               Blocker.unblock((Blockable)AccountChoicePanel.this.lf, (Object)"AUTH");
            }

         }

         public void onAuthPassed(Authenticator auth) {
            AccountChoicePanel.this.lf.runLogin();
         }
      }) {
         public void onAuthPassingError(Authenticator auth, Throwable e) {
            if (!AccountChoicePanel.this.error_shown) {
               super.onAuthPassingError(auth, e);
               AccountChoicePanel.this.error_shown = true;
            } else {
               this.onAuthPassingError(auth, e, false);
            }

         }
      };
   }

   public void fireUpdate(String selected) {
      AuthenticatorDatabase db = this.pm.getAuthDatabase();
      this.account = db.getByUsername(selected);
      this.onAccountsRefreshed(db);
      this.lf.pane.accountEditor.handler.selectAccount(this.account);
   }

   public Account getAccount() {
      return this.account;
   }

   public void selectAccount(Account acc) {
      if (acc != null) {
         String selected = acc.getUsername();
         this.choice.select(selected);
         this.fireUpdate(selected);
      }
   }

   public void onProfilesRefreshed(ProfileManager pm) {
      this.pm = pm;
      this.onAccountsRefreshed(pm.getAuthDatabase());
   }

   public void onAccountsRefreshed(AuthenticatorDatabase db) {
      this.choice.removeAll();
      Iterator var3 = db.getAccounts().iterator();

      while(var3.hasNext()) {
         Account account = (Account)var3.next();
         this.choice.add(account.getUsername());
      }

      if (this.account != null) {
         this.choice.select(this.account.getUsername());
      }

      this.empty = this.choice.getItemCount() == 0;
      if (this.empty) {
         this.choice.add(Localizable.get("account.empty"));
      }

      Blocker.setBlocked(this, "empty", this.empty);
   }

   public void onProfileManagerChanged(ProfileManager pm) {
      this.pm = pm;
      this.onAccountsRefreshed(pm.getAuthDatabase());
   }

   boolean waitOnLogin() {
      this.account = this.pm.getAuthDatabase().getByUsername(this.choice.getSelectedItem());
      if (!this.empty && this.account != null) {
         if (this.account.hasLicense()) {
            this.account.getAuthenticator().asyncPass(this.listener);
            return true;
         } else {
            return false;
         }
      } else {
         this.lf.pane.openAccountEditor();
         return true;
      }
   }

   public void block(Object reason) {
      this.choice.setEnabled(false);
      if (!reason.equals("empty")) {
         this.editAccounts.setEnabled(false);
      }

   }

   public void unblock(Object reason) {
      this.choice.setEnabled(true);
      if (!reason.equals("empty")) {
         this.editAccounts.setEnabled(true);
      }

   }

   public void updateLocale() {
      this.onAccountsRefreshed(this.pm.getAuthDatabase());
   }

   public boolean onLogin() {
      return true;
   }

   public void onLoginFailed() {
      Blocker.unblock((Blockable)this.lf, (Object)"AUTH");
   }

   public void onLoginSuccess() {
   }
}
