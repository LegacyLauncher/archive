package ru.turikhay.tlauncher.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.util.SwingUtil;

public class ImageButton extends ExtendedButton {
   private static final long serialVersionUID = 1L;
   protected Image image;
   protected ImageButton.ImageRotation rotation;
   private int margin;
   private boolean pressed;

   protected ImageButton() {
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.margin = 4;
      this.initListeners();
   }

   public ImageButton(String label, Image image, ImageButton.ImageRotation rotation, int margin) {
      super(label);
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.margin = 4;
      this.image = image;
      this.rotation = rotation;
      this.margin = margin;
      this.initImage();
      this.initListeners();
   }

   public ImageButton(String label, Image image, ImageButton.ImageRotation rotation) {
      this(label, (Image)image, rotation, 4);
   }

   public ImageButton(String label, Image image) {
      this(label, image, ImageButton.ImageRotation.CENTER);
   }

   public ImageButton(Image image) {
      this((String)null, (Image)image);
   }

   public ImageButton(String imagepath) {
      this((String)null, (Image)loadImage(imagepath));
   }

   public ImageButton(String label, String imagepath, ImageButton.ImageRotation rotation, int margin) {
      this(label, loadImage(imagepath), rotation, margin);
   }

   public ImageButton(String label, String imagepath, ImageButton.ImageRotation rotation) {
      this(label, loadImage(imagepath), rotation);
   }

   public ImageButton(String label, String imagepath) {
      this(label, loadImage(imagepath));
   }

   public Image getImage() {
      return this.image;
   }

   public void setImage(Image image) {
      this.image = image;
      this.initImage();
      this.repaint();
   }

   public ImageButton.ImageRotation getRotation() {
      return this.rotation;
   }

   public int getImageMargin() {
      return this.margin;
   }

   public void update(Graphics g) {
      super.update(g);
      this.paint(g);
   }

   public void paint(Graphics g0) {
      super.paint(g0);
      if (this.image != null) {
         Graphics2D g = (Graphics2D)g0;
         String text = this.getText();
         boolean drawtext = text != null && text.length() > 0;
         FontMetrics fm = g.getFontMetrics();
         float opacity = this.isEnabled() ? 1.0F : 0.5F;
         int width = this.getWidth();
         int height = this.getHeight();
         int rmargin = this.margin;
         int offset = this.pressed ? 1 : 0;
         int iwidth = this.image.getWidth((ImageObserver)null);
         int iheight = this.image.getHeight((ImageObserver)null);
         boolean ix = false;
         int iy = height / 2 - iheight / 2;
         int twidth;
         if (drawtext) {
            twidth = fm.stringWidth(text);
         } else {
            rmargin = 0;
            twidth = 0;
         }

         int ix1;
         switch(this.rotation) {
         case LEFT:
            ix1 = width / 2 - twidth / 2 - iwidth - rmargin;
            break;
         case CENTER:
            ix1 = width / 2 - iwidth / 2;
            break;
         case RIGHT:
            ix1 = width / 2 + twidth / 2 + rmargin;
            break;
         default:
            throw new IllegalStateException("Unknown rotation!");
         }

         Composite c = g.getComposite();
         g.setComposite(AlphaComposite.getInstance(3, opacity));
         g.drawImage(this.image, ix1 + offset, iy + offset, (ImageObserver)null);
         g.setComposite(c);
         this.pressed = false;
      }

   }

   protected static Image loadImage(String path) {
      Image image = Images.getImage(path);
      return image.getScaledInstance(SwingUtil.magnify(image.getWidth((ImageObserver)null)), SwingUtil.magnify(image.getHeight((ImageObserver)null)), 4);
   }

   protected void initImage() {
      if (this.image != null) {
         this.setPreferredSize(new Dimension(this.image.getWidth((ImageObserver)null) + 10, this.image.getHeight((ImageObserver)null) + 10));
      }

   }

   private void initListeners() {
      this.initImage();
      this.addMouseListener(new MouseListener() {
         public void mouseClicked(MouseEvent e) {
         }

         public void mouseEntered(MouseEvent e) {
         }

         public void mouseExited(MouseEvent e) {
         }

         public void mousePressed(MouseEvent e) {
            ImageButton.this.pressed = true;
         }

         public void mouseReleased(MouseEvent e) {
         }
      });
      this.addKeyListener(new KeyListener() {
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 32) {
               ImageButton.this.pressed = true;
            }

         }

         public void keyReleased(KeyEvent e) {
            ImageButton.this.pressed = false;
         }

         public void keyTyped(KeyEvent e) {
         }
      });
   }

   public static enum ImageRotation {
      LEFT,
      CENTER,
      RIGHT;
   }
}
