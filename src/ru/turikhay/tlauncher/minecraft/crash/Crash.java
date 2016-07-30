package ru.turikhay.tlauncher.minecraft.crash;

import java.io.File;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class Crash {
   private final CrashManager manager;
   private CrashEntry entry;
   private File crashFile;
   private File nativeCrashFile;

   Crash(CrashManager manager) {
      this.manager = manager;
   }

   public CrashManager getManager() {
      return this.manager;
   }

   public CrashEntry getEntry() {
      return this.entry;
   }

   void setEntry(CrashEntry entry) {
      this.entry = entry;
   }

   public File getCrashFile() {
      return this.crashFile;
   }

   void setCrashFile(String path) {
      this.crashFile = new File(path);
   }

   public File getNativeCrashFile() {
      return this.nativeCrashFile;
   }

   void setNativeCrashFile(String path) {
      this.nativeCrashFile = new File(path);
   }

   public String toString() {
      return (new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).append("entry", this.entry).append("crashFile", this.crashFile).append("nativeCrashFile", this.nativeCrashFile).build();
   }
}
