package com.turikhay.tlauncher.downloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DownloadableContainer {
   private DownloadableHandler handler;
   private boolean available = true;
   private int remain;
   private int errors;
   Downloadable error_elem;
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

   public void setHandler(DownloadableHandler newhandler) {
      this.check();
      this.handler = newhandler;
   }

   void onError() {
      ++this.errors;
      this.onFileComplete();
   }

   void onStart() {
      if (this.handler != null) {
         this.handler.onStart();
      }

   }

   void onFileComplete() {
      --this.remain;
      if (this.remain == 0 && this.handler != null) {
         if (this.errors > 0) {
            this.handler.onCompleteError();
         } else {
            this.handler.onComplete();
         }

      }
   }

   public Throwable getError() {
      return this.error_elem != null ? this.error_elem.getError() : null;
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
