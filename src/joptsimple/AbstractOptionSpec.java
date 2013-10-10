package joptsimple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

abstract class AbstractOptionSpec implements OptionSpec {
   private final List options;
   private final String description;

   protected AbstractOptionSpec(String option) {
      this(Collections.singletonList(option), "");
   }

   protected AbstractOptionSpec(Collection options, String description) {
      this.options = new ArrayList();
      this.arrangeOptions(options);
      this.description = description;
   }

   public final Collection options() {
      return Collections.unmodifiableCollection(this.options);
   }

   public final List values(OptionSet detectedOptions) {
      return detectedOptions.valuesOf((OptionSpec)this);
   }

   public final Object value(OptionSet detectedOptions) {
      return detectedOptions.valueOf((OptionSpec)this);
   }

   abstract List defaultValues();

   String description() {
      return this.description;
   }

   protected abstract Object convert(String var1);

   abstract void handleOption(OptionParser var1, ArgumentList var2, OptionSet var3, String var4);

   abstract boolean acceptsArguments();

   abstract boolean requiresArgument();

   abstract void accept(OptionSpecVisitor var1);

   private void arrangeOptions(Collection unarranged) {
      if (unarranged.size() == 1) {
         this.options.addAll(unarranged);
      } else {
         List shortOptions = new ArrayList();
         List longOptions = new ArrayList();
         Iterator i$ = unarranged.iterator();

         while(i$.hasNext()) {
            String each = (String)i$.next();
            if (each.length() == 1) {
               shortOptions.add(each);
            } else {
               longOptions.add(each);
            }
         }

         Collections.sort(shortOptions);
         Collections.sort(longOptions);
         this.options.addAll(shortOptions);
         this.options.addAll(longOptions);
      }
   }

   public boolean equals(Object that) {
      if (!(that instanceof AbstractOptionSpec)) {
         return false;
      } else {
         AbstractOptionSpec other = (AbstractOptionSpec)that;
         return this.options.equals(other.options);
      }
   }

   public int hashCode() {
      return this.options.hashCode();
   }

   public String toString() {
      return this.options.toString();
   }
}
