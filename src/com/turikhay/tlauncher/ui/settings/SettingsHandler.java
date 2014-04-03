package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.block.Blockable;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class SettingsHandler implements Blockable {
   private final String path;
   private String value;
   private final List listeners;

   SettingsHandler(String path) {
      if (path == null) {
         throw new NullPointerException();
      } else {
         this.path = path;
         this.listeners = Collections.synchronizedList(new ArrayList());
      }
   }

   public boolean addListener(SettingsFieldListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         return this.listeners.add(listener);
      }
   }

   public boolean removeListener(SettingsFieldListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         return this.listeners.remove(listener);
      }
   }

   void onChange(String newvalue) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         SettingsFieldListener listener = (SettingsFieldListener)var3.next();
         listener.onChange(this, this.value, newvalue);
      }

      this.value = newvalue;
   }

   public String getPath() {
      return this.path;
   }

   public void updateValue(Object obj) {
      String val = obj == null ? null : obj.toString();
      this.onChange(val);
      this.setValue0(this.value);
   }

   public void setValue(Object obj) {
      String val = obj == null ? null : obj.toString();
      this.setValue0(val);
   }

   public abstract boolean isValid();

   public abstract Component getComponent();

   public abstract String getValue();

   protected abstract void setValue0(String var1);

   public String toString() {
      return this.getClass().getSimpleName() + "{path='" + this.path + "', value='" + this.value + "'}";
   }
}
