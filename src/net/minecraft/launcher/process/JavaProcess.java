package net.minecraft.launcher.process;

import java.util.List;

public class JavaProcess {
   private static final int MAX_SYSOUT_LINES = 5;
   private final List commands;
   private final Process process;
   private final LimitedCapacityList sysOutLines = new LimitedCapacityList(String.class, 5);
   private JavaProcessListener onExit;

   public JavaProcess(List commands, Process process) {
      this.commands = commands;
      this.process = process;
      ProcessMonitorThread monitor = new ProcessMonitorThread(this);
      monitor.start();
   }

   public Process getRawProcess() {
      return this.process;
   }

   public List getStartupCommands() {
      return this.commands;
   }

   public String getStartupCommand() {
      return this.process.toString();
   }

   public LimitedCapacityList getSysOutLines() {
      return this.sysOutLines;
   }

   public boolean isRunning() {
      try {
         this.process.exitValue();
         return false;
      } catch (IllegalThreadStateException var2) {
         return true;
      }
   }

   void setExitRunnable(JavaProcessListener runnable) {
      this.onExit = runnable;
   }

   public void safeSetExitRunnable(JavaProcessListener runnable) {
      this.setExitRunnable(runnable);
      if (!this.isRunning() && runnable != null) {
         runnable.onJavaProcessEnded(this);
      }

   }

   public JavaProcessListener getExitRunnable() {
      return this.onExit;
   }

   public int getExitCode() {
      try {
         return this.process.exitValue();
      } catch (IllegalThreadStateException var2) {
         var2.fillInStackTrace();
         throw var2;
      }
   }

   public String toString() {
      return "JavaProcess[commands=" + this.commands + ", isRunning=" + this.isRunning() + "]";
   }

   public void stop() {
      this.process.destroy();
   }
}
