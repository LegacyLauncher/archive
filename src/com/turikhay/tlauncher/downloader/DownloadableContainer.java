package com.turikhay.tlauncher.downloader;

import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.ui.console.Console;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DownloadableContainer {
   private List handlers = Collections.synchronizedList(new ArrayList());
   private Console console;
   private boolean available = true;
   private int remain;
   private int errors;
   List elems = new ArrayList();

   public void addAll(Downloadable[] c) {
      Downloadable[] var5 = c;
      int var4 = c.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         Downloadable d = var5[var3];
         this.add(d);
      }

   }

   public void addAll(Collection c) {
      Iterator var3 = c.iterator();

      while(var3.hasNext()) {
         Downloadable d = (Downloadable)var3.next();
         this.add(d);
      }

   }

   public void add(Downloadable d) {
      this.check();
      this.elems.add(d);
      d.setContainer(this);
      ++this.remain;
   }

   public void remove(Downloadable d) {
      this.check();
      this.elems.remove(d);
      d.setContainer(this);
      ++this.remain;
   }

   public List get() {
      this.check();
      List t = new ArrayList();
      t.addAll(this.elems);
      return t;
   }

   public boolean isAvailable() {
      return this.available;
   }

   public void addHandler(DownloadableHandler newhandler) {
      this.check();
      this.handlers.add(newhandler);
   }

   public List getHandlers() {
      List toret = new ArrayList();
      Iterator var3 = this.handlers.iterator();

      while(var3.hasNext()) {
         DownloadableHandler h = (DownloadableHandler)var3.next();
         toret.add(h);
      }

      return toret;
   }

   public void setConsole(Console c) {
      this.console = c;
   }

   public Console getConsole() {
      return this.console;
   }

   public boolean hasConsole() {
      return this.console != null;
   }

   void log(Object... obj) {
      if (this.console != null) {
         this.console.log(obj);
      }

   }

   void onError() {
      ++this.errors;
      this.onFileComplete();
   }

   void onStart() {
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         DownloadableHandler h = (DownloadableHandler)var2.next();
         h.onStart();
      }

   }

   void onAbort() {
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         DownloadableHandler h = (DownloadableHandler)var2.next();
         h.onAbort();
      }

   }

   void onFileComplete() {
      --this.remain;
      if (this.remain == 0) {
         DownloadableHandler h;
         Iterator var2;
         if (this.errors > 0) {
            var2 = this.handlers.iterator();

            while(var2.hasNext()) {
               h = (DownloadableHandler)var2.next();
               h.onCompleteError();
            }
         } else {
            var2 = this.handlers.iterator();

            while(var2.hasNext()) {
               h = (DownloadableHandler)var2.next();
               h.onComplete();
            }
         }

      }
   }

   public int getErrors() {
      return this.errors;
   }

   private void check() {
      if (!this.available) {
         throw new IllegalStateException("DownloadableContainer is unavailable!");
      }
   }
}
