package ru.turikhay.tlauncher.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window.Type;
import javax.swing.JFrame;
import javax.swing.JLabel;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

public class LoadingFrame extends JFrame {
   private final ProgressBar progress;

   public LoadingFrame() {
      SwingUtil.initLookAndFeel();
      this.setLayout(new BorderLayout());
      this.progress = new ProgressBar();
      this.progress.setPreferredSize(new Dimension(250, 0));
      this.add(this.progress, "Center");
      this.add(new JLabel(Images.getIcon("fav32.png")), "West");
      if (OS.JAVA_VERSION.getDouble() > 1.6D) {
         this.setType(Type.UTILITY);
      }

      this.pack();
      this.setResizable(false);
      this.setAlwaysOnTop(true);
      this.setLocationRelativeTo((Component)null);
   }

   public void setProgress(int percent) {
      this.progress.setIndeterminate(false);
      this.progress.setValue(percent);
   }
}
