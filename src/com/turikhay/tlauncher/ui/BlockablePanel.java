package com.turikhay.tlauncher.ui;

import java.awt.Panel;

public abstract class BlockablePanel extends Panel implements Blockable {
   private static final long serialVersionUID = 1L;
   public static final Object UNIVERSAL_UNBLOCK = "lol, nigga";
   private boolean blocked;
   private Object reason = new Object();

   public void block(Object reason) {
      if (reason == null) {
         throw new IllegalArgumentException("Reason cannot be NULL!");
      } else if (!this.blocked) {
         this.blocked = true;
         this.reason = reason;
         this.blockElement(reason);
      }
   }

   public void unblock(Object reason) {
      if (this.blocked && (reason.equals(this.reason) || reason.equals(UNIVERSAL_UNBLOCK))) {
         this.blocked = false;
         this.reason = new Object();
         this.unblockElement(reason);
      }
   }

   public boolean isBlocked() {
      return this.blocked;
   }

   protected abstract void blockElement(Object var1);

   protected abstract void unblockElement(Object var1);
}
