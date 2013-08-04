package net.minecraft.launcher_.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.launcher_.OperatingSystem;

public class JavaProcessLauncher {
   private final String jvmPath;
   private final List commands;
   private File directory;

   public JavaProcessLauncher(String jvmPath, String[] commands) {
      if (jvmPath == null) {
         jvmPath = OperatingSystem.getCurrentPlatform().getJavaDir();
      }

      this.jvmPath = jvmPath;
      this.commands = new ArrayList();
      Collections.addAll(this.commands, commands);
   }

   public JavaProcess start() throws IOException {
      List full = this.getFullCommands();
      return new JavaProcess(full, (new ProcessBuilder(full)).directory(this.directory).redirectErrorStream(true).start());
   }

   public List getFullCommands() {
      List result = new ArrayList(this.commands);
      result.add(0, this.getJavaPath());
      return result;
   }

   public List getCommands() {
      return this.commands;
   }

   public void addCommand(String command) {
      this.commands.add(command);
   }

   public void addCommand(String key, String value) {
      this.commands.add(key);
      this.commands.add(value);
   }

   public void addCommands(String[] commands) {
      this.commands.addAll(Arrays.asList(commands));
   }

   public void addSplitCommands(String commands) {
      this.addCommands(commands.split(" "));
   }

   public JavaProcessLauncher directory(File directory) {
      this.directory = directory;
      return this;
   }

   public File getDirectory() {
      return this.directory;
   }

   protected String getJavaPath() {
      return this.jvmPath;
   }

   public String toString() {
      return "JavaProcessLauncher[commands=" + this.commands + ", java=" + this.jvmPath + "]";
   }
}
