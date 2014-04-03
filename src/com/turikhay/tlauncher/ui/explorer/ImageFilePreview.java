package com.turikhay.tlauncher.ui.explorer;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import net.minecraft.launcher.OperatingSystem;

public class ImageFilePreview extends JComponent {
   private static final long serialVersionUID = -1465489971097254329L;
   private static final Cursor DEFAULT = Cursor.getDefaultCursor();
   private static final Cursor HAND = Cursor.getPredefinedCursor(12);
   ImageIcon thumbnail = null;
   File file = null;

   public ImageFilePreview(JFileChooser fc) {
      this.setCursor(DEFAULT);
      this.setPreferredSize(new Dimension(200, 100));
      fc.addPropertyChangeListener(new PropertyChangeListener() {
         public void propertyChange(PropertyChangeEvent e) {
            boolean update = false;
            String prop = e.getPropertyName();
            if ("directoryChanged".equals(prop)) {
               ImageFilePreview.this.file = null;
               update = true;
            } else if ("SelectedFileChangedProperty".equals(prop)) {
               ImageFilePreview.this.file = (File)e.getNewValue();
               update = true;
            }

            if (update) {
               ImageFilePreview.this.thumbnail = null;
               if (ImageFilePreview.this.isShowing()) {
                  ImageFilePreview.this.loadImage();
                  ImageFilePreview.this.repaint();
               }
            }

         }
      });
      this.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            if (e.getButton() == 1) {
               if (ImageFilePreview.this.file != null) {
                  OperatingSystem.openFile(ImageFilePreview.this.file);
               }

            }
         }
      });
   }

   public void loadImage() {
      if (this.file == null) {
         this.thumbnail = null;
         this.setCursor(DEFAULT);
      } else {
         ImageIcon tmpIcon = new ImageIcon(this.file.getPath());
         this.setCursor(HAND);
         if (tmpIcon != null) {
            if (tmpIcon.getIconWidth() > 190) {
               this.thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(190, -1, 1));
            } else {
               this.thumbnail = tmpIcon;
            }
         }

      }
   }

   protected void paintComponent(Graphics g) {
      if (this.thumbnail == null) {
         this.loadImage();
      }

      if (this.thumbnail != null) {
         int x = this.getWidth() / 2 - this.thumbnail.getIconWidth() / 2;
         int y = this.getHeight() / 2 - this.thumbnail.getIconHeight() / 2;
         if (y < 0) {
            y = 0;
         }

         if (x < 10) {
            x = 10;
         }

         this.thumbnail.paintIcon(this, g, x, y);
      }

   }
}
