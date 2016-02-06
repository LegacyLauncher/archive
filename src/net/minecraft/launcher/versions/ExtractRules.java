package net.minecraft.launcher.versions;

import java.util.Iterator;
import java.util.List;

public class ExtractRules {
   private List exclude;

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
