package ru.turikhay.tlauncher.ui.crash;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.crash.CrashManager;
import ru.turikhay.tlauncher.minecraft.crash.CrashManagerListener;
import ru.turikhay.tlauncher.ui.frames.BActionFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.util.SwingUtil;

public class CrashProcessingFrame extends BActionFrame implements CrashManagerListener {
   private final CrashFrame frame = new CrashFrame(this);
   private CrashManager manager;

   public CrashProcessingFrame() {
      this.setMinimumSize(SwingUtil.magnify(new Dimension(500, 150)));
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            if (CrashProcessingFrame.this.manager != null) {
               CrashProcessingFrame.this.manager.cancel();
            }

         }
      });
      this.setTitlePath("crash.loading.title", new Object[0]);
      this.getHead().setText("crash.loading.head");
      ProgressBar progress = new ProgressBar();
      progress.setPreferredSize(new Dimension(1, SwingUtil.magnify(32)));
      progress.setIndeterminate(true);
      this.getBody().setCenter(progress);
      this.getBody().setWest(Images.getIcon("lightbulb.png"));
   }

   public CrashFrame getCrashFrame() {
      return this.frame;
   }

   public void onCrashManagerProcessing(CrashManager manager) {
      this.manager = manager;
      this.showAtCenter();
   }

   public void onCrashManagerComplete(CrashManager manager, Crash crash) {
      this.manager = null;
      this.frame.setCrash(crash);
      this.setVisible(false);
   }

   public void onCrashManagerCancelled(CrashManager manager) {
      this.manager = null;
      this.setVisible(false);
   }

   public void onCrashManagerFailed(CrashManager manager, Exception e) {
      this.manager = null;
      this.setVisible(false);
   }

   public void updateLocale() {
      super.updateLocale();
      this.frame.updateLocale();
   }
}
