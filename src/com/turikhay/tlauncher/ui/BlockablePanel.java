package com.turikhay.tlauncher.ui;

import java.awt.Panel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class BlockablePanel extends Panel implements Blockable {
   private static final long serialVersionUID = 1L;
   public static final Object UNIVERSAL_UNBLOCK = "lol, nigga";
   private boolean blocked;
   private List reasons = Collections.synchronizedList(new ArrayList());

   public void block(Object reason) {
      if (reason == null) {
         throw new IllegalArgumentException("Reason cannot be NULL!");
      } else if (!this.reasons.contains(reason)) {
         this.reasons.add(reason);
         if (!this.blocked) {
            this.blocked = true;
            this.blockElement(reason);
         }
      }
   }

   public void unblock(Object reason) {
      if (this.blocked && (this.reasons.contains(reason) || reason.equals(UNIVERSAL_UNBLOCK))) {
         this.reasons.remove(reason);
         if (reason.equals(UNIVERSAL_UNBLOCK)) {
            this.reasons.clear();
         }

         if (this.reasons.isEmpty()) {
            this.blocked = false;
            this.unblockElement(reason);
         }
      }
   }

   public boolean isBlocked() {
      return this.blocked;
   }

   protected List getBlockList() {
      List r = new ArrayList();
      Iterator var3 = this.reasons.iterator();

      while(var3.hasNext()) {
         Object o = var3.next();
         r.add(o);
      }

      return r;
   }

   protected abstract void blockElement(Object var1);

   protected abstract void unblockElement(Object var1);
}
