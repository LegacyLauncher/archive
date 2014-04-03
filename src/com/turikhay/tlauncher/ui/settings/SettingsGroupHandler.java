package com.turikhay.tlauncher.ui.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SettingsGroupHandler {
   private final List listeners;
   private final int checkedLimit;
   private int changedFlag;
   private int checkedFlag;

   SettingsGroupHandler(SettingsHandler... handlers) {
      if (handlers == null) {
         throw new NullPointerException();
      } else {
         this.checkedLimit = handlers.length;
         SettingsFieldListener listener = new SettingsFieldListener() {
            protected void onChange(SettingsHandler handler, String oldValue, String newValue) {
               if (newValue != null) {
                  SettingsGroupHandler var10000;
                  if (!newValue.equals(oldValue)) {
                     var10000 = SettingsGroupHandler.this;
                     var10000.changedFlag = var10000.changedFlag + 1;
                  }

                  var10000 = SettingsGroupHandler.this;
                  var10000.checkedFlag = var10000.checkedFlag + 1;
                  if (SettingsGroupHandler.this.checkedFlag == SettingsGroupHandler.this.checkedLimit) {
                     if (SettingsGroupHandler.this.changedFlag > 0) {
                        Iterator var5 = SettingsGroupHandler.this.listeners.iterator();

                        while(var5.hasNext()) {
                           SettingsFieldChangeListener listener = (SettingsFieldChangeListener)var5.next();
                           listener.onChange((String)null, (String)null);
                        }
                     }

                     var10000 = SettingsGroupHandler.this;
                     SettingsGroupHandler.this.changedFlag = 0;
                     var10000.checkedFlag = 0;
                  }

               }
            }
         };

         for(int i = 0; i < handlers.length; ++i) {
            SettingsHandler handler = handlers[i];
            if (handler == null) {
               throw new NullPointerException("Handler is NULL at " + i);
            }

            handler.addListener(listener);
         }

         SettingsHandler[] handlers1 = new SettingsHandler[handlers.length];
         System.arraycopy(handlers, 0, handlers1, 0, handlers.length);
         this.listeners = Collections.synchronizedList(new ArrayList());
      }
   }

   public boolean addListener(SettingsFieldChangeListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         return this.listeners.add(listener);
      }
   }

   public boolean removeListener(SettingsFieldChangeListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         return this.listeners.remove(listener);
      }
   }
}
