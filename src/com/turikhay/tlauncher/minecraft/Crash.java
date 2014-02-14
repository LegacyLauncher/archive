package com.turikhay.tlauncher.minecraft;

import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.util.U;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.launcher.OperatingSystem;

public class Crash {
   private String file;
   private List signatures = new ArrayList();

   void addSignature(CrashSignature sign) {
      this.signatures.add(sign);
   }

   void removeSignature(CrashSignature sign) {
      this.signatures.remove(sign);
   }

   void setFile(String path) {
      this.file = path;
   }

   public String getFile() {
      return this.file;
   }

   public List getSignatures() {
      return Collections.unmodifiableList(this.signatures);
   }

   public boolean hasSignature(CrashSignature s) {
      return this.signatures.contains(s);
   }

   public boolean isRecognized() {
      return !this.signatures.isEmpty();
   }

   public static void handle(Crash crash) {
      String p = "crash.";
      String title = Localizable.get(p + "title");
      String report = crash.getFile();
      if (!crash.isRecognized()) {
         Alert.showError(title, Localizable.get(p + "unknown"), (Throwable)null);
      } else {
         Iterator var5 = crash.getSignatures().iterator();

         while(var5.hasNext()) {
            CrashSignature sign = (CrashSignature)var5.next();
            String path = sign.path;
            String message = Localizable.get(p + path);
            String url = Localizable.get(p + path + ".url");
            URI uri = U.makeURI(url);
            if (uri != null) {
               if (Alert.showQuestion(title, message, report, false)) {
                  OperatingSystem.openLink(uri);
               }
            } else {
               Alert.showMessage(title, message, report);
            }
         }
      }

      if (report != null) {
         if (Alert.showQuestion(p + "store", false)) {
            U.log("Removing crash report...");
            File file = new File(report);
            if (!file.exists()) {
               U.log("File is already removed. LOL.");
            } else {
               try {
                  if (!file.delete()) {
                     throw new Exception("file.delete() returned false");
                  }
               } catch (Exception var10) {
                  U.log("Can't delete crash report file. Okay.");
                  Alert.showAsyncMessage(p + "store.failed", var10);
                  return;
               }

               U.log("Yay, crash report file doesn't exist by now.");
            }

            Alert.showAsyncMessage(p + "store.success");
         }

      }
   }
}
