package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.tlauncher.ui.swing.VersionCellRenderer;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.swing.ListCellRenderer;
import net.minecraft.launcher.events.RefreshedListener;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;

public class VersionComboBox extends ExtendedComboBox implements Blockable, RefreshedListener, LocalizableComponent, LoginListener {
   private static final long serialVersionUID = -9122074452728842733L;
   protected static final VersionSyncInfo LOADING;
   protected static final VersionSyncInfo EMPTY;
   private final VersionManager manager;
   private final LoginForm loginForm;
   private String selectedVersion;

   static {
      LOADING = VersionCellRenderer.LOADING;
      EMPTY = VersionCellRenderer.EMPTY;
   }

   VersionComboBox(LoginForm lf) {
      super((ListCellRenderer)(new VersionCellRenderer()));
      this.loginForm = lf;
      this.manager = TLauncher.getInstance().getVersionManager();
      this.manager.addRefreshedListener(this);
      this.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            VersionComboBox.this.loginForm.buttons.play.updateState();
            VersionSyncInfo selected = VersionComboBox.this.getVersion();
            if (selected != null) {
               VersionComboBox.this.selectedVersion = selected.getId();
            }

         }
      });
      this.selectedVersion = lf.global.get("login.version");
   }

   public VersionSyncInfo getVersion() {
      VersionSyncInfo selected = (VersionSyncInfo)this.getSelectedItem();
      return selected != null && !selected.equals(LOADING) && !selected.equals(EMPTY) ? selected : null;
   }

   public void onLogin() throws LoginException {
      VersionSyncInfo selected = this.getVersion();
      if (selected == null) {
         throw new LoginWaitException("Version list is empty, refreshing", new LoginWaitException.LoginWaitTask() {
            public void runTask() throws LoginException {
               VersionComboBox.this.manager.refreshVersions();
               if (VersionComboBox.this.getVersion() == null) {
                  Alert.showError("versions.notfound");
               }

               throw new LoginException("Giving user a second chance to choose correct version...");
            }
         });
      } else if (selected.isOnRemote() && selected.isInstalled() && !selected.isUpToDate()) {
         if (!Alert.showQuestion("versions.found-update", false)) {
            try {
               CompleteVersion complete = this.manager.getLocalVersionList().getCompleteVersion(selected.getLocalVersion());
               complete.setUpdatedTime(selected.getLatestVersion().getUpdatedTime());
               this.manager.getLocalVersionList().saveVersion(complete);
            } catch (IOException var3) {
               Alert.showError("versions.found-update.error");
            }

         } else {
            this.loginForm.checkbox.forceupdate.setSelected(false);
         }
      }
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }

   public void updateLocale() {
      this.updateList(this.manager.getVersions(), (String)null);
   }

   public void onVersionManagerUpdated(VersionManager vm) {
   }

   public void onVersionsRefreshing(VersionManager vm) {
      this.updateList((List)null, (String)null);
   }

   public void onVersionsRefreshingFailed(VersionManager vm) {
      this.updateList(this.manager.getVersions(), (String)null);
   }

   public void onVersionsRefreshed(VersionManager vm) {
      this.updateList(this.manager.getVersions(), (String)null);
   }

   public void updateList(List list, String select) {
      if (select == null && this.selectedVersion != null) {
         select = this.selectedVersion;
      }

      this.removeAllItems();
      if (list == null) {
         this.addItem(LOADING);
      } else {
         if (list.isEmpty()) {
            this.addItem(EMPTY);
         } else {
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
               VersionSyncInfo version = (VersionSyncInfo)var4.next();
               this.addItem(version);
               if (select != null && select.equals(version.getId())) {
                  this.setSelectedItem(version);
               }
            }
         }

      }
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }
}
