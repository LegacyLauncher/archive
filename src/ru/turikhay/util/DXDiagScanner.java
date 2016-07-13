package ru.turikhay.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.async.ExtendedThread;

public final class DXDiagScanner {
   private static final String[] DXDIAG_SECTIONS = new String[]{"System Information", "Display Devices", "Sound Devices"};
   private static DXDiagScanner.DXDiagException error;
   private static DXDiagScanner instance;
   private final File dxdiagExe;
   private DXDiagScanner.DXDiagScannerThread thread;
   private DXDiagScanner.DXDiagException threadException;
   private DXDiagScanner.DXDiagScannerResult result;
   private boolean pending;
   private static final Pattern keyValuePattern = Pattern.compile("[\\s]*(.+): (.+)");

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
         throw new DXDiagScanner.DXDiagIncompatibleEnvException("current environment can't be scanned");
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

   public DXDiagScanner.DXDiagScannerResult tryResult() throws DXDiagScanner.DXDiagException {
      if (this.threadException != null) {
         throw this.threadException;
      } else if (this.thread != null && this.thread.isAlive()) {
         throw new DXDiagScanner.DXDiagResultPendingException();
      } else {
         return this.result;
      }
   }

   public DXDiagScanner.DXDiagScannerResult getResult() throws DXDiagScanner.DXDiagException, InterruptedException {
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
            DXDiagScanner.this.result = this.fetch();
         } catch (DXDiagScanner.DXDiagException var2) {
            DXDiagScanner.this.threadException = var2;
         } catch (Exception var3) {
            DXDiagScanner.this.threadException = new DXDiagScanner.DXDiagException(var3);
         }

         DXDiagScanner.this.thread = null;
      }

      private DXDiagScanner.DXDiagScannerResult fetch() throws Exception {
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
            } catch (IllegalThreadStateException var21) {
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
               Scanner fileScanner = null;

               try {
                  fileScanner = new Scanner(new FileInputStream(outputFile));
                  DXDiagScanner.DXDiagScannerResult result = DXDiagScanner.this.new DXDiagScannerResult();
                  StringBuilder b = new StringBuilder();
                  String sectionName = null;
                  boolean nextLineIsSectionName = false;
                  boolean skipSection = false;
                  int firstColumnWidth = 0;

                  while(true) {
                     while(fileScanner.hasNextLine()) {
                        String line = fileScanner.nextLine();
                        if (line.startsWith("------")) {
                           firstColumnWidth = 0;
                           nextLineIsSectionName = !nextLineIsSectionName;
                        } else if (nextLineIsSectionName) {
                           sectionName = line;
                           skipSection = U.find(line, DXDiagScanner.DXDIAG_SECTIONS) == -1;
                           if (!skipSection) {
                              result.passLine("------");
                              result.passLine(line);
                              result.passLine("------");
                           }
                        } else if (!skipSection) {
                           Matcher matcher = DXDiagScanner.keyValuePattern.matcher(line);
                           if (matcher.matches()) {
                              if (firstColumnWidth == 0) {
                                 firstColumnWidth = StringUtils.indexOf(line, ":") + 1;
                              }

                              if (line.length() >= firstColumnWidth) {
                                 boolean lineIsKeyValue = false;

                                 for(int i = 0; i < firstColumnWidth; ++i) {
                                    if (line.charAt(i) != ' ') {
                                       lineIsKeyValue = true;
                                    }
                                 }

                                 if (lineIsKeyValue) {
                                    result.passKeyValue(sectionName, matcher.group(1), matcher.group(2));
                                    continue;
                                 }
                              }
                           }

                           b.setLength(0);
                           b.append('\t').append(StringUtils.trim(line));
                           result.passLine(line);
                        }
                     }

                     DXDiagScanner.this.llog("Done in", Time.stop(), "ms");
                     DXDiagScanner.DXDiagSection.Device system = result.getDevice("System Information");
                     if (system != null) {
                        String operatingSystem = system.get("Operating System");
                        if (operatingSystem != null) {
                           result.is64Bit = operatingSystem.contains("64-bit");
                        }
                     }

                     DXDiagScanner.DXDiagScannerResult var24 = result;
                     return var24;
                  }
               } finally {
                  U.close(fileScanner);
                  outputFile.delete();
               }
            }
         }
      }

      // $FF: synthetic method
      DXDiagScannerThread(Object x1) {
         this();
      }
   }

   public final class DXDiagScannerResult {
      private final ArrayList lines;
      private List linesUnmod;
      private DXDiagScanner.DXDiagSection currentSection;
      private String currentSectionName;
      private final Map sectionMap;
      private boolean is64Bit;

      private DXDiagScannerResult() {
         this.lines = new ArrayList();
         this.linesUnmod = Collections.unmodifiableList(this.lines);
         this.sectionMap = new LinkedHashMap();
      }

      public List getLines() {
         return this.linesUnmod;
      }

      public DXDiagScanner.DXDiagSection getSection(String sectionName) {
         return (DXDiagScanner.DXDiagSection)this.sectionMap.get(sectionName);
      }

      public List getDeviceList(String sectionName) {
         DXDiagScanner.DXDiagSection section = this.getSection(sectionName);
         return section == null ? null : section.getDeviceList();
      }

      public DXDiagScanner.DXDiagSection.Device getDevice(String sectionName) {
         List deviceList = this.getDeviceList(sectionName);
         return deviceList != null && deviceList.size() == 1 ? (DXDiagScanner.DXDiagSection.Device)deviceList.get(0) : null;
      }

      void passLine(String line) {
         this.lines.add(line);
      }

      void passKeyValue(String section, String key, String value) {
         if (!section.equals(this.currentSectionName)) {
            this.currentSectionName = section;
            this.sectionMap.put(section, this.currentSection = DXDiagScanner.this.new DXDiagSection(section));
         }

         this.currentSection.passKeyValue(key, value);
      }

      // $FF: synthetic method
      DXDiagScannerResult(Object x1) {
         this();
      }
   }

   public final class DXDiagSection {
      private final String name;
      private final List deviceList;
      private final List deviceListUnmod;
      private DXDiagScanner.DXDiagSection.Device device;
      private final List keys;

      private DXDiagSection(String name) {
         this.deviceList = new ArrayList();
         this.deviceListUnmod = Collections.unmodifiableList(this.deviceList);
         this.keys = new ArrayList();
         this.name = name;
      }

      public List getDeviceList() {
         return this.deviceListUnmod;
      }

      void passKeyValue(String key, String value) {
         boolean contain = this.keys.contains(key);
         if (this.device == null || contain) {
            this.deviceList.add(this.device = new DXDiagScanner.DXDiagSection.Device());
         }

         if (contain) {
            this.keys.clear();
         } else {
            this.keys.add(key);
         }

         this.device.keyValue.put(key, value);
      }

      // $FF: synthetic method
      DXDiagSection(String x1, Object x2) {
         this(x1);
      }

      public final class Device {
         private Map keyValue = new LinkedHashMap();
         private Map keyValueUnmod;

         public Device() {
            this.keyValueUnmod = Collections.unmodifiableMap(this.keyValue);
         }

         public String get(String key) {
            return (String)this.keyValue.get(key);
         }

         public String toString() {
            return (new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).append(this.keyValue).build();
         }
      }
   }
}
