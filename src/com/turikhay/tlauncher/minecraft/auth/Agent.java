package com.turikhay.tlauncher.minecraft.auth;

public class Agent {
   public static final Agent MINECRAFT = new Agent("Minecraft", 1);
   private final String name;
   private final int version;

   private Agent(String name, int version) {
      this.name = name;
      this.version = version;
   }

   public String getName() {
      return this.name;
   }

   public int getVersion() {
      return this.version;
   }

   public String toString() {
      return "Agent{name='" + this.name + '\'' + ", version=" + this.version + '}';
   }
}
