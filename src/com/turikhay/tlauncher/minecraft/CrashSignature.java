package com.turikhay.tlauncher.minecraft;

import java.util.regex.Pattern;

public class CrashSignature {
   public final Pattern pattern;
   public final int exitcode;
   public final String name;
   public final String description;
   public final String path;

   CrashSignature(int exitcode, String pattern, String name, String description, String path) {
      this.pattern = pattern != null ? Pattern.compile(pattern) : null;
      this.exitcode = exitcode;
      this.name = name;
      this.description = description;
      this.path = path;
   }

   public boolean match(String line) {
      return this.pattern == null ? false : this.pattern.matcher(line).matches();
   }

   public boolean match(int exit) {
      if (this.exitcode == 0) {
         return false;
      } else {
         return exit == this.exitcode;
      }
   }
}
