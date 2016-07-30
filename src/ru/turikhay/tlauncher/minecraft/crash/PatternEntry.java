package ru.turikhay.tlauncher.minecraft.crash;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.util.U;

public class PatternEntry extends CrashEntry {
   private final Pattern pattern;

   public PatternEntry(CrashManager manager, String name, Pattern pattern) {
      super(manager, name);
      this.pattern = (Pattern)U.requireNotNull(pattern, "pattern");
   }

   protected boolean checkCapability() throws Exception {
      if (!super.checkCapability()) {
         return false;
      } else {
         Scanner scanner = this.getScanner();

         Matcher matcher;
         do {
            if (!scanner.hasNextLine()) {
               return false;
            }

            matcher = this.pattern.matcher(scanner.nextLine());
         } while(!matcher.matches());

         return true;
      }
   }

   public Scanner getScanner() {
      return getScanner(this.getManager().getOutput());
   }

   public static Scanner getScanner(CharSequence output) {
      return new Scanner(new CharSequenceInputStream(output, "UTF-8"));
   }

   public ToStringBuilder buildToString() {
      return super.buildToString().append("pattern", this.pattern);
   }
}
