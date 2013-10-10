package joptsimple;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import joptsimple.internal.Classes;
import joptsimple.internal.ColumnarData;
import joptsimple.internal.Strings;

class HelpFormatter implements OptionSpecVisitor {
   private final ColumnarData grid = new ColumnarData(new String[]{"Option", "Description"});

   String format(Map options) {
      if (options.isEmpty()) {
         return "No options specified";
      } else {
         this.grid.clear();
         Comparator comparator = new Comparator() {
            public int compare(AbstractOptionSpec first, AbstractOptionSpec second) {
               return ((String)first.options().iterator().next()).compareTo((String)second.options().iterator().next());
            }
         };
         Set sorted = new TreeSet(comparator);
         sorted.addAll(options.values());
         Iterator i$ = sorted.iterator();

         while(i$.hasNext()) {
            AbstractOptionSpec each = (AbstractOptionSpec)i$.next();
            each.accept(this);
         }

         return this.grid.format();
      }
   }

   void addHelpLineFor(AbstractOptionSpec spec, String additionalInfo) {
      this.grid.addRow(this.createOptionDisplay(spec) + additionalInfo, this.createDescriptionDisplay(spec));
   }

   public void visit(NoArgumentOptionSpec spec) {
      this.addHelpLineFor(spec, "");
   }

   public void visit(RequiredArgumentOptionSpec spec) {
      this.visit(spec, '<', '>');
   }

   public void visit(OptionalArgumentOptionSpec spec) {
      this.visit(spec, '[', ']');
   }

   public void visit(AlternativeLongOptionSpec spec) {
      this.addHelpLineFor(spec, ' ' + Strings.surround(spec.argumentDescription(), '<', '>'));
   }

   private void visit(ArgumentAcceptingOptionSpec spec, char begin, char end) {
      String argDescription = spec.argumentDescription();
      String typeIndicator = typeIndicator(spec);
      StringBuilder collector = new StringBuilder();
      if (typeIndicator.length() > 0) {
         collector.append(typeIndicator);
         if (argDescription.length() > 0) {
            collector.append(": ").append(argDescription);
         }
      } else if (argDescription.length() > 0) {
         collector.append(argDescription);
      }

      String helpLine = collector.length() == 0 ? "" : ' ' + Strings.surround(collector.toString(), begin, end);
      this.addHelpLineFor(spec, helpLine);
   }

   private String createOptionDisplay(AbstractOptionSpec spec) {
      StringBuilder buffer = new StringBuilder();
      Iterator iter = spec.options().iterator();

      while(iter.hasNext()) {
         String option = (String)iter.next();
         buffer.append(option.length() > 1 ? "--" : ParserRules.HYPHEN);
         buffer.append(option);
         if (iter.hasNext()) {
            buffer.append(", ");
         }
      }

      return buffer.toString();
   }

   private String createDescriptionDisplay(AbstractOptionSpec spec) {
      List defaultValues = spec.defaultValues();
      if (defaultValues.isEmpty()) {
         return spec.description();
      } else {
         String defaultValuesDisplay = this.createDefaultValuesDisplay(defaultValues);
         return spec.description() + ' ' + Strings.surround("default: " + defaultValuesDisplay, '(', ')');
      }
   }

   private String createDefaultValuesDisplay(List defaultValues) {
      return defaultValues.size() == 1 ? defaultValues.get(0).toString() : defaultValues.toString();
   }

   private static String typeIndicator(ArgumentAcceptingOptionSpec spec) {
      String indicator = spec.typeIndicator();
      return indicator != null && !String.class.getName().equals(indicator) ? Classes.shortNameOf(indicator) : "";
   }
}
