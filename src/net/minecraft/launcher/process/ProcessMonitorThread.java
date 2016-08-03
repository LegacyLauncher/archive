package net.minecraft.launcher.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

class ProcessMonitorThread extends Thread {
   private final JavaProcess process;
   private final Charset charset;

   public ProcessMonitorThread(JavaProcess process) {
      Charset c = FileUtil.getCharset();
      if (TLauncher.getInstance() != null && "ru_RU".equals(TLauncher.getInstance().getSettings().getLocale().toString()) && OS.WINDOWS.isCurrent()) {
         try {
            c = Charset.forName("CP1251");
         } catch (UnsupportedCharsetException var4) {
            U.log("Windows charset is not supported?!", var4);
         }
      }

      this.charset = c;
      this.process = process;
   }

   public void run() {
      JavaProcessListener listener = this.process.getExitRunnable();
      Process raw = this.process.getRawProcess();
      BufferedReader buf = new BufferedReader(new InputStreamReader(raw.getInputStream(), this.charset));

      while(this.process.isRunning()) {
         try {
            String line;
            try {
               for(; (line = buf.readLine()) != null; this.process.getSysOutLines().add(line)) {
                  if (listener != null) {
                     listener.onJavaProcessLog(this.process, line);
                  }
               }
            } catch (IOException var14) {
            }
         } finally {
            try {
               buf.close();
            } catch (IOException var13) {
               Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.SEVERE, (String)null, var13);
            }

         }
      }

      if (listener != null) {
         listener.onJavaProcessEnded(this.process);
      }

   }
}
