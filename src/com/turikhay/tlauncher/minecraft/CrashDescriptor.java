package com.turikhay.tlauncher.minecraft;

import com.turikhay.util.U;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrashDescriptor {
   final String prefix = "[CrashDescriptor]";
   static final String forge_prefix = "^(?:[0-9-]+ [0-9:]+ \\[[\\w]+\\]\\ {0,1}\\[{0,1}[\\w]*\\]{0,1}\\ {0,1}){0,1}";
   static final Pattern crash_pattern = Pattern.compile("^.*[\\#\\@\\!\\@\\#][ ]Game[ ]crashed!.+[\\#\\@\\!\\@\\#][ ](.+)$");
   static final Pattern start_crash_report_pattern = Pattern.compile("^(?:[0-9-]+ [0-9:]+ \\[[\\w]+\\]\\ {0,1}\\[{0,1}[\\w]*\\]{0,1}\\ {0,1}){0,1}---- Minecraft Crash Report ----");
   static final CrashSignature[] signatures = initSigns();
   private final MinecraftLauncher context;

   CrashDescriptor(MinecraftLauncher l) {
      this.context = l;
   }

   public Crash scan(int exit) {
      Crash crash = new Crash();
      String[] lines = this.context.con.getOutput().split("\n");

      for(int i = lines.length - 1; i > -1; --i) {
         String line = lines[i];
         Matcher start_crash = start_crash_report_pattern.matcher(line);
         if (start_crash.matches()) {
            this.log("Will not search further - start of crash report exceed.");
            break;
         }

         Matcher mt = crash_pattern.matcher(line);
         if (mt.matches()) {
            crash.setFile(mt.group(1));
         } else {
            CrashSignature[] var11;
            int var10 = (var11 = signatures).length;

            for(int var9 = 0; var9 < var10; ++var9) {
               CrashSignature sign = var11[var9];
               if (sign.match(line)) {
                  if (sign instanceof FakeCrashSignature) {
                     this.log("Minecraft closed with an illegal exit code not due to error. Cancelling.");
                     this.log("Catched by signature:", sign.name);
                     return null;
                  }

                  if (sign.exitcode == 0 || sign.exitcode == exit) {
                     this.log("Signature \"" + sign.name + "\" matches!");
                     if (!crash.hasSignature(sign)) {
                        crash.addSignature(sign);
                     }
                  }
               }
            }
         }
      }

      return crash;
   }

   public static boolean parseExit(int code) {
      return code == 0;
   }

   private static CrashSignature[] initSigns() {
      CrashSignature[] r = new CrashSignature[]{new CrashSignature(0, "^(?:[0-9-]+ [0-9:]+ \\[[\\w]+\\]\\ {0,1}\\[{0,1}[\\w]*\\]{0,1}\\ {0,1}){0,1}[\\s]*org\\.lwjgl\\.LWJGLException\\: Pixel format not accelerated", "Old graphics driver", "opengl"), new CrashSignature(0, "^(?:[0-9-]+ [0-9:]+ \\[[\\w]+\\]\\ {0,1}\\[{0,1}[\\w]*\\]{0,1}\\ {0,1}){0,1}java\\.lang\\.(?:Error|NoClass|Exception|Error|Throwable|Illegal){1}.+", "Probably modified JAR", "invalid-modify"), new CrashSignature(1, "Exception in thread \"main\" java.lang.SecurityException: SHA1 digest error for .+", "Undeleted META-INF", "meta-inf"), new FakeCrashSignature(1, "^(?:[0-9-]+ [0-9:]+ \\[[\\w]+\\]\\ {0,1}\\[{0,1}[\\w]*\\]{0,1}\\ {0,1}){0,1}Someone is closing me!", "ALC cleanup bug"), new CrashSignature(1, "^Error: Could not find or load main class .+", "Missing main class", "missing-main")};
      return r;
   }

   void log(Object... w) {
      if (this.context.con != null) {
         this.context.con.log("[CrashDescriptor]", w);
      }

      U.log("[CrashDescriptor]", w);
   }
}
