package ru.turikhay.tlauncher.ui.swing;

import java.util.Collection;
import java.util.Vector;
import javax.swing.AbstractListModel;

public class SimpleListModel extends AbstractListModel {
   protected final Vector vector = new Vector();

   public int getSize() {
      return this.vector.size();
   }

   public Object getElementAt(int index) {
      return index >= 0 && index < this.getSize() ? this.vector.get(index) : null;
   }

   public void add(Object elem) {
      int index = this.vector.size();
      this.vector.add(elem);
      this.fireIntervalAdded(this, index, index);
   }

   public void addAll(Collection elem) {
      int size = elem.size();
      if (size != 0) {
         int index0 = this.vector.size();
         int index1 = index0 + size - 1;
         this.vector.addAll(elem);
         this.fireIntervalAdded(this, index0, index1);
      }

   }

   public void clear() {
      int index1 = this.vector.size() - 1;
      this.vector.clear();
      if (index1 >= 0) {
         this.fireIntervalRemoved(this, 0, index1);
      }

   }

   public int indexOf(Object elem) {
      return this.vector.indexOf(elem);
   }

   public String toString() {
      return this.vector.toString();
   }
}
