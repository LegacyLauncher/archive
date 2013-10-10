package com.google.gson;

public enum LongSerializationPolicy {
   DEFAULT {
      public JsonElement serialize(Long value) {
         return new JsonPrimitive(value);
      }
   },
   STRING {
      public JsonElement serialize(Long value) {
         return new JsonPrimitive(String.valueOf(value));
      }
   };

   private LongSerializationPolicy() {
   }

   public abstract JsonElement serialize(Long var1);

   // $FF: synthetic method
   LongSerializationPolicy(Object x2) {
      this();
   }
}
