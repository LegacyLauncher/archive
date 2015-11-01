package ru.turikhay.tlauncher.component;

import java.util.concurrent.Semaphore;
import ru.turikhay.tlauncher.managers.ComponentManager;

public abstract class InterruptibleComponent extends RefreshableComponent {
   protected final boolean[] refreshList;
   private int lastRefreshID;
   protected final Semaphore semaphore;
   protected boolean lastResult;

   protected InterruptibleComponent(ComponentManager manager) throws Exception {
      this(manager, 64);
   }

   private InterruptibleComponent(ComponentManager manager, int listSize) throws Exception {
      super(manager);
      this.semaphore = new Semaphore(1);
      if (listSize < 1) {
         throw new IllegalArgumentException("Invalid list size: " + listSize + " < 1");
      } else {
         this.refreshList = new boolean[listSize];
      }
   }

   public final boolean refresh() {
      boolean var3;
      if (this.semaphore.tryAcquire()) {
         try {
            var3 = this.lastResult = this.refresh(this.nextID());
         } finally {
            this.semaphore.release();
         }

         return var3;
      } else {
         try {
            this.semaphore.acquire();
            var3 = this.lastResult;
            boolean var2 = var3;
            return var2;
         } catch (InterruptedException var11) {
            var11.printStackTrace();
         } finally {
            this.semaphore.release();
         }

         return false;
      }
   }

   public final boolean isRefreshing() {
      return this.semaphore.hasQueuedThreads();
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
