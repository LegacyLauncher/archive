package ru.turikhay.tlauncher.minecraft.crash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class PatternContainerEntry extends CrashEntry {
   private final List patternEntries = new ArrayList();
   private boolean anyPatternMakesCapable;

   public PatternContainerEntry(CrashManager manager, String name) {
      super(manager, name);
   }

   protected final void setAnyPatternMakesCapable(boolean anyPatternMakesCapable) {
      this.anyPatternMakesCapable = anyPatternMakesCapable;
   }

   protected final void addPattern(PatternEntry entry) {
      this.patternEntries.add(entry);
   }

   protected final PatternEntry addPattern(String name, Pattern pattern) {
      PatternEntry entry = new PatternEntry(this.getManager(), name, pattern);
      this.addPattern(entry);
      return entry;
   }

   protected boolean checkCapability() throws Exception {
      if (!super.checkCapability()) {
         return false;
      } else {
         List capablePatterns = new ArrayList();
         Iterator var2 = this.patternEntries.iterator();

         while(var2.hasNext()) {
            PatternEntry entry = (PatternEntry)var2.next();
            if (entry.checkCapability()) {
               capablePatterns.add(entry);
               if (this.anyPatternMakesCapable) {
                  break;
               }
            }
         }

         return !capablePatterns.isEmpty() && this.checkCapability(capablePatterns);
      }
   }

   protected abstract boolean checkCapability(List var1);

   public ToStringBuilder buildToString() {
      return super.buildToString().append("patterns", this.patternEntries);
   }
}
