package com.turikhay.tlauncher.downloader;

import com.turikhay.tlauncher.ui.console.Console;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadableContainer {
   private final List handlers = Collections.synchronizedList(new ArrayList());
   private final List errors = Collections.synchronizedList(new ArrayList());
   final List list = Collections.synchronizedList(new ArrayList());
   private Console console;
   private final AtomicInteger sum = new AtomicInteger();
   private boolean locked;
   private boolean aborted;

   public List getList() {
      return Collections.unmodifiableList(this.list);
   }

   public void add(Downloadable d) {
      if (d == null) {
         throw new NullPointerException();
      } else {
         this.checkLocked();
         this.list.add(d);
         d.setContainer(this);
         this.sum.incrementAndGet();
      }
   }

   public void addAll(Downloadable... ds) {
      if (ds == null) {
         throw new NullPointerException();
      } else {
         for(int i = 0; i < ds.length; ++i) {
            if (ds[i] == null) {
               throw new NullPointerException("Downloadable at " + i + " is NULL!");
            }

            this.list.add(ds[i]);
            ds[i].setContainer(this);
            this.sum.incrementAndGet();
         }

      }
   }

   public void addAll(Collection coll) {
      if (coll == null) {
         throw new NullPointerException();
      } else {
         int i = -1;
         Iterator var4 = coll.iterator();

         while(var4.hasNext()) {
            Downloadable d = (Downloadable)var4.next();
            ++i;
            if (d == null) {
               throw new NullPointerException("Downloadable at" + i + " is NULL!");
            }

            this.list.add(d);
            d.setContainer(this);
            this.sum.incrementAndGet();
         }

      }
   }

   public void addHandler(DownloadableContainerHandler handler) {
      if (handler == null) {
         throw new NullPointerException();
      } else {
         this.checkLocked();
         this.handlers.add(handler);
      }
   }

   public List getErrors() {
      return Collections.unmodifiableList(this.errors);
   }

   public Console getConsole() {
      return this.console;
   }

   public boolean hasConsole() {
      return this.console != null;
   }

   public void setConsole(Console console) {
      this.checkLocked();
      this.console = console;
   }

   public boolean isAborted() {
      return this.aborted;
   }

   void setLocked(boolean locked) {
      this.locked = locked;
   }

   void checkLocked() {
      if (this.locked) {
         throw new IllegalStateException("Downloadable is locked!");
      }
   }

   void onStart() {
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         DownloadableContainerHandler handler = (DownloadableContainerHandler)var2.next();
         handler.onStart(this);
      }

   }

   void onComplete(Downloadable d) throws RetryDownloadException {
      Iterator var3 = this.handlers.iterator();

      DownloadableContainerHandler handler;
      while(var3.hasNext()) {
         handler = (DownloadableContainerHandler)var3.next();
         handler.onComplete(this, d);
      }

      if (this.sum.decrementAndGet() <= 0) {
         var3 = this.handlers.iterator();

         while(var3.hasNext()) {
            handler = (DownloadableContainerHandler)var3.next();
            handler.onFullComplete(this);
         }

      }
   }

   void onAbort(Downloadable d) {
      this.aborted = true;
      this.errors.add(d.getError());
      if (this.sum.decrementAndGet() <= 0) {
         Iterator var3 = this.handlers.iterator();

         while(var3.hasNext()) {
            DownloadableContainerHandler handler = (DownloadableContainerHandler)var3.next();
            handler.onAbort(this);
         }

      }
   }

   void onError(Downloadable d, Throwable e) {
      this.errors.add(e);
      Iterator var4 = this.handlers.iterator();

      while(var4.hasNext()) {
         DownloadableContainerHandler handler = (DownloadableContainerHandler)var4.next();
         handler.onError(this, d, e);
      }

   }

   void log(Object... o) {
      if (this.console != null) {
         this.console.log(o);
      }
   }
}
