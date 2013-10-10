package com.turikhay.tlauncher.minecraft;

public class MinecraftLauncherException extends Exception {
   private static final long serialVersionUID = 7704448719401222348L;
   private String langpath = "launcher.error.";
   private Object replace = null;

   public MinecraftLauncherException(String message) {
      super(message);
      this.langpath = null;
   }

   public MinecraftLauncherException(String message, String langpath) {
      super(message);
      this.langpath = this.langpath + langpath;
   }

   public MinecraftLauncherException(String message, String langpath, Object replace) {
      super(message);
      this.langpath = this.langpath + langpath;
      this.replace = replace;
   }

   public MinecraftLauncherException(String message, String langpath, Throwable e) {
      super(message, e);
      this.langpath = this.langpath + langpath;
   }

   public MinecraftLauncherException(String message, String langpath, Object replace, Throwable e) {
      super(message, e);
      this.langpath = this.langpath + langpath;
      this.replace = replace;
   }

   public String getLangpath() {
      return this.langpath;
   }

   public Object getReplace() {
      return this.replace;
   }
}
