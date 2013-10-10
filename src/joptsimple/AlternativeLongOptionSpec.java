package joptsimple;

import java.util.Collections;

class AlternativeLongOptionSpec extends ArgumentAcceptingOptionSpec {
   AlternativeLongOptionSpec() {
      super(Collections.singletonList("W"), true, "Alternative form of long options");
      this.describedAs("opt=value");
   }

   protected void detectOptionArgument(OptionParser parser, ArgumentList arguments, OptionSet detectedOptions) {
      if (!arguments.hasMore()) {
         throw new OptionMissingRequiredArgumentException(this.options());
      } else {
         arguments.treatNextAsLongOption();
      }
   }

   void accept(OptionSpecVisitor visitor) {
      visitor.visit(this);
   }
}
