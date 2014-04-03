package com.turikhay.util;

import com.turikhay.exceptions.ParseException;

public class IntegerArray {
   public static final char defaultDelimiter = ';';
   private final int[] integers;
   private final char delimiter;
   private final int length;

   private IntegerArray(char del, int... values) {
      this.delimiter = del;
      this.length = values.length;
      this.integers = new int[this.length];
      System.arraycopy(values, 0, this.integers, 0, this.length);
   }

   public IntegerArray(int... values) {
      this(';', values);
   }

   public int get(int pos) {
      if (pos >= 0 && pos < this.length) {
         return this.integers[pos];
      } else {
         throw new ArrayIndexOutOfBoundsException("Invalid position (" + pos + " / " + this.length + ")!");
      }
   }

   public void set(int pos, int val) {
      if (pos >= 0 && pos < this.length) {
         this.integers[pos] = val;
      } else {
         throw new ArrayIndexOutOfBoundsException("Invalid position (" + pos + " / " + this.length + ")!");
      }
   }

   public int[] toArray() {
      int[] r = new int[this.length];
      System.arraycopy(this.integers, 0, r, 0, this.length);
      return r;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      int[] var6;
      int var5 = (var6 = this.integers).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         int i = var6[var4];
         if (!first) {
            sb.append(this.delimiter);
         } else {
            first = false;
         }

         sb.append(i);
      }

      return sb.toString();
   }

   private static IntegerArray parseIntegerArray(String val, char del) throws ParseException {
      if (val == null) {
         throw new ParseException("String cannot be NULL!");
      } else if (val.length() <= 1) {
         throw new ParseException("String mustn't equal or be less than delimiter!");
      } else {
         String[] ints = val.split("(?<!\\\\)\\" + del);
         int l = ints.length;
         int[] arr = new int[l];

         for(int i = 0; i < l; ++i) {
            int cur;
            try {
               cur = Integer.parseInt(ints[i]);
            } catch (NumberFormatException var8) {
               U.log("Cannot parse integer (iteration: " + i + ")", var8);
               throw new ParseException("Cannot parse integer (iteration: " + i + ")", var8);
            }

            arr[i] = cur;
         }

         return new IntegerArray(del, arr);
      }
   }

   public static IntegerArray parseIntegerArray(String val) throws ParseException {
      return parseIntegerArray(val, ';');
   }

   private static int[] toArray(String val, char del) throws ParseException {
      IntegerArray arr = parseIntegerArray(val, del);
      return arr.toArray();
   }

   public static int[] toArray(String val) throws ParseException {
      return toArray(val, ';');
   }
}
