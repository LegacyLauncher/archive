package org.apache.commons.lang3;

import java.io.Serializable;

public class ObjectUtils {
   public static final ObjectUtils.Null NULL = new ObjectUtils.Null();

   public static String toString(Object obj) {
      return obj == null ? "" : obj.toString();
   }

   public static class Null implements Serializable {
   }
}
