package net.minecraft.launcher.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import ru.turikhay.util.OS;

public class JavaProcessLauncher {
   private final String jvmPath;
   private final List commands;
   private File directory;
   private ProcessBuilder process;

   public JavaProcessLauncher(String jvmPath, String[] commands) {
      if (jvmPath == null) {
         jvmPath = OS.getJavaPath();
      }

      this.jvmPath = jvmPath;
      this.commands = new ArrayList();
      Collections.addAll(this.commands, commands);
   }

   public JavaProcess start() throws IOException {
      List full = this.getFullCommands();
      return new JavaProcess(full, this.createProcess().start());
   }

   public ProcessBuilder createProcess() {
      if (this.process == null) {
         this.process = (new ProcessBuilder(this.getFullCommands())).directory(this.directory).redirectErrorStream(true);
      }

      return this.process;
   }

   List getFullCommands() {
      List result = new ArrayList(this.commands);
      result.add(0, this.getJavaPath());
      return result;
   }

   public String getCommandsAsString() {
      List parts = this.getFullCommands();
      StringBuilder full = new StringBuilder();
      boolean first = true;

      String part;
      for(Iterator var5 = parts.iterator(); var5.hasNext(); full.append(part)) {
         part = (String)var5.next();
         if (first) {
            first = false;
         } else {
            full.append(' ');
         }
      }

      return full.toString();
   }

   public List getCommands() {
      return this.commands;
   }

   public void addCommand(Object command) {
      this.commands.add(command.toString());
   }

   public void addCommand(Object key, Object value) {
      this.commands.add(key.toString());
      this.commands.add(value.toString());
   }

   public void addCommands(Object[] commands) {
      Object[] var5 = commands;
      int var4 = commands.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         Object c = var5[var3];
         this.commands.add(c.toString());
      }

   }

   public void addSplitCommands(Object commands) {
      this.addCommands(commands.toString().split(" "));
   }

   public JavaProcessLauncher directory(File directory) {
      this.directory = directory;
      return this;
   }

   public File getDirectory() {
      return this.directory;
   }

   String getJavaPath() {
      return this.jvmPath;
   }

   public String toString() {
      return "JavaProcessLauncher[commands=" + this.commands + ", java=" + this.jvmPath + "]";
   }
}
