package net.minecraft.launcher.versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ExtractRules {
   private List exclude = new ArrayList();

   public ExtractRules() {
   }

   public ExtractRules(String[] exclude) {
      if (exclude != null) {
         Collections.addAll(this.exclude, exclude);
      }

   }

   public ExtractRules(ExtractRules rules) {
      Iterator var3 = rules.exclude.iterator();

      while(var3.hasNext()) {
         String exclude = (String)var3.next();
         this.exclude.add(exclude);
      }

   }

   public List getExcludes() {
      return this.exclude;
   }

   public boolean shouldExtract(String path) {
      if (this.exclude == null) {
         return true;
      } else {
         Iterator var3 = this.exclude.iterator();

         String rule;
         do {
            if (!var3.hasNext()) {
               return true;
            }

            rule = (String)var3.next();
         } while(!path.startsWith(rule));

         return false;
      }
   }
}
