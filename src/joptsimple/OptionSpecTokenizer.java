package joptsimple;

import java.util.NoSuchElementException;

class OptionSpecTokenizer {
   private static final char POSIXLY_CORRECT_MARKER = '+';
   private String specification;
   private int index;

   OptionSpecTokenizer(String specification) {
      if (specification == null) {
         throw new NullPointerException("null option specification");
      } else {
         this.specification = specification;
      }
   }

   boolean hasMore() {
      return this.index < this.specification.length();
   }

   AbstractOptionSpec next() {
      if (!this.hasMore()) {
         throw new NoSuchElementException();
      } else {
         String optionCandidate = String.valueOf(this.specification.charAt(this.index++));
         if ("W".equals(optionCandidate)) {
            AbstractOptionSpec spec = this.handleReservedForExtensionsToken();
            if (spec != null) {
               return spec;
            }
         }

         ParserRules.ensureLegalOption(optionCandidate);
         Object spec;
         if (!this.hasMore()) {
            spec = new NoArgumentOptionSpec(optionCandidate);
         } else if (this.specification.charAt(this.index) == ':') {
            spec = this.handleArgumentAcceptingOption(optionCandidate);
         } else {
            spec = new NoArgumentOptionSpec(optionCandidate);
         }

         return (AbstractOptionSpec)spec;
      }
   }

   void configure(OptionParser parser) {
      this.adjustForPosixlyCorrect(parser);

      while(this.hasMore()) {
         parser.recognize(this.next());
      }

   }

   private void adjustForPosixlyCorrect(OptionParser parser) {
      if ('+' == this.specification.charAt(0)) {
         parser.posixlyCorrect(true);
         this.specification = this.specification.substring(1);
      }

   }

   private AbstractOptionSpec handleReservedForExtensionsToken() {
      if (!this.hasMore()) {
         return new NoArgumentOptionSpec("W");
      } else if (this.specification.charAt(this.index) == ';') {
         ++this.index;
         return new AlternativeLongOptionSpec();
      } else {
         return null;
      }
   }

   private AbstractOptionSpec handleArgumentAcceptingOption(String candidate) {
      ++this.index;
      if (this.hasMore() && this.specification.charAt(this.index) == ':') {
         ++this.index;
         return new OptionalArgumentOptionSpec(candidate);
      } else {
         return new RequiredArgumentOptionSpec(candidate);
      }
   }
}
