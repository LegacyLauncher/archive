package ru.turikhay.tlauncher.ui.versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionManagerListener;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.scenes.VersionManagerScene;

public class VersionHandler implements Blockable, VersionHandlerListener {
   private final List listeners;
   private final VersionHandler instance = this;
   public final VersionManagerScene scene;
   final VersionHandlerThread thread;
   public final VersionList list;
   final VersionManager vm;
   final Downloader downloader;
   List selected;
   List downloading;
   VersionFilter filter;

   public VersionHandler(VersionManagerScene scene) {
      this.scene = scene;
      this.listeners = Collections.synchronizedList(new ArrayList());
      this.downloading = Collections.synchronizedList(new ArrayList());
      TLauncher launcher = TLauncher.getInstance();
      this.vm = launcher.getVersionManager();
      this.downloader = launcher.getDownloader();
      this.list = new VersionList(this);
      this.thread = new VersionHandlerThread(this);
      this.vm.addListener(new VersionManagerListener() {
         public void onVersionsRefreshing(VersionManager manager) {
            VersionHandler.this.instance.onVersionRefreshing(manager);
         }

         public void onVersionsRefreshed(VersionManager manager) {
            VersionHandler.this.instance.onVersionRefreshed(manager);
         }

         public void onVersionsRefreshingFailed(VersionManager manager) {
            this.onVersionsRefreshed(manager);
         }
      });
      this.onVersionDeselected();
   }

   void addListener(VersionHandlerListener listener) {
      this.listeners.add(listener);
   }

   void refresh() {
      this.vm.startRefresh(true);
   }

   void asyncRefresh() {
      this.vm.asyncRefresh();
   }

   public void stopRefresh() {
      this.vm.stopRefresh();
   }

   void exitEditor() {
      this.list.deselect();
      this.scene.getMainPane().openDefaultScene();
   }

   List getSelectedList() {
      return this.selected;
   }

   public void block(Object reason) {
      Blocker.block(reason, this.list, this.scene.getMainPane().defaultScene);
   }

   public void unblock(Object reason) {
      Blocker.unblock(reason, this.list, this.scene.getMainPane().defaultScene);
   }

   public void onVersionRefreshing(VersionManager vm) {
      Blocker.block((Blockable)this.instance, (Object)"refresh");
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         VersionHandlerListener listener = (VersionHandlerListener)var3.next();
         listener.onVersionRefreshing(vm);
      }

   }

   public void onVersionRefreshed(VersionManager vm) {
      Blocker.unblock((Blockable)this.instance, (Object)"refresh");
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         VersionHandlerListener listener = (VersionHandlerListener)var3.next();
         listener.onVersionRefreshed(vm);
      }

   }

   public void onVersionSelected(List version) {
      this.selected = version;
      if (version != null && !version.isEmpty() && ((VersionSyncInfo)version.get(0)).getID() != null) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            VersionHandlerListener listener = (VersionHandlerListener)var3.next();
            listener.onVersionSelected(version);
         }
      } else {
         this.onVersionDeselected();
      }

   }

   public void onVersionDeselected() {
      this.selected = null;
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         VersionHandlerListener listener = (VersionHandlerListener)var2.next();
         listener.onVersionDeselected();
      }

   }

   public void onVersionDownload(List list) {
      this.downloading = list;
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         VersionHandlerListener listener = (VersionHandlerListener)var3.next();
         listener.onVersionDownload(list);
      }

   }
}
