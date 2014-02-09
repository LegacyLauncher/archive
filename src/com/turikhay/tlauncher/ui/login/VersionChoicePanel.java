package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.BlockablePanel;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;
import java.awt.Choice;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.launcher.events.RefreshedListener;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class VersionChoicePanel extends BlockablePanel implements RefreshedListener, LocalizableComponent, LoginListener {
   private static final long serialVersionUID = -1838948772565245249L;
   private final LoginForm lf;
   private final LangConfiguration l;
   private VersionManager vm;
   String version;
   VersionSyncInfo selected;
   Map list;
   List lastupdate = new ArrayList();
   Choice choice;
   boolean foundlocal;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$launcher$versions$ReleaseType;

   VersionChoicePanel(LoginForm lf, String ver) {
      this.lf = lf;
      this.l = lf.lang;
      this.vm = lf.tlauncher.getVersionManager();
      this.version = ver;
      LayoutManager lm = new GridLayout(1, 1);
      this.setLayout(lm);
      this.setOpaque(false);
      this.choice = new Choice();
      this.choice.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            VersionChoicePanel.this.version = (String)VersionChoicePanel.this.list.get(e.getItem().toString());
            VersionChoicePanel.this.onVersionChanged();
         }
      });
      this.list = new LinkedHashMap();
      this.add(this.choice);
      TLauncher.getInstance().getVersionManager().addRefreshedListener(this);
   }

   void onVersionChanged() {
      this.foundlocal = true;
      this.selected = this.vm.getVersionSyncInfo(this.version);
      this.lf.buttons.updateEnterButton();
      this.unblock("refresh");
   }

   void refreshVersions(VersionManager vm, boolean local) {
      Blocker.unblock((Blockable)this.lf, (Object)"version_refresh");
      this.list.clear();
      VersionFilter vf = MinecraftUtil.getVersionFilter();
      this.lastupdate = local ? vm.getInstalledVersions(vf) : vm.getVersions(vf);
      this.updateLocale();
   }

   public void block(Object reason) {
      this.choice.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.choice.setEnabled(true);
   }

   public void onVersionsRefreshed(VersionManager vm) {
      this.refreshVersions(vm, false);
   }

   public void onVersionsRefreshingFailed(VersionManager vm) {
      this.refreshVersions(vm, true);
   }

   public void onVersionsRefreshing(VersionManager vm) {
      this.list.clear();
      this.choice.setEnabled(false);
      this.choice.removeAll();
      this.choice.add(this.l.get("versions.loading"));
      Blocker.block((Blockable)this.lf, (Object)"version_refresh");
   }

   public void refresh() {
      this.vm.refreshVersions();
   }

   public void asyncRefresh() {
      this.vm.asyncRefresh();
   }

   public VersionSyncInfo getSyncVersionInfo() {
      return this.selected;
   }

   public void handleUpdate(boolean ok) {
      VersionSyncInfo syncInfo;
      if (!ok) {
         syncInfo = this.getSyncVersionInfo();
         syncInfo.getLocalVersion().setUpdatedTime(new Date());
      } else {
         syncInfo = this.vm.getVersionSyncInfo(this.version);
      }

      try {
         this.vm.getLocalVersionList().saveVersion(this.vm.getLatestCompleteVersion(syncInfo));
      } catch (IOException var4) {
         var4.printStackTrace();
      }

   }

   public void updateLocale() {
      this.choice.removeAll();
      this.list.clear();
      this.choice.add("...");
      this.choice.select(0);
      this.choice.remove(0);
      boolean exists = false;
      String add = "";
      Iterator var4 = this.lastupdate.iterator();

      while(var4.hasNext()) {
         VersionSyncInfo curv = (VersionSyncInfo)var4.next();
         Version ver = curv.getLatestVersion();
         String id = ver.getId();
         String dId = id;
         if (id.length() < 19) {
            switch($SWITCH_TABLE$net$minecraft$launcher$versions$ReleaseType()[ver.getType().ordinal()]) {
            case 1:
               dId = this.l.get("version.snapshot", id);
               break;
            case 2:
               dId = this.l.get("version.release", id);
            case 3:
            case 4:
            default:
               break;
            case 5:
               dId = this.l.get("version.old_beta", id.substring(1));
               break;
            case 6:
               dId = this.l.get("version.old_alpha", id.startsWith("a") ? id.substring(1) : id);
            }
         } else {
            dId = U.t(id, 20);
            if (dId.length() != id.length()) {
               if (add.length() > 2) {
                  add = "";
               }

               add = add + "~";
               dId = dId + add;
            }
         }

         this.choice.add(dId);
         this.list.put(dId, id);
         if (id.equals(this.version)) {
            this.version = id;
            this.choice.select(dId);
            this.onVersionChanged();
            exists = true;
         }
      }

      if (this.choice.getItemCount() <= 0) {
         this.foundlocal = false;
         this.choice.add(this.l.get("versions.notfound.tip"));
      } else if (!exists || this.version == null) {
         int select = 0;

         for(int i = 0; i < this.choice.getItemCount(); ++i) {
            String ch = (String)this.list.get(this.choice.getItem(i));
            VersionSyncInfo vs = this.vm.getVersionSyncInfo(ch);
            if (vs.getLatestVersion().getType().isDesired()) {
               select = i;
               break;
            }
         }

         this.version = (String)this.list.get(this.choice.getItem(select));
         this.choice.select(select);
         this.onVersionChanged();
      }
   }

   public void onVersionManagerUpdated(VersionManager vm) {
      vm.asyncRefresh();
   }

   public boolean onLogin() {
      if (this.selected != null && this.foundlocal) {
         if (this.selected.isOnRemote() && this.selected.isInstalled() && !this.selected.isUpToDate()) {
            if (!Alert.showQuestion("versions.found-update", false)) {
               try {
                  CompleteVersion complete = this.vm.getLocalVersionList().getCompleteVersion(this.version);
                  complete.setUpdatedTime(this.selected.getLatestVersion().getUpdatedTime());
                  this.vm.getLocalVersionList().saveVersion(complete);
               } catch (IOException var2) {
                  Alert.showError("versions.found-update.error");
               }

               return true;
            } else {
               this.lf.checkbox.setForceUpdate(true);
               return true;
            }
         } else {
            return true;
         }
      } else {
         this.refresh();
         if (!this.foundlocal) {
            Alert.showError("versions.notfound");
         }

         return false;
      }
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$launcher$versions$ReleaseType() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$launcher$versions$ReleaseType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[ReleaseType.values().length];

         try {
            var0[ReleaseType.MODIFIED.ordinal()] = 3;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[ReleaseType.OLD.ordinal()] = 4;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[ReleaseType.OLD_ALPHA.ordinal()] = 6;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[ReleaseType.OLD_BETA.ordinal()] = 5;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[ReleaseType.RELEASE.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[ReleaseType.SNAPSHOT.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[ReleaseType.UNKNOWN.ordinal()] = 7;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$net$minecraft$launcher$versions$ReleaseType = var0;
         return var0;
      }
   }
}
