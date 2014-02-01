package com.turikhay.tlauncher.minecraft.auth;

import java.util.List;

public class User {
   private String id;
   private List properties;

   public String getID() {
      return this.id;
   }

   public List getProperties() {
      return this.properties;
   }

   public class Property {
      private String name;
      private String value;

      public String getKey() {
         return this.name;
      }

      public String getValue() {
         return this.value;
      }

      public String toString() {
         return "Property{" + this.name + " = " + this.value + "}";
      }
   }
}
