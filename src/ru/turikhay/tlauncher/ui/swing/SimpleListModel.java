package ru.turikhay.tlauncher.ui.swing;

import java.util.Collection;
import java.util.Vector;
import javax.swing.AbstractListModel;

public class SimpleListModel extends AbstractListModel {
   private static final long serialVersionUID = 727845864028652893L;
   protected final Vector vector = new Vector();

   public int getSize() {
      return this.vector.size();
   }

   public Object getElementAt(int index) {
      return this.vector.get(index);
   }

   public void add(Object elem) {
      int index = this.vector.size();
      this.vector.add(elem);
      this.fireIntervalAdded(this, index, index);
   }

   public boolean remove(Object elem) {
      int index = this.indexOf(elem);
      boolean rv = this.vector.removeElement(elem);
      if (index >= 0) {
         this.fireIntervalRemoved(this, index, index);
      }

      return rv;
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

   public boolean isEmpty() {
      return this.vector.isEmpty();
   }

   public boolean contains(Object elem) {
      return this.vector.contains(elem);
   }

   public int indexOf(Object elem) {
      return this.vector.indexOf(elem);
   }

   public int indexOf(Object elem, int index) {
      return this.vector.indexOf(elem, index);
   }

   public Object elementAt(int index) {
      return this.vector.elementAt(index);
   }

   public String toString() {
      return this.vector.toString();
   }
}
