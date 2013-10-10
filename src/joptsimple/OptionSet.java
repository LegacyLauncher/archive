package joptsimple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import joptsimple.internal.Objects;

public class OptionSet {
   private final Map detectedOptions = new HashMap();
   private final Map optionsToArguments = new IdentityHashMap();
   private final List nonOptionArguments = new ArrayList();
   private final Map defaultValues;

   OptionSet(Map defaults) {
      this.defaultValues = new HashMap(defaults);
   }

   public boolean has(String option) {
      return this.detectedOptions.containsKey(option);
   }

   public boolean has(OptionSpec option) {
      return this.optionsToArguments.containsKey(option);
   }

   public boolean hasArgument(String option) {
      AbstractOptionSpec spec = (AbstractOptionSpec)this.detectedOptions.get(option);
      return spec != null && this.hasArgument((OptionSpec)spec);
   }

   public boolean hasArgument(OptionSpec option) {
      Objects.ensureNotNull(option);
      List values = (List)this.optionsToArguments.get(option);
      return values != null && !values.isEmpty();
   }

   public Object valueOf(String option) {
      Objects.ensureNotNull(option);
      AbstractOptionSpec spec = (AbstractOptionSpec)this.detectedOptions.get(option);
      if (spec == null) {
         List defaults = this.defaultValuesFor(option);
         return defaults.isEmpty() ? null : defaults.get(0);
      } else {
         return this.valueOf((OptionSpec)spec);
      }
   }

   public Object valueOf(OptionSpec option) {
      Objects.ensureNotNull(option);
      List values = this.valuesOf(option);
      switch(values.size()) {
      case 0:
         return null;
      case 1:
         return values.get(0);
      default:
         throw new MultipleArgumentsForOptionException(option.options());
      }
   }

   public List valuesOf(String option) {
      Objects.ensureNotNull(option);
      AbstractOptionSpec spec = (AbstractOptionSpec)this.detectedOptions.get(option);
      return spec == null ? this.defaultValuesFor(option) : this.valuesOf((OptionSpec)spec);
   }

   public List valuesOf(OptionSpec option) {
      Objects.ensureNotNull(option);
      List values = (List)this.optionsToArguments.get(option);
      if (values != null && !values.isEmpty()) {
         AbstractOptionSpec spec = (AbstractOptionSpec)option;
         List convertedValues = new ArrayList();
         Iterator i$ = values.iterator();

         while(i$.hasNext()) {
            String each = (String)i$.next();
            convertedValues.add(spec.convert(each));
         }

         return Collections.unmodifiableList(convertedValues);
      } else {
         return this.defaultValueFor(option);
      }
   }

   public List nonOptionArguments() {
      return Collections.unmodifiableList(this.nonOptionArguments);
   }

   void add(AbstractOptionSpec option) {
      this.addWithArgument(option, (String)null);
   }

   void addWithArgument(AbstractOptionSpec option, String argument) {
      Iterator i$ = option.options().iterator();

      while(i$.hasNext()) {
         String each = (String)i$.next();
         this.detectedOptions.put(each, option);
      }

      List optionArguments = (List)this.optionsToArguments.get(option);
      if (optionArguments == null) {
         optionArguments = new ArrayList();
         this.optionsToArguments.put(option, optionArguments);
      }

      if (argument != null) {
         ((List)optionArguments).add(argument);
      }

   }

   void addNonOptionArgument(String argument) {
      this.nonOptionArguments.add(argument);
   }

   public boolean equals(Object that) {
      if (this == that) {
         return true;
      } else if (that != null && this.getClass().equals(that.getClass())) {
         OptionSet other = (OptionSet)that;
         Map thisOptionsToArguments = new HashMap(this.optionsToArguments);
         Map otherOptionsToArguments = new HashMap(other.optionsToArguments);
         return this.detectedOptions.equals(other.detectedOptions) && thisOptionsToArguments.equals(otherOptionsToArguments) && this.nonOptionArguments.equals(other.nonOptionArguments());
      } else {
         return false;
      }
   }

   public int hashCode() {
      Map thisOptionsToArguments = new HashMap(this.optionsToArguments);
      return this.detectedOptions.hashCode() ^ thisOptionsToArguments.hashCode() ^ this.nonOptionArguments.hashCode();
   }

   private List defaultValuesFor(String option) {
      if (this.defaultValues.containsKey(option)) {
         List defaults = (List)this.defaultValues.get(option);
         return defaults;
      } else {
         return Collections.emptyList();
      }
   }

   private List defaultValueFor(OptionSpec option) {
      return this.defaultValuesFor((String)option.options().iterator().next());
   }
}
