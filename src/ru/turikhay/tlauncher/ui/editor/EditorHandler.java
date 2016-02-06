package ru.turikhay.tlauncher.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.block.Blockable;

public abstract class EditorHandler implements Blockable {
   private final String path;
   private String value;
   private final List listeners;

   public EditorHandler(String path) {
      this.path = path;
      this.listeners = Collections.synchronizedList(new ArrayList());
   }

   public boolean addListener(EditorFieldListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         return this.listeners.add(listener);
      }
   }

   public void onChange(String newvalue) {
      if (TLauncher.getInstance().isReady()) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            EditorFieldListener listener = (EditorFieldListener)var3.next();
            listener.onChange(this, this.value, newvalue);
         }
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

   public abstract JComponent getComponent();

   public abstract String getValue();

   protected abstract void setValue0(String var1);

   public String toString() {
      return this.getClass().getSimpleName() + "{path='" + this.path + "', value='" + this.value + "'}";
   }
}
