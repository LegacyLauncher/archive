package com.turikhay.util.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AsyncObjectContainer {
   private final List objects;
   private final Map values;
   private boolean executionLock;

   public AsyncObjectContainer() {
      this.objects = new ArrayList();
      this.values = new HashMap();
   }

   public AsyncObjectContainer(AsyncObject[] asyncObjects) {
      this();
      AsyncObject[] var5 = asyncObjects;
      int var4 = asyncObjects.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         AsyncObject object = var5[var3];
         this.add(object);
      }

   }

   public Map execute() {
      this.executionLock = true;
      this.values.clear();
      synchronized(this.objects) {
         int i = 0;
         int size = this.objects.size();
         Iterator var5 = this.objects.iterator();

         AsyncObject object;
         while(var5.hasNext()) {
            object = (AsyncObject)var5.next();
            object.start();
         }

         while(i < size) {
            var5 = this.objects.iterator();

            while(var5.hasNext()) {
               object = (AsyncObject)var5.next();

               try {
                  if (!this.values.containsKey(object)) {
                     this.values.put(object, object.getValue());
                     ++i;
                  }
               } catch (AsyncObjectNotReadyException var7) {
               } catch (AsyncObjectGotErrorException var8) {
                  this.values.put(object, (Object)null);
                  ++i;
               }
            }
         }
      }

      this.executionLock = false;
      return this.values;
   }

   public void add(AsyncObject object) {
      if (object == null) {
         throw new NullPointerException();
      } else {
         synchronized(this.objects) {
            if (this.executionLock) {
               throw new AsyncContainerLockedException();
            } else {
               this.objects.add(object);
            }
         }
      }
   }

   public void remove(AsyncObject object) {
      if (object == null) {
         throw new NullPointerException();
      } else {
         synchronized(this.objects) {
            if (this.executionLock) {
               throw new AsyncContainerLockedException();
            } else {
               this.objects.remove(object);
            }
         }
      }
   }

   public void removeAll() {
      synchronized(this.objects) {
         if (this.executionLock) {
            throw new AsyncContainerLockedException();
         } else {
            this.objects.clear();
         }
      }
   }
}
