package ru.turikhay.tlauncher.minecraft.crash;

import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

public abstract class BindableAction {
   private final String name;
   private final String logPrefix;

   public BindableAction(String name) {
      this.name = StringUtil.requireNotBlank(name);
      this.logPrefix = "[BindAction][" + name + "]";
   }

   public final String getName() {
      return this.name;
   }

   public abstract void execute(String var1) throws Exception;

   public BindableAction.Binding bind(String arg) {
      return new BindableAction.Binding(this, arg);
   }

   protected void log(Object... o) {
      U.log(this.logPrefix, o);
   }

   class Binding implements Action {
      private final String arg;

      Binding(BindableAction action, String arg) {
         this.arg = (String)U.requireNotNull(arg);
      }

      public void execute() throws Exception {
         BindableAction.this.execute(this.arg);
      }
   }
}
