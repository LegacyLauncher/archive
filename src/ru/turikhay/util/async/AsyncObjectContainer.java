package ru.turikhay.util.async;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsyncObjectContainer {
   private final List objects;
   private final Map values;
   private boolean executionLock;

   public AsyncObjectContainer() {
      this.objects = new ArrayList();
      this.values = new LinkedHashMap();
   }

   public AsyncObjectContainer(AsyncObject[] asyncObjects) {
      this();
      AsyncObject[] arr$ = asyncObjects;
      int len$ = asyncObjects.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         AsyncObject object = arr$[i$];
         this.add(object);
      }

   }

   public Map execute() {
      this.executionLock = true;
      this.values.clear();
      synchronized(this.objects) {
         int i = 0;
         int size = this.objects.size();
         Iterator i$ = this.objects.iterator();

         AsyncObject object;
         while(i$.hasNext()) {
            object = (AsyncObject)i$.next();
            object.start();
         }

         while(i < size) {
            i$ = this.objects.iterator();

            while(i$.hasNext()) {
               object = (AsyncObject)i$.next();

               try {
                  if (!this.values.containsKey(object)) {
                     this.values.put(object, object.getValue());
                     ++i;
                  }
               } catch (AsyncObjectNotReadyException var8) {
               } catch (AsyncObjectGotErrorException var9) {
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
