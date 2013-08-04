package net.minecraft.launcher_.process;

import com.turikhay.tlauncher.util.U;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessMonitorThread extends Thread {
   private final JavaProcess process;

   public ProcessMonitorThread(JavaProcess process) {
      this.process = process;
   }

   public void run() {
      Process raw = this.process.getRawProcess();
      InputStreamReader reader = new InputStreamReader(raw.getInputStream());
      BufferedReader buf = new BufferedReader(reader);
      String line = null;
      JavaProcessListener listener = this.process.getExitRunnable();

      while(this.process.isRunning()) {
         try {
            while((line = buf.readLine()) != null) {
               U.plog("> " + line);
               this.process.getSysOutLines().add(line);
            }
         } catch (IOException var15) {
            Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.SEVERE, (String)null, var15);
            if (listener != null) {
               listener.onJavaProcessError(this.process, var15);
            }
         } finally {
            try {
               buf.close();
            } catch (IOException var14) {
               Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.SEVERE, (String)null, var14);
            }

         }
      }

      if (listener != null) {
         listener.onJavaProcessEnded(this.process);
      }

   }
}
