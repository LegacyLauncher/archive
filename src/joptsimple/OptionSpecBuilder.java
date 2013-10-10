package joptsimple;

import java.util.Collection;

public class OptionSpecBuilder extends NoArgumentOptionSpec {
   private final OptionParser parser;

   OptionSpecBuilder(OptionParser parser, Collection options, String description) {
      super(options, description);
      this.parser = parser;
      parser.recognize(this);
   }

   public ArgumentAcceptingOptionSpec withRequiredArg() {
      ArgumentAcceptingOptionSpec newSpec = new RequiredArgumentOptionSpec(this.options(), this.description());
      this.parser.recognize(newSpec);
      return newSpec;
   }

   public ArgumentAcceptingOptionSpec withOptionalArg() {
      ArgumentAcceptingOptionSpec newSpec = new OptionalArgumentOptionSpec(this.options(), this.description());
      this.parser.recognize(newSpec);
      return newSpec;
   }
}
