package ru.turikhay.tlauncher.ui.background;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.U;

class Worker {
   private final ExecutorService service = Executors.newSingleThreadExecutor();
   private final Worker.CoverTask showBackgroundTask = new Worker.CoverTask(0.0F);
   private final Worker.CoverTask hideBackgroundTask = new Worker.CoverTask(1.0F);
   private final BackgroundManager wrapper;

   Worker(BackgroundManager wrapper) {
      this.wrapper = wrapper;
   }

   void setBackground(IBackground background, String path) {
      this.submit(new Worker.SetBackgroundTask(background, path));
   }

   private void submit(Runnable runnable, boolean requireAsync) {
      if (!requireAsync && !TLauncher.getInstance().isReady()) {
         runnable.run();
      } else {
         this.service.submit(runnable);
      }

   }

   private void submit(Runnable runnable) {
      this.submit(runnable, false);
   }

   private void log(Object... o) {
      U.log("[Background][Worker]", o);
   }

   private class SetBackgroundTask implements Runnable {
      private final IBackground background;
      private final String path;

      private SetBackgroundTask(IBackground background, String path) {
         this.background = (IBackground)U.requireNotNull(background, "background");
         this.path = path;
      }

      public void run() {
         Worker.this.hideBackgroundTask.run();
         Worker.this.wrapper.setBackground(this.background);

         try {
            this.background.loadBackground(this.path);
         } catch (Exception var2) {
            Worker.this.log("Could not load background for", this.background, "; path:", this.path, var2);
         }

         Worker.this.showBackgroundTask.run();
      }

      // $FF: synthetic method
      SetBackgroundTask(IBackground x1, String x2, Object x3) {
         this(x1, x2);
      }
   }

   private class CoverTask implements Runnable {
      final float targetOpacity;

      CoverTask(float targetOpacity) {
         this.targetOpacity = targetOpacity;
      }

      public void run() {
         if (!TLauncher.getInstance().isReady()) {
            Worker.this.wrapper.cover.setOpacity(this.targetOpacity);
         } else {
            if (this.targetOpacity == 0.0F) {
               U.sleepFor(1000L);
            }

            float opacity = Worker.this.wrapper.cover.getOpacity();
            float step = opacity > this.targetOpacity ? -0.025F : 0.025F;
            float eps = Math.abs(step / 2.0F);
            Worker.this.log("setting opacity:", opacity, this.targetOpacity, step);

            while(Math.abs(opacity - this.targetOpacity) > eps) {
               Worker.this.wrapper.cover.setOpacity(opacity += step);
               U.sleepFor(16L);
            }

            Worker.this.log("opacity set to", this.targetOpacity);
         }
      }
   }
}
