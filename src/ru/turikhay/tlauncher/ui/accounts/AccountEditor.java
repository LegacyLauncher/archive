package ru.turikhay.tlauncher.ui.accounts;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import javax.swing.ButtonGroup;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableRadioButton;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.swing.CheckBoxListener;
import ru.turikhay.tlauncher.ui.text.ExtendedPasswordField;

public class AccountEditor extends CenterPanel {
   private static final String passlock = "passlock";
   private final AccountEditorScene scene;
   public final UsernameField username;
   public final AccountEditor.BlockablePasswordField password;
   public final ButtonGroup authGroup;
   public final AccountEditor.AuthTypeRadio freeAuth;
   public final AccountEditor.AuthTypeRadio mojangAuth;
   public final AccountEditor.AuthTypeRadio elyAuth;
   public final LinkedHashMap radioMap = new LinkedHashMap();
   public final LocalizableButton save;
   private final ProgressBar progressBar;
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$minecraft$auth$Account$AccountType;

   public AccountEditor(AccountEditorScene sc) {
      super(squareInsets);
      this.scene = sc;
      ActionListener enterHandler = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            AccountEditor.this.defocus();
            AccountEditor.this.scene.handler.saveEditor();
         }
      };
      this.username = new UsernameField(this, UsernameField.UsernameState.USERNAME);
      this.username.addActionListener(enterHandler);
      this.password = new AccountEditor.BlockablePasswordField();
      this.password.addActionListener(enterHandler);
      this.password.setEnabled(false);
      this.authGroup = new ButtonGroup();
      this.freeAuth = new AccountEditor.AuthTypeRadio(Account.AccountType.FREE);
      this.mojangAuth = new AccountEditor.AuthTypeRadio(Account.AccountType.MOJANG);
      this.elyAuth = new AccountEditor.AuthTypeRadio(Account.AccountType.ELY);
      this.save = new LocalizableButton("account.save");
      this.save.addActionListener(enterHandler);
      this.progressBar = new ProgressBar();
      this.progressBar.setPreferredSize(new Dimension(200, 20));
      this.add(this.del(0));
      this.add(sepPan(new Component[]{this.username}));
      this.add(sepPan(new Component[]{this.freeAuth, this.mojangAuth, this.elyAuth}));
      this.add(sepPan(new Component[]{this.password}));
      this.add(this.del(0));
      this.add(sepPan(new Component[]{this.save}));
      this.add(sepPan(new Component[]{this.progressBar}));
   }

   public Account.AccountType getSelectedAccountType() {
      Iterator var2 = this.radioMap.entrySet().iterator();

      Entry en;
      do {
         if (!var2.hasNext()) {
            return Account.AccountType.FREE;
         }

         en = (Entry)var2.next();
      } while(!((AccountEditor.AuthTypeRadio)en.getValue()).isSelected());

      return (Account.AccountType)en.getKey();
   }

   public void setSelectedAccountType(Account.AccountType type) {
      AccountEditor.AuthTypeRadio selectable = (AccountEditor.AuthTypeRadio)this.radioMap.get(type);
      if (selectable != null) {
         selectable.setSelected(true);
      }

   }

   public void fill(Account account) {
      this.setSelectedAccountType(account.getType());
      this.username.setText(account.getUsername());
      this.password.setText((String)null);
   }

   public void clear() {
      this.setSelectedAccountType((Account.AccountType)null);
      this.username.setText((String)null);
      this.password.setText((String)null);
   }

   public Account get() {
      Account account = new Account();
      account.setUsername(this.username.getValue());
      Account.AccountType type = this.getSelectedAccountType();
      switch(type) {
      case MOJANG:
      case ELY:
         if (this.password.hasPassword()) {
            account.setPassword(this.password.getPassword());
         }
      case FREE:
      default:
         account.setType(type);
         return account;
      }
   }

   public Insets getInsets() {
      return squareInsets;
   }

   public void block(Object reason) {
      super.block(reason);
      if (!reason.equals("empty")) {
         this.progressBar.setIndeterminate(true);
      }

   }

   public void unblock(Object reason) {
      super.unblock(reason);
      if (!reason.equals("empty")) {
         this.progressBar.setIndeterminate(false);
      }

   }

   class BlockablePasswordField extends ExtendedPasswordField implements Blockable {
      private BlockablePasswordField() {
      }

      public void block(Object reason) {
         this.setEnabled(false);
      }

      public void unblock(Object reason) {
         this.setEnabled(true);
      }

      // $FF: synthetic method
      BlockablePasswordField(Object x1) {
         this();
      }
   }

   public class AuthTypeRadio extends LocalizableRadioButton {
      private final Account.AccountType type;

      private AuthTypeRadio(final Account.AccountType type) {
         super("account.auth." + type);
         AccountEditor.this.radioMap.put(type, this);
         AccountEditor.this.authGroup.add(this);
         this.type = type;
         final boolean free = type == Account.AccountType.FREE;
         this.addItemListener(new CheckBoxListener() {
            public void itemStateChanged(boolean newstate) {
               if (newstate && !AccountEditor.this.password.hasPassword()) {
                  AccountEditor.this.password.setText((String)null);
               }

               if (newstate) {
                  AccountEditor.this.scene.tip.setAccountType(type);
               }

               newstate &= free;
               Blocker.setBlocked(AccountEditor.this.password, "passlock", newstate);
               AccountEditor.this.username.setState(newstate ? UsernameField.UsernameState.USERNAME : UsernameField.UsernameState.EMAIL);
               AccountEditor.this.defocus();
            }
         });
      }

      public Account.AccountType getAccountType() {
         return this.type;
      }

      // $FF: synthetic method
      AuthTypeRadio(Account.AccountType x1, Object x2) {
         this(x1);
      }
   }
}
