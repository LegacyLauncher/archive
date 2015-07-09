package ru.turikhay.tlauncher.ui.swing.editor;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;

public abstract class ServerHyperlinkProcessor extends HyperlinkProcessor implements Blockable {
   private final JPopupMenu popup = new JPopupMenu();
   private boolean blocked;

   public void process(String link) {
      if (link != null && link.startsWith("server:")) {
         if (!this.blocked) {
            try {
               this.openServer(link);
            } catch (Exception var3) {
               Alert.showLocError("ad.server.error", new RuntimeException("\"" + link + "\"", var3));
            }
         }

      } else {
         HyperlinkProcessor.defaultProcessor.process(link);
      }
   }

   private void openServer(String link) {
      final ServerList.Server server = ServerList.Server.parseFromString(link.substring("server:".length()));
      this.popup.removeAll();
      LocalizableMenuItem openWith = new LocalizableMenuItem("ad.server.openwith", new Object[]{server.getName()});
      openWith.setEnabled(false);
      this.popup.add(openWith);
      List list = new ArrayList();
      Iterator var6 = TLauncher.getInstance().getVersionManager().getVersions().iterator();

      while(var6.hasNext()) {
         VersionSyncInfo syncInfo = (VersionSyncInfo)var6.next();
         int index = server.getVersions().indexOf(syncInfo.getID());
         if (index != -1) {
            list.add(syncInfo);
         }
      }

      for(int i = list.size() - 1; i > -1; --i) {
         final VersionSyncInfo syncInfo = (VersionSyncInfo)list.get(i);
         JMenuItem item = new JMenuItem(syncInfo.getID());
         item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               ServerHyperlinkProcessor.this.open(syncInfo, server);
            }
         });
         this.popup.add(item);
      }

      LocalizableMenuItem currentVersion = new LocalizableMenuItem("ad.server.openwith.current", new Object[]{server.getName()});
      currentVersion.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ServerHyperlinkProcessor.this.open((VersionSyncInfo)null, server);
         }
      });
      this.popup.add(currentVersion);
      this.popup.addSeparator();
      LocalizableMenuItem copyAddress = new LocalizableMenuItem("ad.server.copy");
      copyAddress.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            try {
               StringSelection stringSelection = new StringSelection(server.getAddress());
               Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, (ClipboardOwner)null);
            } catch (RuntimeException var3) {
               Alert.showMessage("IP:", (String)null, server.getAddress());
            }

         }
      });
      this.popup.add(copyAddress);
      if (!server.getAllowedAccountTypeList().isEmpty()) {
         LocalizableMenuItem requiredAccount;
         label46: {
            StringBuilder types;
            Iterator var10;
            switch(server.getAllowedAccountTypeList().size()) {
            case 1:
               requiredAccount = new LocalizableMenuItem("ad.server.required-account", new Object[]{Localizable.get("account.type." + server.getAllowedAccountTypeList().get(0))});
               break label46;
            default:
               types = new StringBuilder();
               var10 = server.getAllowedAccountTypeList().iterator();
            }

            while(var10.hasNext()) {
               Account.AccountType type = (Account.AccountType)var10.next();
               types.append(Localizable.get("account.type." + type)).append(", ");
            }

            requiredAccount = new LocalizableMenuItem("ad.server.required-account.multiple", new Object[]{types.substring(0, types.length() - ", ".length())});
         }

         requiredAccount.setEnabled(false);
         this.popup.add(requiredAccount);
      }

      this.showPopup(this.popup);
   }

   public abstract void showPopup(JPopupMenu var1);

   public abstract void open(VersionSyncInfo var1, ServerList.Server var2);

   public void block(Object reason) {
      this.blocked = true;
   }

   public void unblock(Object reason) {
      this.blocked = false;
   }
}
