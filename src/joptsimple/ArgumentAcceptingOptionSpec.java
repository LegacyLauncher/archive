package joptsimple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import joptsimple.internal.Objects;
import joptsimple.internal.Reflection;
import joptsimple.internal.ReflectionException;
import joptsimple.internal.Strings;

public abstract class ArgumentAcceptingOptionSpec extends AbstractOptionSpec {
   private static final char NIL_VALUE_SEPARATOR = '\u0000';
   private final boolean argumentRequired;
   private ValueConverter converter;
   private String argumentDescription = "";
   private String valueSeparator = String.valueOf('\u0000');
   private final List defaultValues = new ArrayList();

   ArgumentAcceptingOptionSpec(String option, boolean argumentRequired) {
      super(option);
      this.argumentRequired = argumentRequired;
   }

   ArgumentAcceptingOptionSpec(Collection options, boolean argumentRequired, String description) {
      super(options, description);
      this.argumentRequired = argumentRequired;
   }

   public final ArgumentAcceptingOptionSpec ofType(Class argumentType) {
      return this.withValuesConvertedBy(Reflection.findConverter(argumentType));
   }

   public final ArgumentAcceptingOptionSpec withValuesConvertedBy(ValueConverter aConverter) {
      if (aConverter == null) {
         throw new NullPointerException("illegal null converter");
      } else {
         this.converter = aConverter;
         return this;
      }
   }

   public final ArgumentAcceptingOptionSpec describedAs(String description) {
      this.argumentDescription = description;
      return this;
   }

   public final ArgumentAcceptingOptionSpec withValuesSeparatedBy(char separator) {
      if (separator == 0) {
         throw new IllegalArgumentException("cannot use U+0000 as separator");
      } else {
         this.valueSeparator = String.valueOf(separator);
         return this;
      }
   }

   public ArgumentAcceptingOptionSpec defaultsTo(Object value, Object... values) {
      this.addDefaultValue(value);
      Object[] arr$ = values;
      int len$ = values.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Object each = arr$[i$];
         this.addDefaultValue(each);
      }

      return this;
   }

   private void addDefaultValue(Object value) {
      Objects.ensureNotNull(value);
      this.defaultValues.add(value);
   }

   final void handleOption(OptionParser parser, ArgumentList arguments, OptionSet detectedOptions, String detectedArgument) {
      if (Strings.isNullOrEmpty(detectedArgument)) {
         this.detectOptionArgument(parser, arguments, detectedOptions);
      } else {
         this.addArguments(detectedOptions, detectedArgument);
      }

   }

   protected void addArguments(OptionSet detectedOptions, String detectedArgument) {
      StringTokenizer lexer = new StringTokenizer(detectedArgument, this.valueSeparator);
      if (!lexer.hasMoreTokens()) {
         detectedOptions.addWithArgument(this, detectedArgument);
      } else {
         while(lexer.hasMoreTokens()) {
            detectedOptions.addWithArgument(this, lexer.nextToken());
         }
      }

   }

   protected abstract void detectOptionArgument(OptionParser var1, ArgumentList var2, OptionSet var3);

   protected final Object convert(String argument) {
      if (this.converter == null) {
         return argument;
      } else {
         try {
            return this.converter.convert(argument);
         } catch (ReflectionException var3) {
            throw new OptionArgumentConversionException(this.options(), argument, this.converter.valueType(), var3);
         } catch (ValueConversionException var4) {
            throw new OptionArgumentConversionException(this.options(), argument, this.converter.valueType(), var4);
         }
      }
   }

   protected boolean canConvertArgument(String argument) {
      StringTokenizer lexer = new StringTokenizer(argument, this.valueSeparator);

      try {
         while(lexer.hasMoreTokens()) {
            this.convert(lexer.nextToken());
         }

         return true;
      } catch (OptionException var4) {
         return false;
      }
   }

   protected boolean isArgumentOfNumberType() {
      return this.converter != null && Number.class.isAssignableFrom(this.converter.valueType());
   }

   boolean acceptsArguments() {
      return true;
   }

   boolean requiresArgument() {
      return this.argumentRequired;
   }

   String argumentDescription() {
      return this.argumentDescription;
   }

   String typeIndicator() {
      if (this.converter == null) {
         return null;
      } else {
         String pattern = this.converter.valuePattern();
         return pattern == null ? this.converter.valueType().getName() : pattern;
      }
   }

   List defaultValues() {
      return Collections.unmodifiableList(this.defaultValues);
   }

   public boolean equals(Object that) {
      if (!super.equals(that)) {
         return false;
      } else {
         ArgumentAcceptingOptionSpec other = (ArgumentAcceptingOptionSpec)that;
         return this.requiresArgument() == other.requiresArgument();
      }
   }

   public int hashCode() {
      return super.hashCode() ^ (this.argumentRequired ? 0 : 1);
   }
}
