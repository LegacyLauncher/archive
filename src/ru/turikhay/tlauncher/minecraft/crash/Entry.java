package ru.turikhay.tlauncher.minecraft.crash;

public abstract class Entry extends IEntry {
   public Entry(CrashManager manager, String name) {
      super(manager, name);
   }

   protected abstract void execute() throws Exception;
}
