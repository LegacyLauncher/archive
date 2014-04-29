package ru.turikhay.tlauncher.minecraft.crash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Crash {
   private String file;
   private List signatures = new ArrayList();

   void addSignature(CrashSignatureContainer.CrashSignature sign) {
      this.signatures.add(sign);
   }

   void removeSignature(CrashSignatureContainer.CrashSignature sign) {
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

   public boolean hasSignature(CrashSignatureContainer.CrashSignature s) {
      return this.signatures.contains(s);
   }

   public boolean isRecognized() {
      return !this.signatures.isEmpty();
   }
}
