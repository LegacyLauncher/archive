package joptsimple;

import java.util.Collections;

class IllegalOptionClusterException extends OptionException {
   private static final long serialVersionUID = -1L;

   IllegalOptionClusterException(String option) {
      super(Collections.singletonList(option));
   }

   public String getMessage() {
      return "Option cluster containing " + this.singleOptionMessage() + " is illegal";
   }
}
