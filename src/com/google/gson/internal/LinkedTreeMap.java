package com.google.gson.internal;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

public final class LinkedTreeMap extends AbstractMap implements Serializable {
   private static final Comparator NATURAL_ORDER = new Comparator() {
      public int compare(Comparable a, Comparable b) {
         return a.compareTo(b);
      }
   };
   Comparator comparator;
   LinkedTreeMap.Node root;
   int size;
   int modCount;
   final LinkedTreeMap.Node header;
   private LinkedTreeMap.EntrySet entrySet;
   private LinkedTreeMap.KeySet keySet;

   public LinkedTreeMap() {
      this(NATURAL_ORDER);
   }

   public LinkedTreeMap(Comparator comparator) {
      this.size = 0;
      this.modCount = 0;
      this.header = new LinkedTreeMap.Node();
      this.comparator = comparator != null ? comparator : NATURAL_ORDER;
   }

   public int size() {
      return this.size;
   }

   public Object get(Object key) {
      LinkedTreeMap.Node node = this.findByObject(key);
      return node != null ? node.value : null;
   }

   public boolean containsKey(Object key) {
      return this.findByObject(key) != null;
   }

   public Object put(Object key, Object value) {
      if (key == null) {
         throw new NullPointerException("key == null");
      } else {
         LinkedTreeMap.Node created = this.find(key, true);
         Object result = created.value;
         created.value = value;
         return result;
      }
   }

   public void clear() {
      this.root = null;
      this.size = 0;
      ++this.modCount;
      LinkedTreeMap.Node header = this.header;
      header.next = header.prev = header;
   }

   public Object remove(Object key) {
      LinkedTreeMap.Node node = this.removeInternalByKey(key);
      return node != null ? node.value : null;
   }

   LinkedTreeMap.Node find(Object key, boolean create) {
      Comparator comparator = this.comparator;
      LinkedTreeMap.Node nearest = this.root;
      int comparison = 0;
      LinkedTreeMap.Node created;
      if (nearest != null) {
         Comparable comparableKey = comparator == NATURAL_ORDER ? (Comparable)key : null;

         while(true) {
            comparison = comparableKey != null ? comparableKey.compareTo(nearest.key) : comparator.compare(key, nearest.key);
            if (comparison == 0) {
               return nearest;
            }

            created = comparison < 0 ? nearest.left : nearest.right;
            if (created == null) {
               break;
            }

            nearest = created;
         }
      }

      if (!create) {
         return null;
      } else {
         LinkedTreeMap.Node header = this.header;
         if (nearest == null) {
            if (comparator == NATURAL_ORDER && !(key instanceof Comparable)) {
               throw new ClassCastException(key.getClass().getName() + " is not Comparable");
            }

            created = new LinkedTreeMap.Node(nearest, key, header, header.prev);
            this.root = created;
         } else {
            created = new LinkedTreeMap.Node(nearest, key, header, header.prev);
            if (comparison < 0) {
               nearest.left = created;
            } else {
               nearest.right = created;
            }

            this.rebalance(nearest, true);
         }

         ++this.size;
         ++this.modCount;
         return created;
      }
   }

   LinkedTreeMap.Node findByObject(Object key) {
      try {
         return key != null ? this.find(key, false) : null;
      } catch (ClassCastException var3) {
         return null;
      }
   }

   LinkedTreeMap.Node findByEntry(Entry entry) {
      LinkedTreeMap.Node mine = this.findByObject(entry.getKey());
      boolean valuesEqual = mine != null && this.equal(mine.value, entry.getValue());
      return valuesEqual ? mine : null;
   }

   private boolean equal(Object a, Object b) {
      return a == b || a != null && a.equals(b);
   }

   void removeInternal(LinkedTreeMap.Node node, boolean unlink) {
      if (unlink) {
         node.prev.next = node.next;
         node.next.prev = node.prev;
      }

      LinkedTreeMap.Node left = node.left;
      LinkedTreeMap.Node right = node.right;
      LinkedTreeMap.Node originalParent = node.parent;
      if (left != null && right != null) {
         LinkedTreeMap.Node adjacent = left.height > right.height ? left.last() : right.first();
         this.removeInternal(adjacent, false);
         int leftHeight = 0;
         left = node.left;
         if (left != null) {
            leftHeight = left.height;
            adjacent.left = left;
            left.parent = adjacent;
            node.left = null;
         }

         int rightHeight = 0;
         right = node.right;
         if (right != null) {
            rightHeight = right.height;
            adjacent.right = right;
            right.parent = adjacent;
            node.right = null;
         }

         adjacent.height = Math.max(leftHeight, rightHeight) + 1;
         this.replaceInParent(node, adjacent);
      } else {
         if (left != null) {
            this.replaceInParent(node, left);
            node.left = null;
         } else if (right != null) {
            this.replaceInParent(node, right);
            node.right = null;
         } else {
            this.replaceInParent(node, (LinkedTreeMap.Node)null);
         }

         this.rebalance(originalParent, false);
         --this.size;
         ++this.modCount;
      }
   }

   LinkedTreeMap.Node removeInternalByKey(Object key) {
      LinkedTreeMap.Node node = this.findByObject(key);
      if (node != null) {
         this.removeInternal(node, true);
      }

      return node;
   }

   private void replaceInParent(LinkedTreeMap.Node node, LinkedTreeMap.Node replacement) {
      LinkedTreeMap.Node parent = node.parent;
      node.parent = null;
      if (replacement != null) {
         replacement.parent = parent;
      }

      if (parent != null) {
         if (parent.left == node) {
            parent.left = replacement;
         } else {
            assert parent.right == node;

            parent.right = replacement;
         }
      } else {
         this.root = replacement;
      }

   }

   private void rebalance(LinkedTreeMap.Node unbalanced, boolean insert) {
      for(LinkedTreeMap.Node node = unbalanced; node != null; node = node.parent) {
         LinkedTreeMap.Node left = node.left;
         LinkedTreeMap.Node right = node.right;
         int leftHeight = left != null ? left.height : 0;
         int rightHeight = right != null ? right.height : 0;
         int delta = leftHeight - rightHeight;
         LinkedTreeMap.Node leftLeft;
         LinkedTreeMap.Node leftRight;
         int leftRightHeight;
         int leftLeftHeight;
         int leftDelta;
         if (delta == -2) {
            leftLeft = right.left;
            leftRight = right.right;
            leftRightHeight = leftRight != null ? leftRight.height : 0;
            leftLeftHeight = leftLeft != null ? leftLeft.height : 0;
            leftDelta = leftLeftHeight - leftRightHeight;
            if (leftDelta != -1 && (leftDelta != 0 || insert)) {
               assert leftDelta == 1;

               this.rotateRight(right);
               this.rotateLeft(node);
            } else {
               this.rotateLeft(node);
            }

            if (insert) {
               break;
            }
         } else if (delta == 2) {
            leftLeft = left.left;
            leftRight = left.right;
            leftRightHeight = leftRight != null ? leftRight.height : 0;
            leftLeftHeight = leftLeft != null ? leftLeft.height : 0;
            leftDelta = leftLeftHeight - leftRightHeight;
            if (leftDelta == 1 || leftDelta == 0 && !insert) {
               this.rotateRight(node);
            } else {
               assert leftDelta == -1;

               this.rotateLeft(left);
               this.rotateRight(node);
            }

            if (insert) {
               break;
            }
         } else if (delta == 0) {
            node.height = leftHeight + 1;
            if (insert) {
               break;
            }
         } else {
            assert delta == -1 || delta == 1;

            node.height = Math.max(leftHeight, rightHeight) + 1;
            if (!insert) {
               break;
            }
         }
      }

   }

   private void rotateLeft(LinkedTreeMap.Node root) {
      LinkedTreeMap.Node left = root.left;
      LinkedTreeMap.Node pivot = root.right;
      LinkedTreeMap.Node pivotLeft = pivot.left;
      LinkedTreeMap.Node pivotRight = pivot.right;
      root.right = pivotLeft;
      if (pivotLeft != null) {
         pivotLeft.parent = root;
      }

      this.replaceInParent(root, pivot);
      pivot.left = root;
      root.parent = pivot;
      root.height = Math.max(left != null ? left.height : 0, pivotLeft != null ? pivotLeft.height : 0) + 1;
      pivot.height = Math.max(root.height, pivotRight != null ? pivotRight.height : 0) + 1;
   }

   private void rotateRight(LinkedTreeMap.Node root) {
      LinkedTreeMap.Node pivot = root.left;
      LinkedTreeMap.Node right = root.right;
      LinkedTreeMap.Node pivotLeft = pivot.left;
      LinkedTreeMap.Node pivotRight = pivot.right;
      root.left = pivotRight;
      if (pivotRight != null) {
         pivotRight.parent = root;
      }

      this.replaceInParent(root, pivot);
      pivot.right = root;
      root.parent = pivot;
      root.height = Math.max(right != null ? right.height : 0, pivotRight != null ? pivotRight.height : 0) + 1;
      pivot.height = Math.max(root.height, pivotLeft != null ? pivotLeft.height : 0) + 1;
   }

   public Set entrySet() {
      LinkedTreeMap.EntrySet result = this.entrySet;
      return result != null ? result : (this.entrySet = new LinkedTreeMap.EntrySet());
   }

   public Set keySet() {
      LinkedTreeMap.KeySet result = this.keySet;
      return result != null ? result : (this.keySet = new LinkedTreeMap.KeySet());
   }

   private Object writeReplace() throws ObjectStreamException {
      return new LinkedHashMap(this);
   }

   class KeySet extends AbstractSet {
      public int size() {
         return LinkedTreeMap.this.size;
      }

      public Iterator iterator() {
         return new LinkedTreeMap.LinkedTreeMapIterator() {
            public Object next() {
               return this.nextNode().key;
            }
         };
      }

      public boolean contains(Object o) {
         return LinkedTreeMap.this.containsKey(o);
      }

      public boolean remove(Object key) {
         return LinkedTreeMap.this.removeInternalByKey(key) != null;
      }

      public void clear() {
         LinkedTreeMap.this.clear();
      }
   }

   class EntrySet extends AbstractSet {
      public int size() {
         return LinkedTreeMap.this.size;
      }

      public Iterator iterator() {
         return new LinkedTreeMap.LinkedTreeMapIterator() {
            public Entry next() {
               return this.nextNode();
            }
         };
      }

      public boolean contains(Object o) {
         return o instanceof Entry && LinkedTreeMap.this.findByEntry((Entry)o) != null;
      }

      public boolean remove(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            LinkedTreeMap.Node node = LinkedTreeMap.this.findByEntry((Entry)o);
            if (node == null) {
               return false;
            } else {
               LinkedTreeMap.this.removeInternal(node, true);
               return true;
            }
         }
      }

      public void clear() {
         LinkedTreeMap.this.clear();
      }
   }

   private abstract class LinkedTreeMapIterator implements Iterator {
      LinkedTreeMap.Node next;
      LinkedTreeMap.Node lastReturned;
      int expectedModCount;

      private LinkedTreeMapIterator() {
         this.next = LinkedTreeMap.this.header.next;
         this.lastReturned = null;
         this.expectedModCount = LinkedTreeMap.this.modCount;
      }

      public final boolean hasNext() {
         return this.next != LinkedTreeMap.this.header;
      }

      final LinkedTreeMap.Node nextNode() {
         LinkedTreeMap.Node e = this.next;
         if (e == LinkedTreeMap.this.header) {
            throw new NoSuchElementException();
         } else if (LinkedTreeMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         } else {
            this.next = e.next;
            return this.lastReturned = e;
         }
      }

      public final void remove() {
         if (this.lastReturned == null) {
            throw new IllegalStateException();
         } else {
            LinkedTreeMap.this.removeInternal(this.lastReturned, true);
            this.lastReturned = null;
            this.expectedModCount = LinkedTreeMap.this.modCount;
         }
      }

      // $FF: synthetic method
      LinkedTreeMapIterator(Object x1) {
         this();
      }
   }

   static final class Node implements Entry {
      LinkedTreeMap.Node parent;
      LinkedTreeMap.Node left;
      LinkedTreeMap.Node right;
      LinkedTreeMap.Node next;
      LinkedTreeMap.Node prev;
      final Object key;
      Object value;
      int height;

      Node() {
         this.key = null;
         this.next = this.prev = this;
      }

      Node(LinkedTreeMap.Node parent, Object key, LinkedTreeMap.Node next, LinkedTreeMap.Node prev) {
         this.parent = parent;
         this.key = key;
         this.height = 1;
         this.next = next;
         this.prev = prev;
         prev.next = this;
         next.prev = this;
      }

      public Object getKey() {
         return this.key;
      }

      public Object getValue() {
         return this.value;
      }

      public Object setValue(Object value) {
         Object oldValue = this.value;
         this.value = value;
         return oldValue;
      }

      public boolean equals(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            boolean var10000;
            label38: {
               label27: {
                  Entry other = (Entry)o;
                  if (this.key == null) {
                     if (other.getKey() != null) {
                        break label27;
                     }
                  } else if (!this.key.equals(other.getKey())) {
                     break label27;
                  }

                  if (this.value == null) {
                     if (other.getValue() == null) {
                        break label38;
                     }
                  } else if (this.value.equals(other.getValue())) {
                     break label38;
                  }
               }

               var10000 = false;
               return var10000;
            }

            var10000 = true;
            return var10000;
         }
      }

      public int hashCode() {
         return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
      }

      public String toString() {
         return this.key + "=" + this.value;
      }

      public LinkedTreeMap.Node first() {
         LinkedTreeMap.Node node = this;

         for(LinkedTreeMap.Node child = this.left; child != null; child = child.left) {
            node = child;
         }

         return node;
      }

      public LinkedTreeMap.Node last() {
         LinkedTreeMap.Node node = this;

         for(LinkedTreeMap.Node child = this.right; child != null; child = child.right) {
            node = child;
         }

         return node;
      }
   }
}
