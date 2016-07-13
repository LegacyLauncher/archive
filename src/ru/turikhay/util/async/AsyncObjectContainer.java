package ru.turikhay.util.async;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsyncObjectContainer {
   private final List objects = new ArrayList();
   private final Map values = new LinkedHashMap();
   private final Object waitLock = new Object();
   private boolean executionLock;

   public Map execute() {
      this.executionLock = true;
      this.values.clear();
      synchronized(this.objects) {
         Iterator var2 = this.objects.iterator();

         while(var2.hasNext()) {
            AsyncObject object = (AsyncObject)var2.next();
            object.start();
         }

         boolean hasRemaining;
         do {
            hasRemaining = false;
            Iterator var14 = this.objects.iterator();

            while(var14.hasNext()) {
               AsyncObject object = (AsyncObject)var14.next();

               try {
                  if (!this.values.containsKey(object)) {
                     this.values.put(object, object.getValue());
                  }
               } catch (AsyncObjectNotReadyException var8) {
                  hasRemaining = true;
               } catch (AsyncObjectGotErrorException var9) {
                  this.values.put(object, (Object)null);
               }
            }

            if (hasRemaining) {
               Object var10000;
               synchronized(this.waitLock) {
                  try {
                     this.waitLock.wait();
                     continue;
                  } catch (InterruptedException var10) {
                     var10000 = null;
                  }
               }

               return (Map)var10000;
            }
         } while(hasRemaining);
      }

      this.executionLock = false;
      return this.values;
   }

   public void add(AsyncObject object) {
      if (object == null) {
         throw new NullPointerException();
      } else if (object.getContainer() != null) {
         throw new IllegalArgumentException();
      } else {
         synchronized(this.objects) {
            if (this.executionLock) {
               throw new AsyncContainerLockedException();
            } else {
               this.objects.add(object);
               object.setContainer(this);
            }
         }
      }
   }

   void release() {
      synchronized(this.waitLock) {
         this.waitLock.notifyAll();
      }
   }
}
