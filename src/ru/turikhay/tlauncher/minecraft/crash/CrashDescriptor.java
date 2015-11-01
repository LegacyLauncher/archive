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
   private static final String[] phrases = new String[]{"Мы катапультировались. Приятного полёта.", "Сейчас лучше выпить чаю. С бубликами.", "Шаманские бубны. Большой ассортимент. Звоните!", "Тут только звуковая отвёртка поможет. Или большая кувалда."};

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
               String phrase;
               label85:
               while(scanner.hasNextLine()) {
                  phrase = scanner.nextLine();
                  if (filePattern.matcher(phrase).matches()) {
                     Matcher var12 = filePattern.matcher(phrase);
                     if (var12.matches() && var12.groupCount() == 1) {
                        crash.setFile(var12.group(1));
                        this.log("Found crash report file:", crash.getFile());
                     }
                  } else {
                     Iterator var9 = container.getSignatures().iterator();

                     while(true) {
                        CrashSignatureContainer.CrashSignature line;
                        do {
                           do {
                              do {
                                 if (!var9.hasNext()) {
                                    continue label85;
                                 }

                                 line = (CrashSignatureContainer.CrashSignature)var9.next();
                              } while(line.hasVersion() && !line.getVersion().matcher(version).matches());
                           } while(line.getExitCode() != 0 && line.getExitCode() != exitCode);
                        } while(line.hasPattern() && !line.getPattern().matcher(phrase).matches());

                        if (line.isFake()) {
                           this.log("Minecraft closed with an illegal exit code not due to error. Scanning has been cancelled");
                           this.log("Fake signature:", line.getName());
                           scanner.close();
                           return null;
                        }

                        if (!crash.hasSignature(line)) {
                           this.log("Signature \"" + line.getName() + "\" matches!");
                           crash.addSignature(line);
                        }
                     }
                  }
               }

               scanner.close();
               if (!crash.contains("PermGen error") && !crash.contains("OutOfMemory error") && !crash.contains("Too heavy heap")) {
                  phrase = (String)U.getRandom(phrases);
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

               return crash;
            }
         }
      }
   }

   void log(Object... w) {
      this.launcher.log(w);
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

   }
}
