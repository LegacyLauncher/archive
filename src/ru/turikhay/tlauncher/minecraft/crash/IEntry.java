package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

public class IEntry {
   private final CrashManager manager;
   private final String name;
   private final String logName;

   public IEntry(CrashManager manager, String name) {
      this.manager = (CrashManager)U.requireNotNull(manager);
      this.name = StringUtil.requireNotBlank(name, "name");
      this.logName = "[" + this.getClass().getSimpleName() + ":" + name + "]";
   }

   public final CrashManager getManager() {
      return this.manager;
   }

   public final String getName() {
      return this.name;
   }

   public final String toString() {
      return this.buildToString().build();
   }

   protected ToStringBuilder buildToString() {
      return (new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).append("name", this.getName());
   }

   protected final void log(Object... o) {
      this.getManager().log(this.logName, o);
   }
}
