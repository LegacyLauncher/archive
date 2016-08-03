package ru.turikhay.tlauncher.ui.background;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;
import ru.turikhay.util.async.FxRunnable;

public class FXWrapper extends ExtendedLayeredPane implements ISwingBackground {
   private final Class clazz;
   private FXWrapper.FXInitializer init;
   private FXWrapper.JFX wrapper;

   public FXWrapper(Class fxBackground) {
      this.clazz = fxBackground;
      this.setOpaque(false);
      this.setBackground(Color.yellow);
      this.init = new FXWrapper.FXInitializer();
      this.addComponentListener(new ExtendedComponentAdapter(this, 200) {
         public void onComponentResized(ComponentEvent e) {
            if (FXWrapper.this.wrapper != null) {
               FXWrapper.this.wrapper.onResize();
            }

         }
      });
   }

   private void init(Runnable runnable, boolean join) {
      FXWrapper.FXInitializer init = this.init;
      if (init == null) {
         runnable.run();
      } else {
         if (init.isAlive()) {
            this.log("FX is initializing so far...");
         } else {
            init.start();
         }

         if (join) {
            try {
               init.join();
            } catch (InterruptedException var5) {
            }

            runnable.run();
         } else {
            init.queue.add(runnable);
         }

      }
   }

   private void init(Runnable r) {
      this.init(r, false);
   }

   public void onResize() {
      if (this.getParent() != null) {
         Dimension size = this.getParent().getSize();
         this.setSize(size);
         this.log("fxwrapper resized:", size);
      }

   }

   public void startBackground() {
      this.init(new FxRunnable() {
         public void runFx() {
            FXWrapper.this.wrapper.background.startBackground();
         }
      });
   }

   public void pauseBackground() {
      this.init(new FxRunnable() {
         public void runFx() {
            FXWrapper.this.wrapper.background.pauseBackground();
         }
      });
   }

   public void loadBackground(final String path) throws Exception {
      this.init(new FxRunnable() {
         public void runFx() {
            try {
               FXWrapper.this.wrapper.background.loadBackground(path);
            } catch (Exception var2) {
               FXWrapper.this.log("could not load fx background", path, var2);
            }

         }
      }, OS.WINDOWS.isCurrent());
   }

   private void log(Object... o) {
      if (Platform.isFxApplicationThread()) {
         U.log("[FXWrapper][in FX]", o);
      } else {
         U.log("[FXWrapper]", o);
      }

   }

   private class FXInitializer extends ExtendedThread {
      private final Queue queue;

      private FXInitializer() {
         this.queue = new ConcurrentLinkedQueue();
      }

      public void run() {
         this.checkCurrent();

         try {
            FXWrapper.this.log("Initializing...");
            FXWrapper.this.wrapper = FXWrapper.this.new JFX();
         } catch (Exception var2) {
            FXWrapper.this.log("Could not init FX background", var2);
         }

         FXWrapper.this.init = null;

         Runnable task;
         while((task = (Runnable)this.queue.poll()) != null) {
            task.run();
         }

      }

      // $FF: synthetic method
      FXInitializer(Object x1) {
         this();
      }
   }

   private class JFX extends JFXPanel implements ResizeableComponent {
      private IFXBackground background;

      JFX() throws Exception {
         this.setBackground(Color.green);
         final Exception[] exception = new Exception[1];
         Platform.setImplicitExit(false);
         Platform.runLater(new Runnable() {
            public void run() {
               try {
                  JFX.this.background = (IFXBackground)FXWrapper.this.clazz.newInstance();
                  Scene scene = new Scene((Parent)JFX.this.background, javafx.scene.paint.Color.MAGENTA);
                  scene.setFill(javafx.scene.paint.Color.DARKGRAY);
                  JFX.this.setScene(scene);
               } catch (Exception var2) {
                  FXWrapper.this.log("Could not create background", exception[0] = var2);
               }

            }
         });

         while(this.background == null && exception[0] == null) {
            U.sleepFor(500L);
         }

         if (exception[0] != null) {
            try {
               Platform.exit();
            } catch (Exception var4) {
               FXWrapper.this.log("Could not exit JavaFX", var4);
            }

            throw exception[0];
         } else {
            FXWrapper.this.add(this);
            this.onResize();
            FXWrapper.this.log("FX background successfully created", this.background);
         }
      }

      public void onResize() {
         Dimension size = FXWrapper.this.getSize();
         this.setBounds(0, 0, size.width, size.height);
      }
   }
}
