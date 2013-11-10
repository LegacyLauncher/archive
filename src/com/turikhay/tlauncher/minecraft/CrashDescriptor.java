package com.turikhay.tlauncher.minecraft;

import com.turikhay.tlauncher.util.FileUtil;
import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrashDescriptor {
   final String prefix = "[CrashDescriptor]";
   static final Pattern crash_pattern = Pattern.compile("^.*[\\#\\@\\!\\@\\#][ ]Game[ ]crashed!.+[\\#\\@\\!\\@\\#][ ](.+)$");
   static final CrashSignature[] signatures = initSigns();
   private final MinecraftLauncher context;

   CrashDescriptor(MinecraftLauncher l) {
      this.context = l;
   }

   public Crash scan(int exit) {
      Crash crash = new Crash();
      String path = null;
      String[] lines = this.context.con.getOutput().split("\n");
      String[] var8 = lines;
      int var7 = lines.length;

      for(int var6 = 0; var6 < var7; ++var6) {
         String line = var8[var6];
         Matcher mt = crash_pattern.matcher(line);
         if (mt.matches()) {
            path = mt.group(1);
            crash.setFile(path);
         }
      }

      if (path == null) {
         this.log("Cannot find crash report file from log.");
         return crash;
      } else {
         File file = new File(path);
         if (!file.isFile()) {
            this.log("Found crash report path is not a file.");
            return crash;
         } else {
            String f_out;
            try {
               f_out = FileUtil.readFile(file);
            } catch (Exception var16) {
               this.log("Cannot read crash report from file!", var16);
               return crash;
            }

            String[] f_lines = f_out.split("\n");
            this.log("Crash report file accessed!");
            String[] var11 = f_lines;
            int var10 = f_lines.length;

            for(int var21 = 0; var21 < var10; ++var21) {
               String line = var11[var21];
               CrashSignature[] var15;
               int var14 = (var15 = signatures).length;

               for(int var13 = 0; var13 < var14; ++var13) {
                  CrashSignature sign = var15[var13];
                  if (sign.match(line)) {
                     this.log("Signature \"" + sign.name + "\" matches!");
                     crash.addSignature(sign);
                  }
               }
            }

            return crash;
         }
      }
   }

   public static boolean parseExit(int code) {
      return U.interval(0, 1, code);
   }

   private static CrashSignature[] initSigns() {
      CrashSignature[] r = new CrashSignature[]{new CrashSignature(0, "[\\s]*org\\.lwjgl\\.LWJGLException\\: Pixel format not accelerated", "Old graphics driver", "Installed graphics griver does not support OpenGL properly.", "opengl"), new CrashSignature(0, "^Caused by: java.lang.ClassNotFoundException: .+", "Probably modified JAR", "Running JAR file is corrupted by incorrect mod installing. Please, reinstall this version and then try to reinstall mods or find proper version.", "noclass")};
      return r;
   }

   void log(Object... w) {
      if (this.context.con != null) {
         this.context.con.log("[CrashDescriptor]", w);
      }

      U.log("[CrashDescriptor]", w);
   }
}
