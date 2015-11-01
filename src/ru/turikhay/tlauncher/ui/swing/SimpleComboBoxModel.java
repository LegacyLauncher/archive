package ru.turikhay.tlauncher.ui.swing;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;

public class SimpleComboBoxModel extends DefaultComboBoxModel {
   private static final long serialVersionUID = 5950434966721171811L;
   protected Vector objects;
   protected Object selectedObject;

   public SimpleComboBoxModel() {
      this.objects = new Vector();
   }

   public SimpleComboBoxModel(Object[] items) {
      this.objects = new Vector(items.length);
      int i = 0;

      for(int c = items.length; i < c; ++i) {
         this.objects.addElement(items[i]);
      }

      if (this.getSize() > 0) {
         this.selectedObject = this.getElementAt(0);
      }

   }

   public SimpleComboBoxModel(Vector v) {
      this.objects = v;
      if (this.getSize() > 0) {
         this.selectedObject = this.getElementAt(0);
      }

   }

   public void setSelectedItem(Object anObject) {
      if (this.selectedObject != null && !this.selectedObject.equals(anObject) || this.selectedObject == null && anObject != null) {
         this.selectedObject = anObject;
         this.fireContentsChanged(this, -1, -1);
      }

   }

   public Object getSelectedItem() {
      return this.selectedObject;
   }

   public int getSize() {
      return this.objects.size();
   }

   public Object getElementAt(int index) {
      return index >= 0 && index < this.objects.size() ? this.objects.elementAt(index) : null;
   }

   public int getIndexOf(Object anObject) {
      return this.objects.indexOf(anObject);
   }

   public void addElement(Object anObject) {
      this.objects.addElement(anObject);
      int size = this.objects.size();
      int index = this.objects.size() - 1;
      this.fireIntervalAdded(this, index, index);
      if (size == 1 && this.selectedObject == null && anObject != null) {
         this.setSelectedItem(anObject);
      }

   }

   public void addElements(Collection list) {
      if (list.size() != 0) {
         int size = list.size();
         int index0 = this.objects.size();
         int index1 = index0 + size - 1;
         this.objects.addAll(list);
         this.fireIntervalAdded(this, index0, index1);
         if (this.selectedObject == null) {
            Iterator iterator = list.iterator();

            while(iterator.hasNext()) {
               Object elem = iterator.next();
               if (elem != null) {
                  this.setSelectedItem(elem);
                  break;
               }
            }
         }
      }

   }

   public void insertElementAt(Object anObject, int index) {
      this.objects.insertElementAt(anObject, index);
      this.fireIntervalAdded(this, index, index);
   }

   public void removeElementAt(int index) {
      if (this.getElementAt(index) == this.selectedObject) {
         if (index == 0) {
            this.setSelectedItem(this.getSize() == 1 ? null : this.getElementAt(index + 1));
         } else {
            this.setSelectedItem(this.getElementAt(index - 1));
         }
      }

      this.objects.removeElementAt(index);
      this.fireIntervalRemoved(this, index, index);
   }

   public void removeElement(Object anObject) {
      int index = this.objects.indexOf(anObject);
      if (index != -1) {
         this.removeElementAt(index);
      }

   }

   public void removeAllElements() {
      int size = this.objects.size();
      if (size > 0) {
         byte firstIndex = 0;
         int lastIndex = size - 1;
         this.objects.removeAllElements();
         this.selectedObject = null;
         this.fireIntervalRemoved(this, firstIndex, lastIndex);
      } else {
         this.selectedObject = null;
      }

   }
}
