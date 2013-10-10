package joptsimple.util;

public final class KeyValuePair {
   public final String key;
   public final String value;

   private KeyValuePair(String key, String value) {
      this.key = key;
      this.value = value;
   }

   public static KeyValuePair valueOf(String stringRepresentation) {
      int equalsIndex = stringRepresentation.indexOf(61);
      if (equalsIndex == -1) {
         return new KeyValuePair(stringRepresentation, "");
      } else {
         String aKey = stringRepresentation.substring(0, equalsIndex);
         String aValue;
         if (equalsIndex == stringRepresentation.length() - 1) {
            aValue = "";
         } else {
            aValue = stringRepresentation.substring(equalsIndex + 1);
         }

         return new KeyValuePair(aKey, aValue);
      }
   }

   public boolean equals(Object that) {
      if (!(that instanceof KeyValuePair)) {
         return false;
      } else {
         KeyValuePair other = (KeyValuePair)that;
         return this.key.equals(other.key) && this.value.equals(other.value);
      }
   }

   public int hashCode() {
      return this.key.hashCode() ^ this.value.hashCode();
   }

   public String toString() {
      return this.key + '=' + this.value;
   }
}
