package ru.turikhay.tlauncher.ui.accounts;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.managers.ProfileManagerListener;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import ru.turikhay.tlauncher.ui.block.Unblockable;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.swing.AccountCellRenderer;
import ru.turikhay.tlauncher.ui.swing.ImageButton;

public class AccountList extends CenterPanel {
   private static final long serialVersionUID = 3280495266368287215L;
   private final AccountEditorScene scene;
   public final DefaultListModel model;
   public final JList list;
   public final ImageButton add;
   public final ImageButton remove;
   public final ImageButton help;
   public final ImageButton back;

   public AccountList(AccountEditorScene sc) {
      super(squareInsets);
      this.scene = sc;
      JPanel panel = new JPanel(new BorderLayout(0, 5));
      panel.setOpaque(false);
      LocalizableLabel label = new LocalizableLabel("account.list");
      panel.add("North", label);
      this.model = new DefaultListModel();
      this.list = new JList(this.model);
      this.list.setCellRenderer(new AccountCellRenderer(AccountCellRenderer.AccountCellType.EDITOR));
      this.list.setSelectionMode(0);
      this.list.addListSelectionListener(new ListSelectionListener() {
         public void valueChanged(ListSelectionEvent e) {
            Account account = (Account)AccountList.this.list.getSelectedValue();
            AccountList.this.scene.handler.refreshEditor(account);
         }
      });
      JScrollPane scroll = new JScrollPane(this.list);
      scroll.setOpaque(false);
      scroll.getViewport().setOpaque(false);
      scroll.setBorder((Border)null);
      scroll.setHorizontalScrollBarPolicy(31);
      scroll.setVerticalScrollBarPolicy(20);
      panel.add("Center", scroll);
      JPanel buttons = new JPanel(new GridLayout(0, 4));
      buttons.setOpaque(false);
      this.add = new ImageButton("plus.png");
      this.add.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            AccountList.this.scene.handler.addAccount();
            AccountList.this.defocus();
         }
      });
      buttons.add(this.add);
      this.remove = new ImageButton("minus.png");
      this.remove.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            AccountList.this.scene.handler.removeAccount();
            AccountList.this.defocus();
         }
      });
      buttons.add(this.remove);
      this.help = new ImageButton("info.png");
      this.help.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            AccountList.this.defocus();
            AccountList.this.scene.handler.callPopup();
         }
      });
      buttons.add(this.help);
      this.back = new AccountList.UnblockableImageButton("home.png");
      this.back.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            AccountList.this.scene.handler.exitEditor();
         }
      });
      buttons.add(this.back);
      panel.add("South", buttons);
      this.add(panel);
      ProfileManagerListener listener = new ProfileManagerListener() {
         public void onProfilesRefreshed(ProfileManager pm) {
            AccountList.this.refreshFrom(pm.getAuthDatabase());
         }

         public void onProfileManagerChanged(ProfileManager pm) {
            AccountList.this.refreshFrom(pm.getAuthDatabase());
         }

         public void onAccountsRefreshed(AuthenticatorDatabase db) {
            AccountList.this.refreshFrom(db);
         }
      };
      TLauncher.getInstance().getProfileManager().addListener(listener);
   }

   void refreshFrom(AuthenticatorDatabase db) {
      this.model.clear();
      Iterator var3 = db.getAccounts().iterator();

      while(var3.hasNext()) {
         Account account = (Account)var3.next();
         this.model.addElement(account);
      }

      if (this.model.isEmpty()) {
         this.scene.handler.notifyEmpty();
      }

   }

   class UnblockableImageButton extends ImageButton implements Unblockable {
      public UnblockableImageButton(String imagepath) {
         super(imagepath);
      }
   }
}
