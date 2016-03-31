package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public class CrashDescriptor {
   private static CrashSignatureContainer container;
   private final MinecraftLauncher launcher;
   private static final String[] phrases;

   public CrashDescriptor(MinecraftLauncher launcher) {
      if (launcher == null) {
         throw new NullPointerException();
      } else {
         this.launcher = launcher;
      }
   }

   public Crash scan() {
      Crash crash = new Crash();
      String output = this.launcher.getOutput();
      if (output == null) {
         this.log("Could not get console output.");
         return crash;
      } else {
         Pattern filePattern = container.getPattern("crash");
         Pattern reportPattern0 = container.getPattern("report0");
         Pattern reportPattern1 = container.getPattern("report1");
         String version = this.launcher.getVersion();
         Scanner scanner = null;

         try {
            scanner = new Scanner(output);

            label210:
            while(scanner.hasNextLine()) {
               String line = scanner.nextLine();
               Matcher matcher;
               if ((matcher = filePattern.matcher(line)).matches() && matcher.matches() && matcher.groupCount() == 1) {
                  crash.setFile(matcher.group(1));
                  this.log("Found crash report file:", crash.getFile());
               } else {
                  if (reportPattern0.matcher(line).matches()) {
                     line = scanner.nextLine();
                     matcher = reportPattern1.matcher(line);
                     if (matcher.matches() && matcher.groupCount() == 1) {
                        crash.setNativeReport(matcher.group(1));
                        continue;
                     }
                  }

                  Iterator var10 = container.getSignatures().iterator();

                  while(true) {
                     CrashSignatureContainer.CrashSignature signature;
                     do {
                        do {
                           do {
                              if (!var10.hasNext()) {
                                 continue label210;
                              }

                              signature = (CrashSignatureContainer.CrashSignature)var10.next();
                           } while(signature.hasVersion() && !signature.getVersion().matcher(version).matches());
                        } while(signature.hasPattern() && !signature.getPattern().matcher(line).matches());
                     } while(signature.getExitCode() != 0 && signature.getExitCode() != this.launcher.getExitCode());

                     if (signature.isFake()) {
                        this.log("It's a trap, not crash:", signature.getName());
                        Object var12 = null;
                        return (Crash)var12;
                     }

                     if (!crash.hasSignature(signature)) {
                        this.log("Signature matches:", signature.getName());
                        crash.addSignature(signature);
                     }
                  }
               }
            }
         } finally {
            U.close(scanner);
         }

         if (!crash.contains("PermGen error") && !crash.contains("OutOfMemory error") && !crash.contains("Too heavy heap")) {
            String phrase = (String)U.getRandom(phrases);
            String[] var11;
            int var10 = (var11 = StringUtils.split(phrase, '\n')).length;

            for(int var14 = 0; var14 < var10; ++var14) {
               String var13 = var11[var14];
               U.log("//", var13);
            }
         } else {
            U.log("– И это всё потому что у кого-то слишком узкие двери...");
            U.log("– Нет! Всё потому что кто-то слишком много ест!");
         }

         this.readFile(crash.getFile());
         this.readFile(crash.getNativeReport());
         if (OS.WINDOWS.isCurrent()) {
            OS.Windows.printDxDiag();
         }

         return crash;
      }
   }

   private void readFile(String path) {
      if (path != null) {
         this.log("++++++++++++++++++++++++++++++++++");

         try {
            File file = new File(path);
            if (!file.isFile()) {
               this.log("File doesn't exist:", path);
               return;
            }

            this.log("++++++++++++++++++++++++++++++++++");
            this.log("Reading file:", file);
            Scanner scanner = null;

            try {
               scanner = new Scanner(file);

               while(scanner.hasNextLine()) {
                  this.plog("+", scanner.nextLine());
               }
            } catch (Exception var13) {
               this.log("Could not read file:", file, var13);
            } finally {
               U.close(scanner);
            }
         } finally {
            this.log("++++++++++++++++++++++++++++++++++");
         }

      }
   }

   void log(Object... w) {
      this.launcher.log(w);
   }

   void plog(Object... w) {
      U.plog(w);
      this.launcher.plog(w);
   }

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

      phrases = new String[]{"Мы катапультировались. Приятного полёта.", "Сейчас лучше выпить чаю. С бубликами.", "Шаманские бубны. Большой ассортимент. Звоните!", "Тут только звуковая отвёртка поможет. Или большая кувалда."};
   }
}
