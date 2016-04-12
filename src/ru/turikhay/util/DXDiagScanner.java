package ru.turikhay.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.async.ExtendedThread;

public final class DXDiagScanner {
   private static final String[] DXDIAG_SECTIONS = new String[]{"System Information", "Display Devices", "Sound Devices"};
   private static DXDiagScanner.DXDiagException error;
   private static DXDiagScanner instance;
   private final File dxdiagExe;
   private DXDiagScanner.DXDiagScannerThread thread;
   private DXDiagScanner.DXDiagException threadException;
   private List result;
   private boolean pending;

   public static DXDiagScanner getInstance() throws DXDiagScanner.DXDiagException {
      if (error != null) {
         throw error;
      } else {
         if (instance == null) {
            try {
               instance = new DXDiagScanner();
            } catch (DXDiagScanner.DXDiagException var1) {
               error = var1;
               throw var1;
            } catch (Throwable var2) {
               throw error = new DXDiagScanner.DXDiagException(var2);
            }
         }

         return instance;
      }
   }

   public static void scheduleScan() {
      try {
         getInstance().tryResult();
      } catch (DXDiagScanner.DXDiagException var1) {
      }

   }

   private DXDiagScanner() throws DXDiagScanner.DXDiagException {
      if (!OS.WINDOWS.isCurrent()) {
         throw new DXDiagScanner.DXDiagIncompatibleEnvException("DXDiagScanner can run under Windows only");
      } else {
         String systemRoot = System.getenv("WINDIR");
         if (systemRoot == null) {
            throw new DXDiagScanner.DXDiagIncompatibleEnvException("WINDIR is not defined in the environment");
         } else {
            this.dxdiagExe = new File(systemRoot + "\\system32\\dxdiag.exe");
            if (!this.dxdiagExe.isFile()) {
               throw new DXDiagScanner.DXDiagIncompatibleEnvException("dxdiag doesn't exist is the system32 folder");
            } else {
               this.thread = new DXDiagScanner.DXDiagScannerThread();
               this.thread.start();
            }
         }
      }
   }

   public List tryResult() throws DXDiagScanner.DXDiagException {
      if (this.threadException != null) {
         throw this.threadException;
      } else if (this.thread != null && this.thread.isAlive()) {
         throw new DXDiagScanner.DXDiagResultPendingException();
      } else {
         return this.result;
      }
   }

   public List getResult() throws DXDiagScanner.DXDiagException, InterruptedException {
      this.pending = true;
      if (this.threadException != null) {
         throw this.threadException;
      } else {
         if (this.thread != null && this.thread.isAlive()) {
            this.thread.join();
         }

         return this.result;
      }
   }

   private void llog(Object... o) {
      if (this.pending) {
         U.log("[DxDiagScanner]", o);
      }

   }

   public static class DXDiagResultException extends DXDiagScanner.DXDiagException {
      private DXDiagResultException(String message) {
         super((String)message, null);
      }

      // $FF: synthetic method
      DXDiagResultException(String x0, Object x1) {
         this(x0);
      }
   }

   public static class DXDiagResultPendingException extends DXDiagScanner.DXDiagException {
      private DXDiagResultPendingException() {
         super((<undefinedtype>)null);
      }

      // $FF: synthetic method
      DXDiagResultPendingException(Object x0) {
         this();
      }
   }

   public static class DXDiagIncompatibleEnvException extends DXDiagScanner.DXDiagException {
      private DXDiagIncompatibleEnvException(String message) {
         super((String)message, null);
      }

      // $FF: synthetic method
      DXDiagIncompatibleEnvException(String x0, Object x1) {
         this(x0);
      }
   }

   public static class DXDiagException extends Exception {
      private DXDiagException() {
      }

      private DXDiagException(String message) {
         super(message);
      }

      private DXDiagException(Throwable cause) {
         super("unknown error", cause);
      }

      // $FF: synthetic method
      DXDiagException(Throwable x0, Object x1) {
         this(x0);
      }

      // $FF: synthetic method
      DXDiagException(String x0, Object x1) {
         this(x0);
      }

      // $FF: synthetic method
      DXDiagException(Object x0) {
         this();
      }
   }

   private class DXDiagScannerThread extends ExtendedThread {
      private DXDiagScannerThread() {
      }

      public void run() {
         try {
            DXDiagScanner.this.result = Collections.unmodifiableList(this.fetch());
         } catch (DXDiagScanner.DXDiagException var2) {
            DXDiagScanner.this.threadException = var2;
         } catch (Exception var3) {
            DXDiagScanner.this.threadException = new DXDiagScanner.DXDiagException(var3);
         }

         DXDiagScanner.this.thread = null;
      }

      private List fetch() throws Exception {
         Time.start();
         File outputFile = File.createTempFile("tlauncher-dxdiag", (String)null);
         outputFile.deleteOnExit();
         ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"cmd.exe", "/c", DXDiagScanner.this.dxdiagExe.getAbsolutePath(), "/whql:off", "/dontskip", "/t", outputFile.getAbsolutePath()});
         final Process process = processBuilder.start();
         int timer = 0;

         while(timer < 60) {
            DXDiagScanner.this.llog("DXDiagScanner is waiting for result...", timer);

            try {
               process.exitValue();
            } catch (IllegalThreadStateException var12) {
               ++timer;
               U.sleepFor(1000L);
               continue;
            }

            timer = 0;
            break;
         }

         if (timer == 60) {
            DXDiagScanner.this.llog("Timeout is exceeded");
            AsyncThread.execute(new Runnable() {
               public void run() {
                  process.destroy();
               }
            });
            throw new DXDiagScanner.DXDiagResultException("timeout");
         } else {
            int exitCode = process.exitValue();
            if (exitCode != 0) {
               throw new DXDiagScanner.DXDiagResultException("invalid exit code: " + exitCode);
            } else {
               List lines = new ArrayList();
               StringBuilder b = new StringBuilder();
               Scanner fileScanner = new Scanner(new FileInputStream(outputFile));
               boolean nextLineIsSectionName = false;
               boolean skipSection = false;

               while(fileScanner.hasNextLine()) {
                  String line = fileScanner.nextLine();
                  if (line.startsWith("------")) {
                     nextLineIsSectionName = !nextLineIsSectionName;
                  } else if (nextLineIsSectionName) {
                     skipSection = U.find(line, DXDiagScanner.DXDIAG_SECTIONS) == -1;
                     if (!skipSection) {
                        lines.add("------");
                        lines.add(line);
                        lines.add("------");
                     }
                  } else if (!skipSection) {
                     b.setLength(0);
                     b.append('\t').append(StringUtils.trim(line));
                     lines.add(b.toString());
                  }
               }

               DXDiagScanner.this.llog("Done in", Time.stop());
               outputFile.delete();
               return lines;
            }
         }
      }

      // $FF: synthetic method
      DXDiagScannerThread(Object x1) {
         this();
      }
   }
}
