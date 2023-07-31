package joptsimple;

import java.util.Collection;

class OptionMissingRequiredArgumentException extends OptionException {
   private static final long serialVersionUID = -1L;

   OptionMissingRequiredArgumentException(Collection options) {
      super(options);
   }

   public String getMessage() {
      return "Option " + this.multipleOptionMessage() + " requires an argument";
   }
}
