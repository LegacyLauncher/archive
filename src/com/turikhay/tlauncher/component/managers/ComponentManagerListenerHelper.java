package com.turikhay.tlauncher.component.managers;

import com.turikhay.tlauncher.component.LauncherComponent;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.Blocker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ComponentManagerListenerHelper extends LauncherComponent implements Blockable, VersionManagerListener {
   private final List listeners = Collections.synchronizedList(new ArrayList());

   public ComponentManagerListenerHelper(ComponentManager manager) throws Exception {
      super(manager);
      ((VersionManager)manager.getComponent(VersionManager.class)).addListener(this);
   }

   public void addListener(ComponentManagerListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         this.listeners.add(listener);
      }
   }

   public void onVersionsRefreshing(VersionManager manager) {
      Blocker.block((Blockable)this, (Object)manager);
   }

   public void onVersionsRefreshingFailed(VersionManager manager) {
      Blocker.unblock((Blockable)this, (Object)manager);
   }

   public void onVersionsRefreshed(VersionManager manager) {
      Blocker.unblock((Blockable)this, (Object)manager);
   }

   public void block(Object reason) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         ComponentManagerListener listener = (ComponentManagerListener)var3.next();
         listener.onComponentsRefreshing(this.manager);
      }

   }

   public void unblock(Object reason) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         ComponentManagerListener listener = (ComponentManagerListener)var3.next();
         listener.onComponentsRefreshed(this.manager);
      }

   }
}
