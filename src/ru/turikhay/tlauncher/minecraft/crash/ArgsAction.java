package ru.turikhay.tlauncher.minecraft.crash;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.U;

public abstract class ArgsAction extends BindableAction {
   protected final OptionParser parser;

   ArgsAction(String name) {
      super(name);
      this.parser = new OptionParser();
   }

   ArgsAction(String name, String[] args) {
      this(name);
      if (((String[])U.requireNotNull(args)).length == 0) {
         throw new IllegalArgumentException();
      } else {
         String[] var3 = args;
         int var4 = args.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String arg = var3[var5];
            this.parser.accepts(arg).withRequiredArg();
         }

      }
   }

   public void execute(String args) throws Exception {
      this.log(new Object[]{"Args passed: \"" + args + "\""});
      OptionSet set = this.parser.parse(StringUtils.split(args, ' '));
      this.execute(set);
   }

   abstract void execute(OptionSet var1) throws Exception;
}
