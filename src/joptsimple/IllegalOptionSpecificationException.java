package joptsimple;

import java.util.Collections;

class IllegalOptionSpecificationException extends OptionException {
   private static final long serialVersionUID = -1L;

   IllegalOptionSpecificationException(String option) {
      super(Collections.singletonList(option));
   }

   public String getMessage() {
      return this.singleOptionMessage() + " is not a legal option character";
   }
}
