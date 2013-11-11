package com.turikhay.tlauncher.minecraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
      List r = new ArrayList();
      Iterator var3 = this.signatures.iterator();

      while(var3.hasNext()) {
         CrashSignature sign = (CrashSignature)var3.next();
         r.add(sign);
      }

      return r;
   }

   public boolean hasSignature(CrashSignature s) {
      return this.signatures.contains(s);
   }

   public boolean isRecognized() {
      return !this.signatures.isEmpty();
   }
}
