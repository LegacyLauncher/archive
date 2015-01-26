package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

public class CrashDescriptor {
   private static CrashSignatureContainer container;
   public static final int goodExitCode = 0;
   private static final String loggerPrefix = "[Crash]";
   private final MinecraftLauncher launcher;

   static {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(CrashSignatureContainer.class, new CrashSignatureContainer.CrashSignatureContainerDeserializer());
      Gson gson = builder.create();

      try {
         container = (CrashSignatureContainer)gson.fromJson(FileUtil.getResource(CrashDescriptor.class.getResource("signatures.json")), CrashSignatureContainer.class);
      } catch (Exception var3) {
         U.log("Cannot parse crash signatures!", var3);
         container = new CrashSignatureContainer();
      }

   }

   public CrashDescriptor(MinecraftLauncher launcher) {
      if (launcher == null) {
         throw new NullPointerException();
      } else {
         this.launcher = launcher;
      }
   }

   public Crash scan() {
      int exitCode = this.launcher.getExitCode();
      if (exitCode == 0) {
         return null;
      } else {
         Crash crash = new Crash();
         Pattern filePattern = container.getPattern("crash");
         String version = this.launcher.getVersion();
         Scanner scanner = new Scanner(this.launcher.getStream().getOutput());

         while(true) {
            label67:
            while(scanner.hasNextLine()) {
               String line = scanner.nextLine();
               if (filePattern.matcher(line).matches()) {
                  Matcher fileMatcher = filePattern.matcher(line);
                  if (fileMatcher.matches() && fileMatcher.groupCount() == 1) {
                     crash.setFile(fileMatcher.group(1));
                     this.log("Found crash report file:", crash.getFile());
                  }
               } else {
                  Iterator var8 = container.getSignatures().iterator();

                  while(true) {
                     CrashSignatureContainer.CrashSignature signature;
                     do {
                        do {
                           do {
                              if (!var8.hasNext()) {
                                 continue label67;
                              }

                              signature = (CrashSignatureContainer.CrashSignature)var8.next();
                           } while(signature.hasVersion() && !signature.getVersion().matcher(version).matches());
                        } while(signature.getExitCode() != 0 && signature.getExitCode() != exitCode);
                     } while(signature.hasPattern() && !signature.getPattern().matcher(line).matches());

                     if (signature.isFake()) {
                        this.log("Minecraft closed with an illegal exit code not due to error. Scanning has been cancelled");
                        this.log("Fake signature:", signature.getName());
                        scanner.close();
                        return null;
                     }

                     if (!crash.hasSignature(signature)) {
                        this.log("Signature \"" + signature.getName() + "\" matches!");
                        crash.addSignature(signature);
                     }
                  }
               }
            }

            scanner.close();
            if (crash.isRecognized()) {
               this.log("Crash has been recognized!");
            }

            return crash;
         }
      }
   }

   void log(Object... w) {
      this.launcher.getLogger().log("[Crash]", w);
      U.log("[Crash]", w);
   }
}
