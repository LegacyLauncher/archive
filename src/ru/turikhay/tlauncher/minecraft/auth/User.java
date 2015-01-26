package ru.turikhay.tlauncher.minecraft.auth;

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
}
