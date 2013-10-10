package joptsimple.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class AbbreviationMap {
   private String key;
   private Object value;
   private final Map children = new TreeMap();
   private int keysBeyond;

   public boolean contains(String aKey) {
      return this.get(aKey) != null;
   }

   public Object get(String aKey) {
      char[] chars = charsOf(aKey);
      AbbreviationMap child = this;
      char[] arr$ = chars;
      int len$ = chars.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         char each = arr$[i$];
         child = (AbbreviationMap)child.children.get(each);
         if (child == null) {
            return null;
         }
      }

      return child.value;
   }

   public void put(String aKey, Object newValue) {
      if (newValue == null) {
         throw new NullPointerException();
      } else if (aKey.length() == 0) {
         throw new IllegalArgumentException();
      } else {
         char[] chars = charsOf(aKey);
         this.add(chars, newValue, 0, chars.length);
      }
   }

   public void putAll(Collection keys, Object newValue) {
      Iterator i$ = keys.iterator();

      while(i$.hasNext()) {
         String each = (String)i$.next();
         this.put(each, newValue);
      }

   }

   private boolean add(char[] chars, Object newValue, int offset, int length) {
      if (offset == length) {
         this.value = newValue;
         boolean wasAlreadyAKey = this.key != null;
         this.key = new String(chars);
         return !wasAlreadyAKey;
      } else {
         char nextChar = chars[offset];
         AbbreviationMap child = (AbbreviationMap)this.children.get(nextChar);
         if (child == null) {
            child = new AbbreviationMap();
            this.children.put(nextChar, child);
         }

         boolean newKeyAdded = child.add(chars, newValue, offset + 1, length);
         if (newKeyAdded) {
            ++this.keysBeyond;
         }

         if (this.key == null) {
            this.value = this.keysBeyond > 1 ? null : newValue;
         }

         return newKeyAdded;
      }
   }

   public void remove(String aKey) {
      if (aKey.length() == 0) {
         throw new IllegalArgumentException();
      } else {
         char[] keyChars = charsOf(aKey);
         this.remove(keyChars, 0, keyChars.length);
      }
   }

   private boolean remove(char[] aKey, int offset, int length) {
      if (offset == length) {
         return this.removeAtEndOfKey();
      } else {
         char nextChar = aKey[offset];
         AbbreviationMap child = (AbbreviationMap)this.children.get(nextChar);
         if (child != null && child.remove(aKey, offset + 1, length)) {
            --this.keysBeyond;
            if (child.keysBeyond == 0) {
               this.children.remove(nextChar);
            }

            if (this.keysBeyond == 1 && this.key == null) {
               this.setValueToThatOfOnlyChild();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private void setValueToThatOfOnlyChild() {
      Entry entry = (Entry)this.children.entrySet().iterator().next();
      AbbreviationMap onlyChild = (AbbreviationMap)entry.getValue();
      this.value = onlyChild.value;
   }

   private boolean removeAtEndOfKey() {
      if (this.key == null) {
         return false;
      } else {
         this.key = null;
         if (this.keysBeyond == 1) {
            this.setValueToThatOfOnlyChild();
         } else {
            this.value = null;
         }

         return true;
      }
   }

   public Map toJavaUtilMap() {
      Map mappings = new TreeMap();
      this.addToMappings(mappings);
      return mappings;
   }

   private void addToMappings(Map mappings) {
      if (this.key != null) {
         mappings.put(this.key, this.value);
      }

      Iterator i$ = this.children.values().iterator();

      while(i$.hasNext()) {
         AbbreviationMap each = (AbbreviationMap)i$.next();
         each.addToMappings(mappings);
      }

   }

   private static char[] charsOf(String aKey) {
      char[] chars = new char[aKey.length()];
      aKey.getChars(0, aKey.length(), chars, 0);
      return chars;
   }
}
