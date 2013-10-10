package joptsimple;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

class NoArgumentOptionSpec extends AbstractOptionSpec {
   NoArgumentOptionSpec(String option) {
      this(Collections.singletonList(option), "");
   }

   NoArgumentOptionSpec(Collection options, String description) {
      super(options, description);
   }

   void handleOption(OptionParser parser, ArgumentList arguments, OptionSet detectedOptions, String detectedArgument) {
      detectedOptions.add(this);
   }

   boolean acceptsArguments() {
      return false;
   }

   boolean requiresArgument() {
      return false;
   }

   void accept(OptionSpecVisitor visitor) {
      visitor.visit(this);
   }

   protected Void convert(String argument) {
      return null;
   }

   List defaultValues() {
      return Collections.emptyList();
   }
}
