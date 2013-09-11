package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.MinecraftUtil;
import java.awt.Choice;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.launcher_.events.RefreshedListener;
import net.minecraft.launcher_.updater.VersionFilter;
import net.minecraft.launcher_.updater.VersionManager;
import net.minecraft.launcher_.updater.VersionSyncInfo;
import net.minecraft.launcher_.versions.ReleaseType;
import net.minecraft.launcher_.versions.Version;

public class VersionChoicePanel extends BlockablePanel implements RefreshedListener {
   private static final long serialVersionUID = -1838948772565245249L;
   private final LoginForm lf;
   private final Settings l;
   private final VersionManager vm;
   String version;
   VersionSyncInfo selected;
   Map list;
   Choice choice;
   boolean foundlocal;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$launcher_$versions$ReleaseType;

   VersionChoicePanel(LoginForm lf, String ver) {
      this.lf = lf;
      this.l = lf.l;
      this.vm = lf.t.vm;
      this.version = ver;
      LayoutManager lm = new GridLayout(1, 1);
      this.setLayout(lm);
      this.choice = new Choice();
      this.choice.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            VersionChoicePanel.this.version = (String)VersionChoicePanel.this.list.get(e.getItem().toString());
            VersionChoicePanel.this.onVersionChanged();
         }
      });
      this.list = new LinkedHashMap();
      this.add(this.choice);
   }

   void onVersionChanged() {
      this.foundlocal = true;
      this.selected = this.vm.getVersionSyncInfo(this.version);
      this.lf.buttons.updateEnterButton();
      this.unblock("refresh");
   }

   void refreshVersions(VersionManager vm, boolean local) {
      this.lf.unblock("version_refresh");
      this.choice.removeAll();
      this.list.clear();
      VersionFilter vf = MinecraftUtil.getVersionFilter();
      List listver = local ? vm.getInstalledVersions(vf) : vm.getVersions(vf);
      boolean exists = false;
      Iterator var7 = listver.iterator();

      while(var7.hasNext()) {
         VersionSyncInfo curv = (VersionSyncInfo)var7.next();
         Version ver = curv.getLatestVersion();
         String id = ver.getId();
         String dId = null;
         switch($SWITCH_TABLE$net$minecraft$launcher_$versions$ReleaseType()[ver.getType().ordinal()]) {
         case 1:
            dId = this.l.get("version.snapshot", "v", id);
            break;
         case 2:
            dId = this.l.get("version.release", "v", id);
            break;
         case 3:
            dId = this.l.get("version.beta", "v", id.substring(1));
            break;
         case 4:
            dId = this.l.get("version.alpha", "v", id.startsWith("a") ? id.substring(1) : id);
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
         this.choice.add(this.l.get("versions.notfound.tip"));
      } else {
         if (!exists || this.version == null) {
            this.version = (String)this.list.get(this.choice.getItem(0));
         }

         this.onVersionChanged();
      }
   }

   protected void blockElement(Object reason) {
      this.choice.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
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
      this.lf.block("version_refresh");
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

   public void onResourcesRefreshing(VersionManager vm) {
      this.lf.block("resource_refresh");
   }

   public void onResourcesRefreshed(VersionManager vm) {
      this.lf.unblock("resource_refresh");
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$launcher_$versions$ReleaseType() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$launcher_$versions$ReleaseType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[ReleaseType.values().length];

         try {
            var0[ReleaseType.OLD_ALPHA.ordinal()] = 4;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[ReleaseType.OLD_BETA.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[ReleaseType.RELEASE.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[ReleaseType.SNAPSHOT.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$net$minecraft$launcher_$versions$ReleaseType = var0;
         return var0;
      }
   }
}
