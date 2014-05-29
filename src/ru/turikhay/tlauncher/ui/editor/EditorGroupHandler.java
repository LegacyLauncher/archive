package ru.turikhay.tlauncher.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EditorGroupHandler {
   private final List listeners;
   private final int checkedLimit;
   private int changedFlag;
   private int checkedFlag;

   public EditorGroupHandler(EditorHandler... handlers) {
      if (handlers == null) {
         throw new NullPointerException();
      } else {
         this.checkedLimit = handlers.length;
         EditorFieldListener listener = new EditorFieldListener() {
            protected void onChange(EditorHandler handler, String oldValue, String newValue) {
               if (newValue != null) {
                  EditorGroupHandler var10000;
                  if (!newValue.equals(oldValue)) {
                     var10000 = EditorGroupHandler.this;
                     var10000.changedFlag = var10000.changedFlag + 1;
                  }

                  var10000 = EditorGroupHandler.this;
                  var10000.checkedFlag = var10000.checkedFlag + 1;
                  if (EditorGroupHandler.this.checkedFlag == EditorGroupHandler.this.checkedLimit) {
                     if (EditorGroupHandler.this.changedFlag > 0) {
                        Iterator var5 = EditorGroupHandler.this.listeners.iterator();

                        while(var5.hasNext()) {
                           EditorFieldChangeListener listener = (EditorFieldChangeListener)var5.next();
                           listener.onChange((String)null, (String)null);
                        }
                     }

                     var10000 = EditorGroupHandler.this;
                     EditorGroupHandler.this.changedFlag = 0;
                     var10000.checkedFlag = 0;
                  }

               }
            }
         };

         for(int i = 0; i < handlers.length; ++i) {
            EditorHandler handler = handlers[i];
            if (handler == null) {
               throw new NullPointerException("Handler is NULL at " + i);
            }

            handler.addListener(listener);
         }

         EditorHandler[] handlers1 = new EditorHandler[handlers.length];
         System.arraycopy(handlers, 0, handlers1, 0, handlers.length);
         this.listeners = Collections.synchronizedList(new ArrayList());
      }
   }

   public boolean addListener(EditorFieldChangeListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         return this.listeners.add(listener);
      }
   }

   public boolean removeListener(EditorFieldChangeListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         return this.listeners.remove(listener);
      }
   }
}
