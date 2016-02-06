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

   public EditorGroupHandler(List handlers) {
      if (handlers == null) {
         throw new NullPointerException();
      } else {
         this.checkedLimit = handlers.size();
         EditorFieldListener listener = new EditorFieldListener() {
            protected void onChange(EditorHandler handler, String oldValue, String newValue) {
               if (newValue != null) {
                  if (!newValue.equals(oldValue)) {
                     EditorGroupHandler.this.changedFlag = EditorGroupHandler.this.changedFlag + 1;
                  }

                  EditorGroupHandler.this.checkedFlag = EditorGroupHandler.this.checkedFlag + 1;
                  if (EditorGroupHandler.this.checkedFlag == EditorGroupHandler.this.checkedLimit) {
                     if (EditorGroupHandler.this.changedFlag > 0) {
                        Iterator var5 = EditorGroupHandler.this.listeners.iterator();

                        while(var5.hasNext()) {
                           EditorFieldChangeListener listener = (EditorFieldChangeListener)var5.next();
                           listener.onChange((String)null, (String)null);
                        }
                     }

                     EditorGroupHandler var10000 = EditorGroupHandler.this;
                     EditorGroupHandler.this.changedFlag = 0;
                     var10000.checkedFlag = 0;
                  }
               }

            }
         };

         for(int i = 0; i < handlers.size(); ++i) {
            EditorHandler handler = (EditorHandler)handlers.get(i);
            if (handler == null) {
               throw new NullPointerException("Handler is NULL at " + i);
            }

            handler.addListener(listener);
         }

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
}
