package ru.turikhay.util;

import java.awt.Dimension;
import ru.turikhay.exceptions.ParseException;

public class IntegerArray {
   public static final char defaultDelimiter = ';';
   private final int[] integers;
   private final char delimiter;
   private final int length;

   public IntegerArray(char del, int... values) {
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

   public int size() {
      return this.length;
   }

   public int[] toArray() {
      int[] r = new int[this.length];
      System.arraycopy(this.integers, 0, r, 0, this.length);
      return r;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      int[] var6 = this.integers;
      int var5 = this.integers.length;

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

   public static IntegerArray parseIntegerArray(String val, char del) throws ParseException {
      if (val == null) {
         throw new ParseException("String cannot be NULL!");
      } else if (val.length() <= 1) {
         throw new ParseException("String mustn't equal or be less than delimiter!");
      } else {
         String regexp = "(?<!\\\\)";
         if (del != 'x') {
            regexp = regexp + "\\";
         }

         regexp = regexp + del;
         String[] ints = val.split(regexp);
         int l = ints.length;
         int[] arr = new int[l];

         for(int i = 0; i < l; ++i) {
            int cur;
            try {
               cur = Integer.parseInt(ints[i]);
            } catch (NumberFormatException var9) {
               throw new ParseException("Cannot parse integer (iteration: " + i + ", del: \"" + del + "\")", var9);
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

   public static IntegerArray fromDimension(Dimension d) {
      return new IntegerArray('x', new int[]{d.width, d.height});
   }
}
