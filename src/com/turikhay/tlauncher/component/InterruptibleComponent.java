package com.turikhay.tlauncher.component;

import com.turikhay.tlauncher.managers.ComponentManager;

public abstract class InterruptibleComponent extends RefreshableComponent {
   protected final boolean[] refreshList;
   private int lastRefreshID;

   protected InterruptibleComponent(ComponentManager manager) throws Exception {
      this(manager, 64);
   }

   private InterruptibleComponent(ComponentManager manager, int listSize) throws Exception {
      super(manager);
      if (listSize < 1) {
         throw new IllegalArgumentException("Invalid list size: " + listSize + " < 1");
      } else {
         this.refreshList = new boolean[listSize];
      }
   }

   public boolean startRefresh() {
      return this.refresh(this.nextID());
   }

   protected boolean refresh() {
      return this.startRefresh();
   }

   public synchronized void stopRefresh() {
      for(int i = 0; i < this.refreshList.length; ++i) {
         this.refreshList[i] = false;
      }

   }

   protected synchronized int nextID() {
      int listSize = this.refreshList.length;
      int next = this.lastRefreshID++;
      if (next >= listSize) {
         next = 0;
      }

      this.lastRefreshID = next;
      return next;
   }

   protected boolean isCancelled(int refreshID) {
      return !this.refreshList[refreshID];
   }

   protected abstract boolean refresh(int var1);
}
