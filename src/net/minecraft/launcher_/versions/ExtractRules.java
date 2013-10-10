package net.minecraft.launcher_.versions;

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

   public List getExcludes() {
      return this.exclude;
   }

   public boolean shouldExtract(String path) {
      if (this.exclude != null) {
         Iterator var3 = this.exclude.iterator();

         while(var3.hasNext()) {
            String rule = (String)var3.next();
            if (path.startsWith(rule)) {
               return false;
            }
         }
      }

      return true;
   }
}
