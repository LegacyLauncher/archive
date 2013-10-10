package joptsimple;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import joptsimple.internal.AbbreviationMap;
import joptsimple.util.KeyValuePair;

public class OptionParser {
   private final AbbreviationMap recognizedOptions;
   private OptionParserState state;
   private boolean posixlyCorrect;

   public OptionParser() {
      this.recognizedOptions = new AbbreviationMap();
      this.state = OptionParserState.moreOptions(false);
   }

   public OptionParser(String optionSpecification) {
      this();
      (new OptionSpecTokenizer(optionSpecification)).configure(this);
   }

   public OptionSpecBuilder accepts(String option) {
      return this.acceptsAll(Collections.singletonList(option));
   }

   public OptionSpecBuilder accepts(String option, String description) {
      return this.acceptsAll(Collections.singletonList(option), description);
   }

   public OptionSpecBuilder acceptsAll(Collection options) {
      return this.acceptsAll(options, "");
   }

   public OptionSpecBuilder acceptsAll(Collection options, String description) {
      if (options.isEmpty()) {
         throw new IllegalArgumentException("need at least one option");
      } else {
         ParserRules.ensureLegalOptions(options);
         return new OptionSpecBuilder(this, options, description);
      }
   }

   public void posixlyCorrect(boolean setting) {
      this.posixlyCorrect = setting;
      this.state = OptionParserState.moreOptions(setting);
   }

   boolean posixlyCorrect() {
      return this.posixlyCorrect;
   }

   public void recognizeAlternativeLongOptions(boolean recognize) {
      if (recognize) {
         this.recognize(new AlternativeLongOptionSpec());
      } else {
         this.recognizedOptions.remove(String.valueOf("W"));
      }

   }

   void recognize(AbstractOptionSpec spec) {
      this.recognizedOptions.putAll(spec.options(), spec);
   }

   public void printHelpOn(OutputStream sink) throws IOException {
      this.printHelpOn((Writer)(new OutputStreamWriter(sink)));
   }

   public void printHelpOn(Writer sink) throws IOException {
      sink.write((new HelpFormatter()).format(this.recognizedOptions.toJavaUtilMap()));
      sink.flush();
   }

   public OptionSet parse(String... arguments) {
      ArgumentList argumentList = new ArgumentList(arguments);
      OptionSet detected = new OptionSet(this.defaultValues());

      while(argumentList.hasMore()) {
         this.state.handleArgument(this, argumentList, detected);
      }

      this.reset();
      return detected;
   }

   void handleLongOptionToken(String candidate, ArgumentList arguments, OptionSet detected) {
      KeyValuePair optionAndArgument = parseLongOptionWithArgument(candidate);
      if (!this.isRecognized(optionAndArgument.key)) {
         throw OptionException.unrecognizedOption(optionAndArgument.key);
      } else {
         AbstractOptionSpec optionSpec = this.specFor(optionAndArgument.key);
         optionSpec.handleOption(this, arguments, detected, optionAndArgument.value);
      }
   }

   void handleShortOptionToken(String candidate, ArgumentList arguments, OptionSet detected) {
      KeyValuePair optionAndArgument = parseShortOptionWithArgument(candidate);
      if (this.isRecognized(optionAndArgument.key)) {
         this.specFor(optionAndArgument.key).handleOption(this, arguments, detected, optionAndArgument.value);
      } else {
         this.handleShortOptionCluster(candidate, arguments, detected);
      }

   }

   private void handleShortOptionCluster(String candidate, ArgumentList arguments, OptionSet detected) {
      char[] options = extractShortOptionsFrom(candidate);
      this.validateOptionCharacters(options);
      AbstractOptionSpec optionSpec = this.specFor(options[0]);
      if (optionSpec.acceptsArguments() && options.length > 1) {
         String detectedArgument = String.valueOf(options, 1, options.length - 1);
         optionSpec.handleOption(this, arguments, detected, detectedArgument);
      } else {
         char[] arr$ = options;
         int len$ = options.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            char each = arr$[i$];
            this.specFor(each).handleOption(this, arguments, detected, (String)null);
         }
      }

   }

   void noMoreOptions() {
      this.state = OptionParserState.noMoreOptions();
   }

   boolean looksLikeAnOption(String argument) {
      return ParserRules.isShortOptionToken(argument) || ParserRules.isLongOptionToken(argument);
   }

   private boolean isRecognized(String option) {
      return this.recognizedOptions.contains(option);
   }

   private AbstractOptionSpec specFor(char option) {
      return this.specFor(String.valueOf(option));
   }

   private AbstractOptionSpec specFor(String option) {
      return (AbstractOptionSpec)this.recognizedOptions.get(option);
   }

   private void reset() {
      this.state = OptionParserState.moreOptions(this.posixlyCorrect);
   }

   private static char[] extractShortOptionsFrom(String argument) {
      char[] options = new char[argument.length() - 1];
      argument.getChars(1, argument.length(), options, 0);
      return options;
   }

   private void validateOptionCharacters(char[] options) {
      for(int i = 0; i < options.length; ++i) {
         String option = String.valueOf(options[i]);
         if (!this.isRecognized(option)) {
            throw OptionException.unrecognizedOption(option);
         }

         if (this.specFor(option).acceptsArguments()) {
            if (i > 0) {
               throw OptionException.illegalOptionCluster(option);
            }

            return;
         }
      }

   }

   private static KeyValuePair parseLongOptionWithArgument(String argument) {
      return KeyValuePair.valueOf(argument.substring(2));
   }

   private static KeyValuePair parseShortOptionWithArgument(String argument) {
      return KeyValuePair.valueOf(argument.substring(1));
   }

   private Map defaultValues() {
      Map defaults = new HashMap();
      Iterator i$ = this.recognizedOptions.toJavaUtilMap().entrySet().iterator();

      while(i$.hasNext()) {
         Entry each = (Entry)i$.next();
         defaults.put(each.getKey(), ((AbstractOptionSpec)each.getValue()).defaultValues());
      }

      return defaults;
   }
}
