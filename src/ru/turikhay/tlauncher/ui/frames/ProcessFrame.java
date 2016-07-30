package ru.turikhay.tlauncher.ui.frames;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.util.SwingUtil;

public class ProcessFrame extends BActionFrame {
   private final ExecutorService service = Executors.newCachedThreadPool();
   private volatile ProcessFrame.Process process;

   public ProcessFrame() {
      this.setMinimumSize(SwingUtil.magnify(new Dimension(500, 1)));
      this.addComponentListener(new ComponentAdapter() {
         public void componentHidden(ComponentEvent e) {
            ProcessFrame.this.process = null;
            ProcessFrame.this.onCancelled();
         }
      });
      ProgressBar progress = new ProgressBar();
      progress.setPreferredSize(new Dimension(1, SwingUtil.magnify(32)));
      progress.setIndeterminate(true);
      this.getBody().setCenter(progress);
   }

   public final void setIcon(String path) {
      this.getBody().setWest(Images.getIcon(path, SwingUtil.magnify(32)));
   }

   public final void submit(ProcessFrame.Process process) {
      this.process = process;
      if (process != null) {
         this.service.submit(process);
      } else {
         this.onCancelled();
      }

   }

   protected void onProcessing(ProcessFrame.Process process) {
      this.checkIfCurrent(process);
      this.showAtCenter();
   }

   protected void onSucceeded(ProcessFrame.Process process, Object result) {
      this.checkIfCurrent(process);
      this.setVisible(false);
   }

   protected void onFailed(ProcessFrame.Process process, Exception e) {
      this.checkIfCurrent(process);
      this.setVisible(false);
   }

   protected void onCancelled() {
      this.setVisible(false);
   }

   protected void checkIfCurrent(ProcessFrame.Process process) throws IllegalStateException {
      if (this.process != process) {
         throw new IllegalStateException();
      }
   }

   public abstract class Process implements Runnable {
      public void run() {
         ProcessFrame.this.onProcessing(this);

         Object result;
         try {
            result = this.get();
         } catch (Exception var3) {
            ProcessFrame.this.onFailed(this, var3);
            return;
         }

         ProcessFrame.this.onSucceeded(this, result);
      }

      protected abstract Object get() throws Exception;
   }
}
