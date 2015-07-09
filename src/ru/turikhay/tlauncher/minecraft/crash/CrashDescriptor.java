package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

public class CrashDescriptor {
   private static CrashSignatureContainer container;
   public static final int goodExitCode = 0;
   private final MinecraftLauncher launcher;
   private static final String[] phrases = new String[]{"Мы катапультировались. Приятного полёта.", "Сейчас лучше выпить чаю. С бубликами.", "Шаманские бубны. Большой ассортимент. Звоните!", "Тут только звуковая отвётка поможет. Или большая кувалда."};

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
         String output = this.launcher.getOutput();
         if (output == null) {
            this.log("Could not get console output.");
            return crash;
         } else {
            Scanner scanner = new Scanner(output);
            Pattern filePattern = container.getPattern("crash");
            String version = this.launcher.getVersion();

            while(true) {
               String line;
               label85:
               while(scanner.hasNextLine()) {
                  line = scanner.nextLine();
                  if (filePattern.matcher(line).matches()) {
                     Matcher fileMatcher = filePattern.matcher(line);
                     if (fileMatcher.matches() && fileMatcher.groupCount() == 1) {
                        crash.setFile(fileMatcher.group(1));
                        this.log("Found crash report file:", crash.getFile());
                     }
                  } else {
                     Iterator var9 = container.getSignatures().iterator();

                     while(true) {
                        CrashSignatureContainer.CrashSignature signature;
                        do {
                           do {
                              do {
                                 if (!var9.hasNext()) {
                                    continue label85;
                                 }

                                 signature = (CrashSignatureContainer.CrashSignature)var9.next();
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
               if (!crash.contains("PermGen error") && !crash.contains("OutOfMemory error") && !crash.contains("Too heavy heap")) {
                  line = (String)U.getRandom(phrases);
                  String[] var11;
                  int var10 = (var11 = StringUtils.split(line, '\n')).length;

                  for(int var14 = 0; var14 < var10; ++var14) {
                     String line = var11[var14];
                     U.log("//", line);
                  }
               } else {
                  U.log("– И это всё потому что у кого-то слишком узкие двери...");
                  U.log("– Нет! Всё потому что кто-то слишком много ест!");
               }

               return crash;
            }
         }
      }
   }

   void log(Object... w) {
      this.launcher.log(w);
   }
}
